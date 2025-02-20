import {
  EventScheduledOpenResponse,
  PageDTO,
  SearchParams,
} from "../../types/eventScheduledOpen.ts";
import { useState, useEffect } from "react";
import { Genre } from "../../types/ReservationRateRanking.ts";
import InfiniteScroll from "react-infinite-scroll-component";
import { useNavigate } from "react-router-dom";
import { searchEventScheduledOpen } from "../../service/eventScheduledOpen/api.ts";
import { format } from "date-fns";
import { ko } from "date-fns/locale";

const MobileEventOpenList = () => {
  const navigate = useNavigate();
  const [isGenreDropdownOpen, setGenreDropdownOpen] = useState(false);

  const [searchParams, setSearchParams] = useState<SearchParams>({
    searchValue: null,
    genre: null,
    page: 0,
    size: 15,
    sort: "createdAt,DESC",
  });

  const [response, setResponse] =
    useState<PageDTO<EventScheduledOpenResponse> | null>(null);

  // 새로운 데이터를 가져오는 로직
  const fetchEvents = async (params: SearchParams) => {
    try {
      const fetchedResponse = await searchEventScheduledOpen(params);

      setResponse((prev) => {
        if (!prev || params.page === 0) {
          // 첫 번째 페이지거나 초기화가 필요한 경우
          return fetchedResponse;
        }

        // 기존 데이터와 병합
        return {
          ...fetchedResponse,
          content: [...(prev.content || []), ...fetchedResponse.content],
        };
      });
    } catch (error) {
      console.error("Error fetching events:", error);
    }
  };

  // searchParams 변경 시 데이터 가져오기
  useEffect(() => {
    fetchEvents(searchParams);
  }, [searchParams]);

  const fetchMoreEvents = async (): Promise<void> => {
    if (response?.last) return; // 마지막 페이지일 경우 중단

    const nextPage = (searchParams.page ?? 0) + 1;

    // 페이지 번호를 증가시켜 데이터 요청
    setSearchParams((prev) => ({
      ...prev,
      page: nextPage,
    }));
  };

  const handleSearchParamsChange = (newParams: Partial<SearchParams>): void => {
    setSearchParams((prev) => ({
      ...prev,
      ...newParams,
      page: 0, // 새로운 검색 시 페이지를 초기화
    }));
  };

  const handleGenreSelect = (genre: string | [string, string]) => {
    setGenreDropdownOpen(false);

    if (genre === "전체") {
      handleSearchParamsChange({ genre: null });
    } else if (Array.isArray(genre)) {
      const [key] = genre;
      handleSearchParamsChange({ genre: key as Genre });
    }
  };

  const handleSortChange = (newSort: string) => {
    handleSearchParamsChange({ sort: newSort });
  };

  const handleEventClick = (eventId: number) => {
    navigate(`/events/detail/${eventId}`);
  };

  return (
    <div className="px-4 py-2 mt-14 min-h-[calc(100vh-100px)]">
      <div className="flex justify-between items-center mb-4 border-b pb-2">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => handleSortChange("createdAt,DESC")}
            className={`text-sm flex items-center ${
              searchParams.sort === "createdAt,DESC"
                ? "text-black font-bold"
                : "text-gray-700"
            }`}
          >
            등록일순 <span className="ml-1">›</span>
          </button>
          <button
            onClick={() => handleSortChange("ticketingTime,DESC")}
            className={`text-sm flex items-center ${
              searchParams.sort === "ticketingTime,DESC"
                ? "text-black font-bold"
                : "text-gray-700"
            }`}
          >
            오픈일순 <span className="ml-1">›</span>
          </button>
        </div>

        <div className="relative">
          <button
            onClick={() => setGenreDropdownOpen((prev) => !prev)}
            className="text-sm text-gray-700 flex items-center"
          >
            {searchParams.genre ? `${searchParams.genre}` : "전체"}
            <span className="ml-1">▾</span>
          </button>
          {isGenreDropdownOpen && (
            <div className="absolute right-0 mt-2 bg-white border border-gray-300 rounded shadow-lg z-10 w-56">
              <div className="grid grid-cols-2 divide-x divide-gray-300 text-sm text-gray-700">
                <button
                  onClick={() => handleGenreSelect("전체")}
                  className={`px-4 py-2 text-left ${
                    !searchParams.genre ? "text-red-500 font-bold" : ""
                  }`}
                >
                  전체
                </button>
                <button
                  onClick={() => handleGenreSelect(["뮤지컬", "뮤지컬"])}
                  className="px-4 py-2 text-left hover:bg-gray-100"
                >
                  뮤지컬
                </button>
                <button
                  onClick={() => handleGenreSelect(["콘서트", "콘서트"])}
                  className="px-4 py-2 text-left hover:bg-gray-100"
                >
                  콘서트
                </button>
                <button
                  onClick={() => handleGenreSelect(["스포츠", "스포츠"])}
                  className="px-4 py-2 text-left hover:bg-gray-100"
                >
                  스포츠
                </button>
                <button
                  onClick={() => handleGenreSelect(["전시_행사", "전시/행사"])}
                  className="px-4 py-2 text-left hover:bg-gray-100"
                >
                  전시/행사
                </button>
                <button
                  onClick={() =>
                    handleGenreSelect(["클래식_무용", "클래식/무용"])
                  }
                  className="px-4 py-2 text-left hover:bg-gray-100"
                >
                  클래식/무용
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      <InfiniteScroll
        dataLength={response?.content.length || 0}
        next={fetchMoreEvents}
        hasMore={!response?.last}
        loader={<h4>Loading...</h4>}
        endMessage={
          <p className="text-center text-gray-500">
            더 이상 이벤트가 없습니다.
          </p>
        }
      >
        <div className="grid grid-cols-1 gap-4">
          {response?.content.map((event) => (
            <div
              key={event.eventId}
              className="border p-4 rounded shadow cursor-pointer hover:shadow-md transition-shadow"
              onClick={() => handleEventClick(event.eventId)}
            >
              <div className="flex justify-between items-center">
                <div className="flex-1">
                  <h4 className="text-lg font-bold">
                    {event.title}
                    {event.newPostEvent && (
                      <span className="ml-1 px-1.5 py-0.5 text-[10px] font-medium text-white bg-gradient-to-r from-red-500 to-pink-500 rounded-full shadow-sm">
                        N
                      </span>
                    )}
                  </h4>
                  {event.genreList && (
                    <p className="text-sm text-gray-500 mt-1">
                      {event.genreList
                        .map((genre) => genre.replace(/_/g, "/"))
                        .join(", ")}
                    </p>
                  )}
                </div>
                <div className="ml-4 w-24 h-30">
                  <img
                    src={event.mobilePosterUrl}
                    alt={event.title}
                    className="w-full h-full rounded border border-gray-300 object-cover"
                  />
                </div>
              </div>
              <div className="border-t mt-4 pt-2 text-xs flex justify-between">
                <span className="text-gray-500">티켓 오픈</span>
                <span className="text-red-500">
                  {format(
                    new Date(event.ticketStartTime),
                    "yyyy.MM.dd일(E) HH:mm",
                    { locale: ko },
                  )}
                </span>
              </div>
            </div>
          ))}
        </div>
      </InfiniteScroll>
    </div>
  );
};

export default MobileEventOpenList;
