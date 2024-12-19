import TicketingHeader from "../../components/ticketing/TicketingHeader.tsx";
import OrderConfirmation from "../../components/order/OrderConfirmation.tsx";
import { useEffect } from "react";
import { releaseSlot } from "../../service/queue/api.ts";
import { useStore } from "zustand/index";
import { eventDetailStore } from "../../stores/EventStore.tsx";

function Order() {
  const event = useStore(eventDetailStore);

  const eventId = event.eventId;
  const faceImg = event.faceImg;
  const eventScheduleId = event.scheduleId;
  const selectedSeats = event.selectedSeats;
  console.log("넘겨받은 데이터:", { faceImg, eventScheduleId, selectedSeats });

  useEffect(() => {
    // 창 닫힘 이벤트 처리
    const handleReleaseSlot = async () => {
      try {
        await releaseSlot(eventId);
        console.log("Slot released successfully.");
      } catch (error) {
        console.error("Error releasing slot:", error);
      }
    };

    window.addEventListener("unload", handleReleaseSlot);

    return () => {
      window.removeEventListener("unload", handleReleaseSlot);
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
