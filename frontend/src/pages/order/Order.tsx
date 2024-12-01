import { useEventStore } from '../../types/StoreType/EventState';

function Order() {
  const { faceImg, eventScheduleId, selectedSeats } = useEventStore();
  console.log('넘겨받은 데이터:', { faceImg, eventScheduleId, selectedSeats });

  return (
    <div>
      <h1>다음 단계</h1>
      {/* 데이터를 활용 */}
    </div>
  );
}

export default Order;
