import TicketingHeader from "../../components/ticketing/TicketingHeader.tsx";
import React, { useEffect, useState } from "react";
import { releaseSlot } from "../../service/queue/api.ts";
import { useStore } from "zustand/index";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { userStore } from "../../stores/UserStore.tsx";
import { WorkStatus } from "../../types/queue.ts";
import { useNavigate } from "react-router-dom";
import { unLockSeats } from "../../service/selectseat/api.ts";
import { createOrder } from "../../service/order/api.ts";
import { CreateOrderRequest } from "../../types/order.ts";
import PortOne from "@portone/browser-sdk/v2";
import { Helmet } from "react-helmet-async";

const STORE_ID: string = import.meta.env.VITE_STORE_ID;
const CHANNEL_KEY: string = import.meta.env.VITE_CHANNEL_KEY;
const PORTONE_WEBHOOK_URL: string = import.meta.env.VITE_PORTONE_WEBHOOK_URL;
const WORK_WEBSOCKET_URL: string = import.meta.env.VITE_WORK_WEBSOCKET_URL;
const REDIRECT_URL: string = import.meta.env.VITE_REDIRECT_URL;

function Order() {
  const navigate = useNavigate();
  const event = useStore(eventDetailStore);
  const user = useStore(userStore);

  const eventId = event.eventId;
  const faceId = event.faceId;
  const faceImg = event.faceImg;
  const eventScheduleId = event.scheduleId;
  const selectedSeats = event.selectedSeats;
  const setFaceImg = event.setFaceImg;
  const setSelectedSeats = event.setSelectedSeats;
  const eventTitle = event.title;
  const eventStage = event.stageName;
  const eventDate = event.choiceDate;
  const eventTime = event.choiceTime;
  const setPersistFaceId = event.setFaceId;
  const setPersistFaceImg = event.setFaceImg;

  const [isWaitingPayment, setWaitingPayment] = useState<boolean>(false);
  const SEAT_FEE = 2000; // 수수료 per seat1
  const totalAmount =
    selectedSeats.reduce((sum, seat) => sum + seat.price, 0) +
    selectedSeats.length * SEAT_FEE;
  const customMessage = selectedSeats
    .map((seat) => `${seat.grade}석 ${seat.row}열 ${seat.col}번`)
    .join(", ");

  const randomId = () => {
    return Array.from(crypto.getRandomValues(new Uint32Array(2)))
      .map((word) => word.toString(16).padStart(8, "0"))
      .join("");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setWaitingPayment(true);

    const paymentId = randomId();

    try {
      console.log("Creating order...");

      const createOrderRequest: CreateOrderRequest = {
        paymentId: paymentId, // 결제 ID
        eventScheduleId: eventScheduleId, // 이벤트 일정 ID
        faceId: faceId, // 얼굴 ID
        faceImgUrl: faceImg, // 얼굴 이미지 URL
        selectSeatInfoList: Array.from(
          new Set(
            selectedSeats.map((seat) =>
              JSON.stringify({
                seatMappingId: seat.seatMappingId,
                seatPrice: seat.price,
                seatGrade: seat.grade,
              }),
            ),
          ),
        ).map((item) => JSON.parse(item)),
      };

      await createOrder(createOrderRequest);

      console.log("Sending payment request to PortOne...");
      await PortOne.requestPayment({
        storeId: STORE_ID,
        paymentId: paymentId,
        orderName: `${eventTitle} - ${eventDate} ${eventTime} (${eventStage})`,
        totalAmount: totalAmount,
        currency: "CURRENCY_KRW",
        channelKey: CHANNEL_KEY,
        payMethod: "EASY_PAY",
        redirectUrl: REDIRECT_URL,
        easyPay: {
          easyPayProvider: "EASY_PAY_PROVIDER_KAKAOPAY",
        },
        bypass: {
          kakaopay: {
            custom_message: customMessage,
          },
        },
        windowType: {
          pc: "IFRAME",
          mobile: "REDIRECTION",
        },
        noticeUrls: [PORTONE_WEBHOOK_URL],
      });

      console.log("Payment request sent successfully!");
    } catch (error) {
      console.error("Error creating order or requesting payment:", error);
      alert("주문 생성에 실패했습니다. 다시 시도해주세요.");
      setWaitingPayment(false);
    }
  };

  let wsInstance: WebSocket | null = null;

  // 페이지 이동 시 웹소켓 메시지 전송
  const notifyNavigation = (message: string) => {
    if (wsInstance?.readyState === WebSocket.OPEN) {
      wsInstance.send(message);
    }
  };

  const handleBeforeStep = async () => {
    setPersistFaceId(0);
    setPersistFaceImg("");

    notifyNavigation("BEFORE_STEP");

    navigate(`/ticketing/register-face`);
  };

  const connectWebSocket = () => {
    const encodedToken = encodeURIComponent(user.accessToken);
    const WEBSOCKET_URL = `${WORK_WEBSOCKET_URL}/${eventId}/${eventScheduleId}?Authorization=${encodedToken}`;
    const ws = new WebSocket(WEBSOCKET_URL);

    ws.onopen = () => {
      console.log("WebSocket 연결 성공");
    };

    ws.onmessage = (event: MessageEvent) => {
      const handleMessage = async () => {
        try {
          if (event.data === WorkStatus.ORDER_RIGHT_LOST) {
            await releaseSlot(eventId);
            const payload = {
              eventScheduleId: eventScheduleId,
              seatMappingIds: selectedSeats.map((seat) => seat.seatMappingId),
            };
            await unLockSeats(payload); // 좌석 선점 해제 API 호출
            alert("세션이 만료되었습니다. 창을 닫습니다.");
            ws.close();
            window.close();
          } else if (event.data === WorkStatus.SEAT_RESERVATION_RELEASED) {
            alert("좌석 선점이 만료되었습니다.");
            setSelectedSeats([]);
            setFaceImg("");
            navigate(`/ticketing/select-seat`);
          } else if (event.data === WorkStatus.ORDER_FAILED) {
            setWaitingPayment(false);
            alert("결제에 실패했습니다. 다시 시도해주세요.");
            ws.close();
            navigate(`/ticketing/register-face`);
          } else if (event.data === WorkStatus.ORDER_PAID) {
            const payload = {
              eventScheduleId: eventScheduleId,
              seatMappingIds: selectedSeats.map((seat) => seat.seatMappingId),
            };
            await unLockSeats(payload); // 좌석 선점 해제 API 호출
            setWaitingPayment(false);
            alert("결제에 성공했습니다.");
            ws.close();
            window.close();
          }
        } catch (error) {
          console.error("WebSocket 메시지 처리 중 오류 발생:", error);
        }
      };

      handleMessage();
    };

    ws.onclose = () => {
      console.log("WebSocket 연결 종료");
    };

    ws.onerror = (error) => {
      console.error("WebSocket 오류:", error);
    };

    return ws;
  };

  useEffect(() => {
    wsInstance = connectWebSocket();

    return () => {
      wsInstance?.close();
    };
  }, []);

  return (
    <div className="relative w-full h-auto min-h-screen bg-[#F0F0F0]">
      <Helmet>
        <title>티켓팅 - 결제</title>
      </Helmet>
      {/* 헤더 */}
      <div className="relative z-10 h-[192px] sm:bg-black">
        <TicketingHeader step={4} />
      </div>

      <div className="relative -mt-20 sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-center px-4 z-10 space-y-8 sm:space-y-0">
        <div className="max-w-[1200px] w-full bg-white border border-gray-300 rounded-lg shadow-lg">
          {/* 주문 정보 */}
          <div className="px-6 pb-6 space-y-6">
            <div className="flex flex-col sm:grid sm:grid-cols-2 gap-4">
              <div className="mt-3">
                <div className="text-sm text-gray-500">제목</div>
                <div className="font-bold text-base">{eventTitle}</div>
              </div>
              <div>
                <div className="mt-3 text-sm text-gray-500">얼굴 정보</div>
                {faceImg && (
                  <div>
                    <img
                      src={faceImg}
                      alt="사용자 얼굴"
                      className="w-32 h-32 border border-gray-300 rounded-md object-cover"
                    />
                  </div>
                )}
              </div>
              <div>
                <div className="text-sm text-gray-500">장소 및 시간</div>
                <div className="text-base">
                  {eventStage} {eventDate} {eventTime}
                </div>
              </div>
              <div>
                <div className="text-sm text-gray-500">좌석 정보</div>
                <div className="text-base space-y-1">
                  {selectedSeats.map((seat, index) => (
                    <div key={index}>
                      {seat.grade}석 {seat.row}열 {seat.col}번 (
                      {seat.price.toLocaleString()}원)
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex flex-col sm:grid sm:grid-cols-2 gap-4 border-t pt-4 sm:pt-2">
              <div>
                <h2 className="text-sm text-gray-500">수수료</h2>
                <p className="text-base">
                  {selectedSeats.length}매 x {SEAT_FEE.toLocaleString()}원 ={" "}
                  {(selectedSeats.length * SEAT_FEE).toLocaleString()}원
                </p>
              </div>
              <div className="flex flex-col sm:flex-row sm:justify-end mt-4">
                <h2 className="text-lg font-semibold">
                  총 금액: {totalAmount.toLocaleString("ko-KR")}원
                </h2>
              </div>
            </div>
          </div>

          <div className="p-4 flex justify-between sm:-mt-5">
            <button
              onClick={handleBeforeStep}
              className="bg-[#666666] w-[45%] sm:w-auto px-4 py-2 text-white border border-black text-sm"
            >
              이전 단계
            </button>

            <button
              onClick={handleSubmit}
              className="bg-[#CF1212] w-[45%] sm:w-auto px-4 py-2 text-white border border-black text-sm"
              disabled={isWaitingPayment}
            >
              {isWaitingPayment ? "결제 진행 중..." : "결제하기"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Order;
