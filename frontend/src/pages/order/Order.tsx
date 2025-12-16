import TicketingHeader from "../../components/ticketing/TicketingHeader.tsx";
import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router";
import { useStore } from "zustand";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { createOrder, getOrderStatus } from "../../service/order/api.ts";
import { CreateOrderRequest, OrderStatus } from "../../types/order.ts";
import PortOne from "@portone/browser-sdk/v2";
import { Helmet } from "react-helmet-async";
import {
  checkTicketingStatus,
  leaveTicketing,
} from "../../service/queue/api.ts";

const STORE_ID: string = import.meta.env.VITE_STORE_ID;
const CHANNEL_KEY: string = import.meta.env.VITE_CHANNEL_KEY;
const PORTONE_WEBHOOK_URL: string = import.meta.env.VITE_PORTONE_WEBHOOK_URL;
const REDIRECT_URL: string = import.meta.env.VITE_REDIRECT_URL;

// 폴링 설정
const POLLING_INTERVAL = 1000; // 1초마다 체크
const MAX_POLLING_TIME = 100000; // 최대 1분

function Order() {
  const event = useStore(eventDetailStore);
  const navigate = useNavigate();
  const eventId = event.eventId;
  const faceId = event.faceId;
  const faceImg = event.faceImg;
  const eventScheduleId = event.scheduleId;
  const selectedSeats = event.selectedSeats;
  const eventTitle = event.title;
  const eventStage = event.stageName;
  const eventDate = event.choiceDate;
  const eventTime = event.choiceTime;
  const setPersistFaceId = event.setFaceId;
  const setPersistFaceImg = event.setFaceImg;

  const [isWaitingPayment, setWaitingPayment] = useState<boolean>(false);

  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const pollingStartTimeRef = useRef<number | null>(null);

  const SEAT_FEE = 2000;
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

  // 폴링 시작
  const startPolling = (orderId: number) => {
    pollingStartTimeRef.current = Date.now();

    pollingIntervalRef.current = setInterval(async () => {
      try {
        const elapsed = Date.now() - (pollingStartTimeRef.current || 0);

        // 최대 폴링 시간 초과 체크
        if (elapsed > MAX_POLLING_TIME) {
          stopPolling();
          alert(
            "결제 확인 시간이 초과되었습니다. 마이페이지에서 주문 상태를 확인해주세요.",
          );
          setWaitingPayment(false);
          return;
        }

        const status = await getOrderStatus(orderId);
        console.log("Current order status:", status);

        if (status === OrderStatus.COMPLETED) {
          stopPolling();

          try {
            await leaveTicketing(eventId);
            console.log("티켓팅 종료 완료");
          } catch (error) {
            console.error("티켓팅 종료 실패:", error);
          }

          alert("결제가 완료되었습니다. 창이 자동으로 닫힙니다.");
          window.close();
        } else if (status === OrderStatus.CANCELLED) {
          stopPolling();
          alert("결제가 실패했거나 취소되었습니다.");
          setWaitingPayment(false);
        }
      } catch (error) {
        console.error("Error polling order status:", error);
      }
    }, POLLING_INTERVAL);
  };

  // 폴링 중지
  const stopPolling = () => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current);
      pollingIntervalRef.current = null;
    }
    pollingStartTimeRef.current = null;
  };

  // 컴포넌트 언마운트 시 폴링 정리
  useEffect(() => {
    return () => {
      stopPolling();
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    const isInTicketing = await checkTicketingStatus(eventId);

    if (!isInTicketing) {
      alert("예매 가능 시간이 만료되었습니다. 다시 대기열에 진입해주세요.");
      navigate(`/queues/${eventId}`);
      return;
    }

    e.preventDefault();
    setWaitingPayment(true);

    const paymentId = randomId();

    try {
      const createOrderRequest: CreateOrderRequest = {
        paymentId: paymentId,
        eventScheduleId: eventScheduleId,
        faceId: faceId,
        faceImgUrl: faceImg,
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

      // 주문 생성 및 orderId 가져오기
      const orderResponse = await createOrder(createOrderRequest);
      console.log("Order created with ID:", orderResponse.orderId);

      // 폴링 시작
      startPolling(orderResponse.orderId);

      // 포트원 결제 요청
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
    } catch (error) {
      console.error("Error creating order or requesting payment:", error);
      alert("주문 생성에 실패했습니다. 다시 시도해주세요.");
      setWaitingPayment(false);
      stopPolling();
    }
  };

  const handleBeforeStep = async () => {
    const isInTicketing = await checkTicketingStatus(eventId);

    if (!isInTicketing) {
      alert("예매 가능 시간이 만료되었습니다. 다시 대기열에 진입해주세요.");
      navigate(`/queues/${eventId}`);
      return;
    }

    setPersistFaceId(0);
    setPersistFaceImg("");

    navigate(`/ticketing/register-face`);
  };

  return (
    <div className="relative w-full h-auto min-h-screen bg-[#F0F0F0]">
      <Helmet>
        <title>티켓팅 - 결제</title>
      </Helmet>

      <div className="relative z-10 h-[192px] sm:bg-black">
        <TicketingHeader step={4} />
      </div>

      <div className="relative -mt-20 sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-center px-4 z-10 space-y-8 sm:space-y-0">
        <div className="max-w-[1200px] w-full bg-white border border-gray-300 rounded-lg shadow-lg">
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
              disabled={isWaitingPayment}
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
