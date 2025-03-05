import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchReservationRateRanking } from "../../service/reservationRateRanking/api.ts";
import {
  ReservationRateRankingResponse,
  RankingItem,
  Genre,
  Period,
} from "../../types/ReservationRateRanking";

const RankingTop50ByGenre = ({
  genre,
  period,
}: {
  genre: Genre;
  period: Period;
}) => {
  const navigate = useNavigate();
  const [rankingData, setRankingData] = useState<RankingItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const data = await fetchReservationRateRanking({ genre, period });
        setRankingData(addRanksToData(data));
      } catch (error) {
        console.error("Error fetching ranking data:", error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [genre, period]);

  // 동일 예매율에 따라 등수 처리
  const addRanksToData = (
    data: ReservationRateRankingResponse[],
  ): RankingItem[] => {
    let rank = 1;
    let previousRate = -1;
    let skipCount = 0;

    return data.map((item) => {
      if (item.reservationRate === previousRate) {
        skipCount++;
      } else {
        rank += skipCount;
        skipCount = 1;
      }
      previousRate = item.reservationRate;
      return { ...item, rank };
    });
  };

  // 클릭 이벤트 핸들러
  const handleEventClick = (eventId: number) => {
    navigate(`/events/detail/${eventId}`);
  };

  const formatSchedule = (schedules: string[]): string => {
    if (!schedules || schedules.length === 0) {
      return "일정 없음";
    }

    // 날짜만 추출 (YYYY-MM-DD)
    const formattedDates = schedules.map(
      (schedule) => schedule.split("T")[0], // "YYYY-MM-DDTHH:mm:ss"에서 "YYYY-MM-DD" 추출
    );

    const uniqueDates = Array.from(new Set(formattedDates)).sort(); // 중복 제거 및 정렬

    if (uniqueDates.length === 1) {
      // 단일 날짜일 경우
      return uniqueDates[0];
    }

    if (uniqueDates.length > 1) {
      // 이어진 날짜인지 확인
      const isSequential = uniqueDates.every((date, index, array) => {
        if (index === 0) return true; // 첫 번째 요소는 비교할 이전 값이 없음
        const prevDate = new Date(array[index - 1]);
        const currentDate = new Date(date);
        return (
          currentDate.getTime() - prevDate.getTime() === 24 * 60 * 60 * 1000 // 하루 차이인지 확인
        );
      });

      if (isSequential) {
        return `${uniqueDates[0]} ~ ${uniqueDates[uniqueDates.length - 1]}`;
      } else {
        // 비연속적인 날짜들
        return uniqueDates.join(", ");
      }
    }

    return "일정 없음";
  };

  return (
    <div className="mt-4">
      {loading ? (
        <div>Loading...</div>
      ) : (
        <div className="space-y-6">
          {rankingData.map((item) => (
            <div
              key={item.eventId}
              onClick={() => handleEventClick(item.eventId)}
              className="flex items-center border-b pb-4 cursor-pointer hover:bg-gray-100"
            >
              {/* 순위 */}
              <div className="text-lg font-bold w-8 shrink-0 text-center mr-2">
                {item.rank}
              </div>

              {/* 이미지 */}
              <div className="w-[80px] h-[110px] shrink-0 mr-4">
                <img
                  src={item.eventPcPosterUrl}
                  srcSet={`${item.eventMobilePosterUrl} 480w, ${item.eventPcPosterUrl} 1024w`}
                  sizes="(max-width: 640px) 80px, 110px"
                  alt={item.eventTitle}
                  className="w-full h-full object-cover"
                />
              </div>

              {/* 상세 정보 */}
              <div className="flex-1 flex flex-col sm:flex-row sm:items-center sm:justify-between">
                {/* 제목 */}
                <div className="w-[140px] truncate text-gray-800 font-semibold mb-1 sm:w-[200px] md:w-[400px]">
                  {item.eventTitle}
                </div>

                {/* 장소 */}
                <div className="text-gray-600 text-xs sm:text-sm mb-1 sm:mb-0 sm:text-center w-[140px] sm:w-[150px] md:w-[150px] truncate">
                  {item.eventStageName}
                </div>

                {/* 날짜 및 폐막 */}
                <div className="text-gray-500 text-xs sm:text-sm mb-1 sm:mb-0 sm:text-center w-[140px] sm:w-[200px] md:w-[200px] truncate">
                  {formatSchedule(item.eventDates)}
                  {item.isClosed && (
                    <div className="text-purple-500 font-medium">폐막</div>
                  )}
                </div>

                {/* 예매율 */}
                <div className="text-purple-500 font-semibold text-left w-[80px]">
                  {item.reservationRate}%
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RankingTop50ByGenre;
