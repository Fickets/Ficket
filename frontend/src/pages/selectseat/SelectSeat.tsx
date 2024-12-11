import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import RightPanel from "../../components/selectseat/RightPanel";
import {
  fetchAllSeatStatus,
  fetchEventSeatSummary,
  fetchSeatCntGrade,
  lockSeats,
} from "../../service/selectseat/api";
import {
  EventSeatSummary,
  SeatCntGrade,
  SeatStatusResponse,
} from "../../types/selectseat";
import {
  AiOutlineArrowLeft,
  AiOutlineReload,
  AiOutlineDown,
  AiOutlineUp,
} from "react-icons/ai";
import DraggableSeatMap from "../../components/selectseat/DraggableSeatMap";
import TicketingHeader from "../../components/ticketing/TicketingHeader.tsx";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { useStore } from "zustand";

const SelectSeat = () => {
  const navigate = useNavigate();

  const event = useStore(eventDetailStore);
  console.log(event);
  // const eventId = event.eventId;
  const eventScheduleId = event.scheduleId;
  const eventTitle = event.title;
  const eventDate = event.choiceDate;
  const eventTime = event.choiceTime;
  const eventStage = event.stageName;

  const setSelectedSeats = event.setSelectedSeats;

  const [eventSummary, setEventSummary] = useState<EventSeatSummary | null>(
    null,
  );
  const [seatCntGrade, setSeatCntGrade] = useState<SeatCntGrade[] | null>(null);
  const [seatStatusResponse, setSeatStatusResponse] = useState<
    SeatStatusResponse[] | null
  >(null);
  const [selectedSeats, setLocalSelectedSeats] = useState<
    {
      seatMappingId: number;
      grade: string;
      row: string;
      col: string;
      price: number;
    }[]
  >([]);
  const [gradeColors, setGradeColors] = useState<{
    [key: string]: string;
  } | null>(null);
  const [showPricePopup, setShowPricePopup] = useState(false);
  const [detailsVisible, setDetailsVisible] = useState(true); // 상세 창 보이기 여부

  const generateDistinctColors = (totalGrades: number) => {
    const colors: string[] = [];
    const step = 360 / totalGrades;

    for (let i = 0; i < totalGrades; i++) {
      const hue = Math.round(i * step);
      const color = `hsl(${hue}, 70%, 50%)`;
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

          const distinctColors = generateDistinctColors(seatGrades.length);
          const newGradeColors: { [key: string]: string } = {};
          seatGrades.forEach((grade, index) => {
            newGradeColors[grade.partitionName] = distinctColors[index];
          });
          setGradeColors(newGradeColors);
        } catch (error) {
          console.error("Error loading event data:", error);
        }
      }
    };

    loadEventData();
  }, [eventScheduleId]);

  if (!eventSummary || !seatCntGrade || !seatStatusResponse || !gradeColors) {
    return <div>Loading...</div>;
  }

  const refreshSeatsAndGrades = async () => {
    if (eventScheduleId) {
      try {
        const seatGrades = await fetchSeatCntGrade(eventScheduleId);
        const seatStatusList = await fetchAllSeatStatus(eventScheduleId);

        setSeatCntGrade(seatGrades);
        setSeatStatusResponse(seatStatusList);

        const distinctColors = generateDistinctColors(seatGrades.length);
        const newGradeColors: { [key: string]: string } = {};
        seatGrades.forEach((grade, index) => {
          newGradeColors[grade.partitionName] = distinctColors[index];
        });
        setGradeColors(newGradeColors);
      } catch (error) {
        console.error("Error refreshing seats and grades:", error);
      }
    }
  };

  const handleNextStep = async () => {
    if (selectedSeats.length === 0) {
      alert("좌석을 선택해주세요.");
    } else if (selectedSeats.length > event.reservationLimit) {
      alert("예매 한도를 초과했습니다.");
    } else {
      try {
        const payload = {
          eventScheduleId: eventScheduleId,
          selectSeatInfoList: selectedSeats.map((seat) => ({
            seatMappingId: seat.seatMappingId,
            seatPrice: seat.price,
            seatGrade: seat.grade,
          })),
        };

        await lockSeats(payload);

        setSelectedSeats(selectedSeats);

        navigate("/ticketing/register-face");
      } catch (error: any) {
        alert(`${error.message}`);
      }
    }
  };

  const handleBeforeStep = () => {
    navigate("/ticketing/select-date");
  };

  const handleRefresh = async () => {
    await refreshSeatsAndGrades();
  };

  return (
    <div className="relative w-full h-auto min-h-screen bg-[#F0F0F0]">
      <div className="relative z-10 h-[192px] bg-black hidden sm:block">
        <TicketingHeader step={2} />
      </div>

      {/* 헤더 */}
      <div className="fixed top-0 left-0 w-full h-12 bg-gray-800 shadow-md flex items-center justify-between px-4 sm:hidden z-50">
        <button
          onClick={handleBeforeStep}
          className="text-white text-lg flex items-center"
        >
          <AiOutlineArrowLeft className="text-2xl" />
        </button>

        <div className="relative flex items-center justify-between">
          <button
            className="bg-gray-700 text-white text-sm px-3 py-1 rounded-full flex items-center mr-4"
            onClick={() => setShowPricePopup(!showPricePopup)}
          >
            좌석가격
          </button>

          {showPricePopup && (
            <div className="absolute top-12 right-0 bg-gray-800 text-white rounded-lg shadow-lg w-60 p-4 z-50">
              <div className="flex flex-col space-y-2">
                {eventSummary.seatGradeInfoList.map((seat) => (
                  <div
                    key={seat.grade}
                    className="flex justify-between items-center border-b border-gray-700 pb-2 last:border-b-0"
                  >
                    <span
                      style={{ color: gradeColors[seat.grade] }}
                      className="font-semibold text-sm"
                    >
                      {seat.grade}
                    </span>
                    <span className="text-sm font-medium">
                      {seat.price.toLocaleString()}원
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          <button
            onClick={handleRefresh}
            className="text-white text-lg flex items-center"
          >
            <AiOutlineReload className="text-2xl" />
          </button>
        </div>
      </div>

      <div className="relative -mt-8 sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-start space-y-4 sm:space-y-0 sm:space-x-0 px-4 z-10">
        <DraggableSeatMap
          eventStageImg={eventSummary.eventStageImg}
          reservationLimit={event.reservationLimit}
          seatStatusResponse={seatStatusResponse}
          onSeatSelect={setLocalSelectedSeats}
          selectedSeats={selectedSeats}
          gradeColors={gradeColors}
        />

        <div className="flex flex-col items-center sm:w-auto w-full">
          <div className="hidden sm:flex flex-col items-center">
            <RightPanel
              gradeColors={gradeColors}
              posterMobileUrl={eventSummary.posterMobileUrl}
              seatGradeInfoList={eventSummary.seatGradeInfoList}
              seatCntGrade={seatCntGrade}
              eventTitle={eventTitle}
              eventStage={eventStage}
              eventDate={eventDate}
              eventTime={eventTime}
              selectedSeats={selectedSeats}
            />
          </div>
          <div className="w-[235px] -mt-[25px] relative">
            <div className="hidden sm:block">
              <div className="w-full mt-4">
                <button
                  onClick={handleNextStep}
                  className="bg-red-500 text-white w-full py-2 text-center border border-black"
                >
                  다음
                </button>
              </div>
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

            <div className="sm:hidden fixed bottom-0 left-0 w-full bg-gray-900 text-white p-4 z-50">
              <div className="flex justify-between items-center border-b border-gray-600 pb-2">
                <span className="text-sm">
                  {selectedSeats
                    .reduce<{ grade: string; count: number }[]>(
                      (summary, seat) => {
                        const existing = summary.find(
                          (item) => item.grade === seat.grade,
                        );
                        if (existing) {
                          existing.count++;
                        } else {
                          summary.push({ grade: seat.grade, count: 1 });
                        }
                        return summary;
                      },
                      [],
                    )
                    .map((item) => `${item.grade}석 ${item.count}매`)
                    .join(", ")}
                </span>
                <button
                  className="text-lg flex items-center"
                  onClick={() => setDetailsVisible(!detailsVisible)}
                >
                  {detailsVisible ? <AiOutlineDown /> : <AiOutlineUp />}
                </button>
              </div>

              {detailsVisible && (
                <>
                  <div className="mt-4 space-y-2">
                    {selectedSeats.map((seat, index) => (
                      <div
                        key={index}
                        className="flex justify-between items-center text-sm border-b border-gray-600 pb-2"
                      >
                        <span>
                          {seat.grade}석{" "}
                          <span className="text-red-300">{seat.row}열</span>{" "}
                          <span className="text-red-300">{seat.col}번</span>
                        </span>
                      </div>
                    ))}
                  </div>
                  <div className="border-t border-gray-600 my-2"></div>
                </>
              )}

              <div className="flex justify-between w-full p-4">
                <button
                  className="bg-red-500 text-white flex-1 py-2 text-center border border-black"
                  disabled
                >
                  총 {selectedSeats.length}매
                </button>
                <button
                  onClick={handleNextStep}
                  className="bg-red-500 text-white flex-1 py-2 text-center border border-black ml-2"
                >
                  다음
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SelectSeat;
