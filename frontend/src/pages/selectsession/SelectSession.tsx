import { useNavigate } from 'react-router-dom';
import { useEventStore } from '../../types/StoreType/EventState';

const SelectSession = () => {
  const navigate = useNavigate();

  const setEventInfo = useEventStore((state) => state.setEventInfo);

  const handleNextStep = () => {
    const eventInfo = {
      eventId: 1,
      eventScheduleId: 6,
      eventTitle: 'LOVE IN SEOUL - 엔플라잉',
      eventDate: '2024-12-20',
      eventTime: '18:00',
      eventStage: '인터파크 유니버스',
    };

    setEventInfo(eventInfo);

    navigate(`/ticketing/select-seat`);
  };

  return (
    <div>
      <button
        className="bg-[#CF1212] w-[45%] sm:w-auto px-4 py-2 text-white border border-black text-sm"
        onClick={handleNextStep}
      >
        다음단계
      </button>
    </div>
  );
};

export default SelectSession;
