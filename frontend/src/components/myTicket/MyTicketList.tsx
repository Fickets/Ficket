import { useState, useEffect, useRef } from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import { EventContent } from "../../types/myTicket.ts";
import { fetchMyTickets, refundMyTicket } from "../../service/myTicket/api.ts";
import { useStore } from "zustand";
import { userStore } from "../../stores/UserStore.tsx";

const MyTicketList = () => {
  const [events, setEvents] = useState<EventContent[]>([]);
  const [page, setPage] = useState<number>(0);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");
  const [sidoFilter, setSidoFilter] = useState<string | null>(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState<boolean>(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const userName = useStore(userStore).userName;

  const loadMore = async () => {
    try {
      const response = await fetchMyTickets(page, sortOrder, sidoFilter);
      setEvents((prevEvents) => [...prevEvents, ...response.content]);
      setPage(response.page + 1);
      setHasMore(response.page + 1 < response.totalPages);
    } catch (error: any) {
      console.error("데이터 로드 실패:", error.message);
    }
  };

  const toggleSortOrder = () => {
    setSortOrder((prevOrder) => (prevOrder === "desc" ? "asc" : "desc"));
    setEvents([]);
    setPage(0);
    setHasMore(true);
  };

  const changeSidoFilter = (sido: string | null) => {
    setSidoFilter(sido);
    setEvents([]);
    setPage(0);
    setHasMore(true);
    setIsDropdownOpen(false);
  };

  const postRefundMyTicket = async (orderId: number) => {
    try {
      const response = await refundMyTicket(orderId);
      if (response == 204) {
        alert("환불 완료 되었습니다.");
      }
    } catch (error: any) {
      alert(error.message);
    }
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  useEffect(() => {
    loadMore();
  }, [sortOrder, sidoFilter]);

  return (
    <div className="container mx-auto px-4 lg:px-8 xl:px-20 mt-[50px]">
      {/* Title */}
      <div className="text-center">
        <h1 className="text-[40px] font-medium">마이티켓</h1>
      </div>
      {/* Divider */}
      <hr className="border-t-[1px] border-gray-300 my-[20px]" />
      {/* Filters */}
      <div className="flex justify-end space-x-4 mr-10">
        {/* Sort Order Button */}
        <button
          onClick={toggleSortOrder}
          className="flex items-center px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-100"
        >
          {sortOrder === "desc" ? "최신순" : "오래된순"}
          <span className="ml-2">▼</span>
        </button>
        {/* Sido Filter Dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            className="flex items-center px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-100"
          >
            {sidoFilter || "지역 전체"}
            <span className="ml-2">▼</span>
          </button>
          {isDropdownOpen && (
            <div className="absolute bg-white border mt-2 rounded-md shadow-md z-10">
              {[
                "지역 전체",
                "서울특별시",
                "인천광역시",
                "부산광역시",
                "대구광역시",
                "광주광역시",
                "대전광역시",
                "울산광역시",
              ].map((sido) => (
                <button
                  key={sido}
                  onClick={() =>
                    changeSidoFilter(sido === "지역 전체" ? null : sido)
                  }
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 w-full text-left"
                >
                  {sido}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
      <InfiniteScroll
        dataLength={events.length}
        next={loadMore}
        hasMore={hasMore}
        loader={<h4>로딩 중...</h4>}
        endMessage={
          <div className="flex justify-center items-center mt-6">
            <div className="bg-gray-100 p-4 rounded-md shadow-md">
              <p className="text-gray-600 text-sm font-medium">
                더 이상 데이터가 없습니다 🎉
              </p>
            </div>
          </div>
        }
      >
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 gap-y-12 mt-6 justify-items-center">
          {events.map((event, index) => (
            <div
              key={index}
              className="relative border rounded-md shadow-md overflow-hidden"
              style={{ width: "600px" }} // 카드 크기 고정
            >
              {/* Banner */}
              <img
                src={event.eventPcBannerUrl}
                alt={event.eventName}
                className="w-full h-[193px] object-cover"
              />
              {/* Overlay with Event Details */}
              <div className="absolute inset-0 flex flex-col justify-between text-white p-4 text-xs">
                <div>
                  <h2 className="text-lg font-bold">{event.eventName}</h2>
                  <p className="mt-1">
                    {new Date(event.eventDateTime).toLocaleString()}
                  </p>
                  <p className="mt-1">{event.eventStageName}</p>
                  <p className="mt-1">
                    총 {event.mySeatInfoList.length}명 -{" "}
                    {event.mySeatInfoList
                      .map(
                        (seat) =>
                          `${seat.seatGrade} ${seat.seatRow}${seat.seatCol}`,
                      )
                      .join(", ")}
                  </p>
                </div>
                <div className="flex justify-between items-end mt-4">
                  <p className="text-xs">{userName || "구매자 정보 없음"}</p>
                  <p className="text-xs font-bold">{event.companyName}</p>
                </div>
              </div>
              {/* Refund Button */}
              <button
                className="absolute top-4 right-4 text-red-500 text-xs font-bold hover:underline"
                onClick={() => postRefundMyTicket(event.orderId)}
              >
                환불하기
              </button>
            </div>
          ))}
        </div>
      </InfiniteScroll>
    </div>
  );
};

export default MyTicketList;
