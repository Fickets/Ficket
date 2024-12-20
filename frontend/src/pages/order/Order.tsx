import TicketingHeader from "../../components/ticketing/TicketingHeader.tsx";
import OrderConfirmation from "../../components/order/OrderConfirmation.tsx";
import { useEffect } from "react";
import { releaseSlot } from "../../service/queue/api.ts";
import { useStore } from "zustand/index";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { userStore } from "../../stores/UserStore.tsx";
import { WorkStatus } from "../../types/queue.ts";
import { useNavigate } from "react-router-dom";
import { unLockSeats } from "../../service/selectseat/api.ts";

function Order() {
  const navigate = useNavigate();
  const event = useStore(eventDetailStore);
  const user = useStore(userStore);

  const faceImg = event.faceImg;
  const eventScheduleId = event.scheduleId;
  const selectedSeats = event.selectedSeats;
  const setFaceImg = event.setFaceImg;
  const setSelectedSeats = event.setSelectedSeats;
  console.log("넘겨받은 데이터:", { faceImg, eventScheduleId, selectedSeats });

  const connectWebSocket = (onExpire: () => void) => {
    const encodedToken = encodeURIComponent(user.accessToken);
    const WEBSOCKET_URL = `ws://localhost:9000/work-status/${user.userId}?Authorization=${encodedToken}`;
    const ws = new WebSocket(WEBSOCKET_URL);

    ws.onopen = () => {
      console.log("WebSocket 연결 성공");
    };

    ws.onmessage = async (event: MessageEvent) => {
      try {
        console.log("WebSocket 메시지 수신:", event.data);

        if (event.data === WorkStatus.ORDER_RIGHT_LOST) {
          alert("세션이 만료되었습니다. 창을 닫습니다.");
          onExpire(); // TTL 만료 처리
          ws.close();
          const payload = {
            eventScheduleId: eventScheduleId,
            seatMappingIds: selectedSeats.map((seat) => seat.seatMappingId),
          };
          await unLockSeats(payload); // 좌석 선점 해제 API 호출
          window.close();
        } else if (event.data === WorkStatus.SEAT_RESERVATION_RELEASED) {
          alert("좌석 선점이 만료되었습니다.");
          setSelectedSeats([]);
          setFaceImg(null);
          navigate(`/ticketing/select-seat`);
        }
      } catch (error) {
        console.error("WebSocket 메시지 파싱 실패:", error);
      }
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
    let ws: WebSocket;
    let manualClose = false; // 플래그로 창 닫힘 구분

    const onExpire = () => {
      manualClose = true;
    };

    // WebSocket 연결
    ws = connectWebSocket(onExpire);

    const handleUnload = async () => {
      if (!manualClose) {
        // 메시지를 통해 창이 닫히는 경우는 제외
        try {
          const payload = {
            eventScheduleId: eventScheduleId,
            seatMappingIds: selectedSeats.map((seat) => seat.seatMappingId),
          };
          await unLockSeats(payload); // 좌석 선점 해제 API 호출
          await releaseSlot(event.eventId);
          console.log("Slot released successfully.");
        } catch (error) {
          console.error("Error releasing slot:", error);
        }
      }
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

      {/* 메인 UI */}
      <div className="relative -mt-8 sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-start space-y-4 sm:space-y-0 sm:space-x-8 px-4 z-10">
        <div className="flex-1 max-w-[400px]">
          <OrderConfirmation />
        </div>
      </div>
    </div>
  );
}

export default Order;
