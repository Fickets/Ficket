import { useEventStore } from '../../types/StoreType/EventState';
import TicketingHeader from '../../components/ticketing/TicketingHeader.tsx';
import OrderConfirmation from '../../components/order/OrderConfirmation.tsx';

function Order() {
  const { faceImg, eventScheduleId, selectedSeats } = useEventStore();
  console.log('넘겨받은 데이터:', { faceImg, eventScheduleId, selectedSeats });

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
