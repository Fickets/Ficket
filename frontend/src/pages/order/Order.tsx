import TicketingHeader from "../../components/ticketing/TicketingHeader.tsx";
import { useEffect, useState } from "react";
import { releaseSlot } from "../../service/queue/api.ts";
import { useStore } from "zustand/index";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { userStore } from "../../stores/UserStore.tsx";
import { WorkStatus } from "../../types/queue.ts";
import { useNavigate } from "react-router-dom";
import { unLockSeats } from "../../service/selectseat/api.ts";
import { createOrder } from "../../service/order/api.ts";
import PortOne from "@portone/browser-sdk/v2";

const STORE_ID: string = import.meta.env.VITE_STORE_ID;
const CHANNEL_KEY: string = import.meta.env.VITE_CHANNEL_KEY;

function Order() {
  const navigate = useNavigate();
  const event = useStore(eventDetailStore);
  const user = useStore(userStore);

  const eventId = event.eventId;
  const faceImg = event.faceImg;
  const eventScheduleId = event.scheduleId;
  const selectedSeats = event.selectedSeats;
  const setFaceImg = event.setFaceImg;
  const setSelectedSeats = event.setSelectedSeats;
  const eventTitle = event.title;
  const eventStage = event.stageName;
  const eventDate = event.choiceDate;
  const eventTime = event.choiceTime;

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
    console.log(`Generated paymentId: ${paymentId}`);

    try {
      console.log("Creating order...");
      const createOrderRequest = {
        paymentId: paymentId,
        eventScheduleId: eventScheduleId,
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

      const formData = new FormData();

      if (faceImg) {
        formData.append("userFaceImg", faceImg);
      } else {
        alert("얼굴 이미지를 가져오지 못했습니다. 다시 시도해주세요.");
        setWaitingPayment(false);
        navigate(-1);
        return;
      }

      formData.append(
        "createOrderRequest",
        new Blob([JSON.stringify(createOrderRequest)], {
          type: "application/json",
        }),
      );

      const orderDetails = await createOrder(formData);
      console.log("Order created successfully:", orderDetails);

      console.log("Sending payment request to PortOne...");
      await PortOne.requestPayment({
        storeId: STORE_ID,
        paymentId: paymentId,
        orderName: `${eventTitle} - ${eventDate} ${eventTime} (${eventStage})`,
        totalAmount: totalAmount,
        currency: "CURRENCY_KRW",
        channelKey: CHANNEL_KEY,
        payMethod: "EASY_PAY",
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
        noticeUrls: [
          "https://0993-218-39-17-13.ngrok-free.app/api/v1/ticketing/order/valid",
        ],
      });

      console.log("Payment request sent successfully!");
    } catch (error) {
      console.error("Error creating order or requesting payment:", error);
      alert("주문 생성에 실패했습니다. 다시 시도해주세요.");
      setWaitingPayment(false);
    }
  };

  const connectWebSocket = () => {
    const encodedToken = encodeURIComponent(user.accessToken);
    const WEBSOCKET_URL = `ws://localhost:9000/work-status/${user.userId}?Authorization=${encodedToken}`;
    const ws = new WebSocket(WEBSOCKET_URL);

    ws.onopen = () => {
      console.log("WebSocket 연결 성공");
    };

    ws.onmessage = (event: MessageEvent) => {
      const handleMessage = async () => {
        try {
          console.log("WebSocket 메시지 수신:", event.data);

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
            setFaceImg(null);
            navigate(`/ticketing/select-seat`);
          } else if (event.data === WorkStatus.ORDER_FAILED) {
            setWaitingPayment(false);
            alert("결제에 실패했습니다. 다시 시도해주세요.");
            ws.close();
            navigate(`/ticketing/register-face`);
          } else if (event.data === WorkStatus.ORDER_PAID) {
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
    let ws = connectWebSocket();

    const handleUnload = async () => {
      const payload = {
        eventScheduleId: eventScheduleId,
        seatMappingIds: selectedSeats.map((seat) => seat.seatMappingId),
      };
      await unLockSeats(payload); // 좌석 선점 해제 API 호출
    };

    // 창 닫힘 이벤트 추가
    window.addEventListener("unload", handleUnload);

    return () => {
      // 컴포넌트 언마운트 시 WebSocket 종료 및 이벤트 제거
      ws.close();
      window.removeEventListener("unload", handleUnload);
    };
  }, []);

  return (
    <div className="relative w-full h-auto min-h-screen bg-[#F0F0F0]">
      {/* 헤더 */}
      <div className="relative z-10 h-[192px] bg-black hidden sm:block">
        <TicketingHeader step={4} />
      </div>

      <div className="relative -mt-8 sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-center px-4 z-10 space-y-8 sm:space-y-0">
        <div className="max-w-[1200px] w-full bg-white border border-gray-300 rounded-lg shadow-lg">
          {/* 주문 정보 */}
          <div className="px-6 pb-6 space-y-6">
            <div className="grid grid-cols-2 gap-4">
              <div className="mt-3">
                <div className="text-sm text-gray-500">제목</div>
                <div className="font-bold text-base">{eventTitle}</div>
              </div>
              <div className="mt-3">
                <div className="text-sm text-gray-500">일시</div>
                <div className="text-base">
                  {eventDate} {eventTime}
                </div>
              </div>
              <div>
                <div className="text-sm text-gray-500">장소</div>
                <div className="text-base">{eventStage}</div>
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
              <div>
                <div className="-mt-[80px] text-sm text-gray-500">
                  얼굴 정보
                </div>
                {faceImg && (
                  <div>
                    <img
                      src={URL.createObjectURL(faceImg)}
                      alt="사용자 얼굴"
                      className="w-32 h-32 border border-gray-300 rounded-md object-cover"
                    />
                  </div>
                )}
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 border-t pt-4">
              <div>
                <h2 className="text-sm text-gray-500">수수료</h2>
                <p className="text-base">
                  {selectedSeats.length}매 x {SEAT_FEE.toLocaleString()}원 ={" "}
                  {(selectedSeats.length * SEAT_FEE).toLocaleString()}원
                </p>
              </div>
              <div className="text-right mt-4">
                <h2 className="text-lg font-semibold">
                  총 금액: {totalAmount.toLocaleString("ko-KR")}원
                </h2>
              </div>
            </div>
          </div>

          {/* 결제 버튼 */}
          <div className="p-4 -mt-6">
            <button
              onClick={handleSubmit}
              className="w-full bg-red-500 text-white py-3 border border-black font-semibold text-lg rounded-md"
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
