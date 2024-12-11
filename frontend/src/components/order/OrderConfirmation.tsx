import KakaoPay from "./KakaoPay.tsx";
import { useStore } from "zustand";
import { eventDetailStore } from "../../stores/EventStore.tsx";

const OrderConfirmation = () => {
  const event = useStore(eventDetailStore);

  const selectedSeats = event.selectedSeats;
  const eventTitle = event.title;
  const eventStage = event.stageName;
  const eventDate = event.choiceDate;
  const eventTime = event.choiceTime;

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
          <div className="text-sm text-gray-500">좌석 정보</div>
          <div className="text-base">
            {selectedSeats.map((seat, index) => (
              <div key={index}>
                {seat.grade}석 {seat.row}열 {seat.col}번
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="p-4 -mt-4">
        <KakaoPay />
      </div>
    </div>
  );
};

export default OrderConfirmation;
