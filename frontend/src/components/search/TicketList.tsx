import { SearchResult, Schedule, SaleType } from "../../types/search.ts";
import { useNavigate } from "react-router-dom";

interface TicketListProps {
  ticketList: SearchResult;
  onPageChange: () => void;
}

const formatSchedule = (schedules: Schedule[]): string => {
  if (!schedules || schedules.length === 0) {
    return "일정 없음";
  }

  // 날짜만 추출 (YYYY-MM-DD)
  const formattedDates = schedules.map(
    (schedule) => schedule.Schedule.split("T")[0], // "YYYY-MM-DDTHH:mm:ss"에서 "YYYY-MM-DD" 추출
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

const getSaleTypeStyle = (saleType: SaleType) => {
  switch (saleType) {
    case "ON_SALE":
      return "text-purple-500 border border-purple-500 rounded px-1 py-0.5 text-[12px] font-medium;";
    case "TO_BE_SALE":
      return "text-purple-500 border border-purple-500 bg-purple-50 rounded px-1 py-0.5 text-[12px] font-medium";
    case "END_OF_SALE":
      return "text-gray-500 border border-gray-500 rounded px-1 py-0.5 text-[12px] font-medium";
    default:
      return "text-gray-500 border border-gray-300 rounded px-1 py-0.5 text-[12px] font-medium";
  }
};

// SaleType 텍스트 변환 함수
const getSaleTypeText = (saleType: string) => {
  switch (saleType) {
    case "ON_SALE":
      return "판매중";
    case "TO_BE_SALE":
      return "판매예정";
    case "END_OF_SALE":
      return "판매종료";
    default:
      return "알 수 없음";
  }
};

const TicketList = ({ ticketList, onPageChange }: TicketListProps) => {
  const navigate = useNavigate();
  const allTickets = ticketList.results;

  return (
    <div>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-y-10 gap-x-7 ml-[70px] sm:ml-0">
        {allTickets.map((ticket) => (
          <div
            key={ticket.EventId}
            className="border rounded-lg overflow-hidden shadow-sm bg-white w-[220px] h-[420px] cursor-pointer"
            onClick={() => navigate(`/events/detail/${ticket.EventId}`)}
          >
            <img
              src={ticket.Poster_Url}
              alt={ticket.Title}
              className="w-full h-[300px] object-fill rounded-t-lg"
            />
            <div className="p-2 w-full">
              {/* 제목 */}
              <h3 className="text-md font-semibold text-black leading-tight mb-2">
                {ticket.Title}
              </h3>
              {/* 스테이지 */}
              <p className="text-sm text-gray-800 leading-tight mb-1">
                {ticket.Stage}
              </p>
              {/* 스케줄 */}
              <p className="text-sm text-gray-500 leading-tight mb-2">
                {formatSchedule(ticket.Schedules)}
              </p>
              {/* SaleType */}
              <span className={`${getSaleTypeStyle(ticket.SaleType)}`}>
                {getSaleTypeText(ticket.SaleType)}
              </span>
            </div>
          </div>
        ))}
      </div>

      {allTickets.length > 0 && allTickets.length < ticketList.totalSize ? (
        <button
          className="w-full sm:w-3/4 md:w-2/3 lg:w-1/2 mt-7 bg-gray-100 py-2 rounded hover:bg-gray-200 mb-10 text-sm sm:text-base md:text-lg lg:py-3 lg:text-lg mx-auto block"
          onClick={onPageChange}
        >
          티켓 더보기
        </button>
      ) : allTickets.length === 0 ? (
        <div className="w-full sm:w-3/4 md:w-2/3 lg:w-1/2 mt-4 text-center text-gray-500 text-sm sm:text-base mx-auto">
          해당 검색어 또는 필터와 일치하는 티켓이 없습니다.
        </div>
      ) : (
        <div className="w-full sm:w-3/4 md:w-2/3 lg:w-1/2 mt-4 text-center text-gray-500 mb-10 text-sm sm:text-base mx-auto">
          더이상 해당 티켓이 없습니다.
        </div>
      )}
    </div>
  );
};

export default TicketList;
