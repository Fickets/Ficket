import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const SelectSession = () => {
  const navigate = useNavigate();

  const [evnetScheduleId, setEventScheduleId] = useState(1);

  const handleNextStep = () => {
    if (true) {
      navigate(`/ticketing/select-seat/${evnetScheduleId}`, {
        state: {
          eventId: 1,
          evnetScheduleId: 1,
          eventTitle: 'LOVE IN SEOUL - 엔플라잉',
          eventDate: '2024-12-20',
          eventTime: '18:00',
          eventStage: '인터파크 유니버스',
          eventPoster:
            'https://ficketresizebucket.s3.ap-northeast-2.amazonaws.com/pc_poster/poster/5f9ab135-0e55-4da9-b523-71ebb2617d9f.jpg',
        },
      });
    }
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
