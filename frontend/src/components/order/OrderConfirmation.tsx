import { useEventStore } from "../../types/StoreType/EventState.ts";
import KakaoPay from "./KakaoPay.tsx";

const OrderConfirmation = () => {
  const {
    faceImg,
    eventScheduleId,
    selectedSeats,
    eventTitle,
    eventStage,
    eventDate,
    eventTime,
  } = useEventStore();
  // 등급별 매수 계산
  const gradeCounts = selectedSeats.reduce(
    (acc: Record<string, number>, ticket) => {
      acc[ticket.grade] = (acc[ticket.grade] || 0) + 1;
      return acc;
    },
    {},
  );

  return (
    <div className="max-w-sm mx-auto bg-white border border-gray-300 rounded-lg shadow-lg">
      <div className="border-b border-gray-300 p-4 text-center font-semibold text-lg">
        예약확인
      </div>
      <div className="p-4 space-y-4">
        <div>
          <div className="text-sm text-gray-500">제목</div>
          <div className="font-bold text-base">{eventTitle}</div>
        </div>
        <div>
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
          <div className="text-sm text-gray-500">티켓수</div>
          <div className="text-base">
            {Object.entries(gradeCounts).map(([grade, count], index) => (
              <div key={index}>
                {grade} {count}매
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="p-4">
        <KakaoPay />
      </div>
    </div>
  );
};

export default OrderConfirmation;
