import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../../assets/logo.png';
import RightPanel from '../../components/selectseat/RightPanel';
import SeatMap from '../../components/selectseat/SeatMap';
import {
  fetchAllSeatStatus,
  fetchEventSeatSummary,
  fetchSeatCntGrade,
  lockSeats,
} from '../../service/selectseat/api';
import {
  EventSeatSummary,
  SeatCntGrade,
  SeatStatusResponse,
} from '../../types/selectseat';
import { useEventStore } from '../../types/StoreType/EventState';

const SelectSeat = () => {
  const navigate = useNavigate();
  const {
    eventId,
    eventScheduleId,
    eventTitle,
    eventDate,
    eventTime,
    eventStage,
    setSelectedSeats, // Zustand에서 가져오기
  } = useEventStore();
  const [eventSummary, setEventSummary] = useState<EventSeatSummary | null>(
    null
  );
  const [seatCntGrade, setSeatCntGrade] = useState<SeatCntGrade[] | null>(null);
  const [seatStatusResponse, setSeatStatusResponse] = useState<
    SeatStatusResponse[] | null
  >(null);
  const [selectedSeats, setLocalSelectedSeats] = useState<
    { seatMappingId: number; grade: string; row: string; col: string }[]
  >([]);
  const [gradeColors, setGradeColors] = useState<{
    [key: string]: string;
  } | null>(null);

  const generateDistinctColors = (totalGrades: number) => {
    const colors: string[] = [];
    const step = 360 / totalGrades; // Hue 간격 계산

    for (let i = 0; i < totalGrades; i++) {
      const hue = Math.round(i * step); // Hue 값
      const color = `hsl(${hue}, 70%, 50%)`; // HSL 색상 (포화도 70%, 밝기 50%)
      colors.push(color);
    }

    return colors;
  };

  useEffect(() => {
    const loadEventData = async () => {
      if (eventScheduleId) {
        try {
          const [summary, seatGrades, seatStatusList] = await Promise.all([
            fetchEventSeatSummary(eventScheduleId),
            fetchSeatCntGrade(eventScheduleId),
            fetchAllSeatStatus(eventScheduleId),
          ]);

          setEventSummary(summary);
          setSeatCntGrade(seatGrades);
          setSeatStatusResponse(seatStatusList);

          // gradeColors 생성
          const distinctColors = generateDistinctColors(seatGrades.length);
          const newGradeColors: { [key: string]: string } = {};
          seatGrades.forEach((grade, index) => {
            newGradeColors[grade.partitionName] = distinctColors[index];
          });
          setGradeColors(newGradeColors);
        } catch (error) {
          console.error('Error loading event data:', error);
        }
      }
    };

    loadEventData();
  }, [eventScheduleId]);

  if (!eventSummary || !seatCntGrade || !seatStatusResponse || !gradeColors) {
    return <div>Loading...</div>; // 로딩 상태 표시
  }
  const refreshSeatsAndGrades = async () => {
    if (eventScheduleId) {
      try {
        const seatGrades = await fetchSeatCntGrade(eventScheduleId);
        const seatStatusList = await fetchAllSeatStatus(eventScheduleId);

        setSeatCntGrade(seatGrades);
        setSeatStatusResponse(seatStatusList);

        // gradeColors 갱신
        const distinctColors = generateDistinctColors(seatGrades.length);
        const newGradeColors: { [key: string]: string } = {};
        seatGrades.forEach((grade, index) => {
          newGradeColors[grade.partitionName] = distinctColors[index];
        });
        setGradeColors(newGradeColors);
      } catch (error) {
        console.error('Error refreshing seats and grades:', error);
      }
    }
  };

  const handleNextStep = async () => {
    if (selectedSeats.length === 0) {
      alert('좌석을 선택해주세요.');
    } else if (selectedSeats.length > eventSummary.reservationLimit) {
      alert('예매 한도를 초과했습니다.');
    } else {
      try {
        const payload = {
          eventScheduleId: eventScheduleId,
          reservationLimit: eventSummary.reservationLimit,
          seatMappingIds: selectedSeats.map((seat) => seat.seatMappingId),
        };

        await lockSeats(payload); // 좌석 선점 API 호출

        setSelectedSeats(selectedSeats);

        navigate('/ticketing/register-face');
      } catch (error) {
        console.error('Error locking seats:', error);

        // 실패 시 사용자에게 알림
        alert('좌석 선점에 실패했습니다. 다시 시도해주세요.');
      }
    }
  };

  const handleBeforeStep = () => {
    navigate(`/ticketing/select-session/${eventId}`);
  };

  const handleRefresh = async () => {
    await refreshSeatsAndGrades(); // 필요한 데이터만 새로고침
  };
  return (
    <div className="relative w-full h-auto min-h-screen bg-[#F0F0F0]">
      {/* 상단 바 */}
      <div className="relative z-10 h-[192px] bg-black text-white">
        {/* 데스크탑: 로고와 텍스트 */}
        <div className="hidden sm:flex items-center justify-between px-4 sm:px-8 py-3">
          <div className="flex items-center">
            <img
              src={logo}
              alt="Logo"
              className="w-8 h-8 sm:w-10 sm:h-10 mr-2"
            />
            <h3 className="text-white text-sm sm:text-lg font-semibold">
              Ficket 티켓예매
            </h3>
          </div>
        </div>

        {/* 데스크탑: 기존 단계 표시 */}
        <div className="hidden sm:flex justify-center py-4 -mt-4">
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#D9D9D9] border border-black font-bold flex items-center justify-center text-xs sm:text-base">
            <span>01 관람일 / 회차선택</span>
          </div>
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#E94343] border border-black text-white font-bold flex items-center justify-center text-xs sm:text-base">
            <span>02 좌석 선택</span>
          </div>
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#D9D9D9] border border-black font-bold flex items-center justify-center text-xs sm:text-base">
            <span>03 얼굴 인식</span>
          </div>
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#D9D9D9] border border-black font-bold flex items-center justify-center text-xs sm:text-base">
            <span>04 결제하기</span>
          </div>
        </div>
      </div>

      {/* 좌석배치도 & 우측 패널*/}
      <div className="relative -mt-8 sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-start space-y-4 sm:space-y-0 sm:space-x-8 px-4 z-10">
        <SeatMap
          eventStageImg={eventSummary.eventStageImg}
          reservationLimit={eventSummary.reservationLimit}
          seatStatusResponse={seatStatusResponse}
          onSeatSelect={setLocalSelectedSeats} // 좌석 선택 콜백
          selectedSeats={selectedSeats} // 선택된 좌석
          gradeColors={gradeColors} // 등급별 색상 전달
        />

        <div className="flex flex-col items-center">
          <RightPanel
            gradeColors={gradeColors} // 등급별 색상 전달
            posterMobileUrl={eventSummary.posterMobileUrl}
            seatGradeInfoList={eventSummary.seatGradeInfoList}
            seatCntGrade={seatCntGrade}
            eventTitle={eventTitle}
            eventStage={eventStage}
            eventDate={eventDate}
            eventTime={eventTime}
            selectedSeats={selectedSeats}
          />

          {/* 버튼들 */}
          <div className="w-[210px] -mt-7">
            <button
              onClick={handleNextStep}
              className="bg-red-500 text-white w-full py-2 font-bold text-center"
            >
              다음 단계
            </button>
            <div className="flex justify-between items-center w-full mt-2 gap-1">
              <button
                onClick={handleBeforeStep}
                className="bg-gray-300 text-black flex-1 py-1 text-center"
              >
                이전 단계
              </button>
              <button
                onClick={handleRefresh}
                className="bg-gray-300 text-black flex-1 py-1 text-center"
              >
                새로고침
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SelectSeat;
