import { useState, useEffect, useRef } from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import { EventContent } from "../../types/myTicket.ts";
import { fetchMyTickets, refundMyTicket } from "../../service/myTicket/api.ts";
import { useStore } from "zustand";
import { userStore } from "../../stores/UserStore.tsx";
import MobileHeader from "../@common/MobileHeader.tsx";

const MyTicketList = () => {
  // 티켓 데이터 관리하는 상태
  const [events, setEvents] = useState<EventContent[]>([]);
  // 현재 페이지 관리하는 상태
  const [page, setPage] = useState<number>(0);
  // 더 불러올 데이터가 있는지 여부
  const [hasMore, setHasMore] = useState<boolean>(true);
  // 정렬 순서 관리 (최신순, 오래된순)
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");
  // 지역 필터 관리
  const [sidoFilter, setSidoFilter] = useState<string | null>(null);
  // 드롭다운 열림 상태 관리
  const [isDropdownOpen, setIsDropdownOpen] = useState<boolean>(false);
  // 모바일 환경 여부 확인
  const [isMobile, setIsMobile] = useState<boolean>(
    window.matchMedia("(max-width: 640px)").matches,
  );
  // 카드 플립 상태 관리
  const [flipped, setFlipped] = useState<{ [key: number]: boolean }>({});
  // 드롭다운 참조용 Ref
  const dropdownRef = useRef<HTMLDivElement>(null);
  // 사용자 이름 가져오기
  const userName = useStore(userStore).userName;

  // 무한 스크롤로 데이터 로드
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

  // 정렬 순서 토글
  const toggleSortOrder = () => {
    setSortOrder((prevOrder) => (prevOrder === "desc" ? "asc" : "desc"));
    setEvents([]);
    setPage(0);
    setHasMore(true);
  };

  // 지역 필터 변경
  const changeSidoFilter = (sido: string | null) => {
    setSidoFilter(sido);
    setEvents([]);
    setPage(0);
    setHasMore(true);
    setIsDropdownOpen(false);
  };

  // 티켓 환불 처리
  const postRefundMyTicket = async (orderId: number) => {
    try {
      const response = await refundMyTicket(orderId);
      if (response === 204) {
        alert("환불 완료 되었습니다.");
      }
    } catch (error: any) {
      alert(error.message);
    }
  };

  // 화면 크기 변경 감지
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.matchMedia("(max-width: 640px)").matches);
    };

    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  // 드롭다운 외부 클릭 감지
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

  // 정렬 순서 또는 필터 변경 시 데이터 로드
  useEffect(() => {
    loadMore();
  }, [sortOrder, sidoFilter]);

  return (
    <div className="container mx-auto px-4 lg:px-8 xl:px-20 mt-[50px]">
      <div>
        {/* PC 헤더 */}
        {!isMobile && (
          <div className="text-center">
            <h1 className="text-[40px] font-medium">마이티켓</h1>
            <hr className="border-t-[1px] border-gray-300 my-[20px]" />
          </div>
        )}

        {/* 모바일 헤더 */}
        {isMobile && <MobileHeader title="마이티켓" />}
      </div>

      {/* 필터 UI */}
      <div className="flex justify-end space-x-4 mr-5">
        {/* 정렬 순서 버튼 */}
        <button
          onClick={toggleSortOrder}
          className="flex items-center px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-100"
        >
          {sortOrder === "desc" ? "최신순" : "오래된순"}
          <span className="ml-2">▼</span>
        </button>
        {/* 지역 필터 드롭다운 */}
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
      {/* 무한 스크롤 컴포넌트 */}
      <InfiniteScroll
        dataLength={events.length}
        next={loadMore}
        hasMore={hasMore}
        loader={<h4>로딩 중...</h4>}
        endMessage={
          <div className="flex justify-center items-center mt-6">
            <div>
              <p className="text-gray-600 text-sm font-medium">
                더 이상 구매 티켓이 없습니다 🎉
              </p>
            </div>
          </div>
        }
      >
        {/* 티켓 리스트 */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6 md:gap-8 mt-6 justify-items-center">
          {events.map((event, index) => {
            const isExpanded = flipped[index]; // 확장 상태 확인

            return (
              <div
                key={index}
                className={`relative rounded-md overflow-hidden shadow-md ${
                  isMobile ? "w-[280px]" : "w-full max-w-[600px]"
                }`}
              >
                {/* 카드 상단 (이미지) */}
                <div
                  className="cursor-pointer"
                  onClick={() => {
                    if (isMobile) {
                      // 모바일에서만 확장 상태를 토글
                      setFlipped((prev) => ({
                        ...prev,
                        [index]: !prev[index],
                      }));
                    }
                  }}
                >
                  <img
                    src={
                      isMobile
                        ? event.eventMobileBannerUrl
                        : event.eventPcBannerUrl
                    }
                    alt={event.eventName}
                    className={`w-full object-cover ${
                      isMobile
                        ? "h-[150px]"
                        : "sm:h-[180px] md:h-[220px] lg:h-[193px]"
                    }`}
                  />
                  {/* PC에서는 이미지 위에 티켓 정보 표시 */}
                  {!isMobile && (
                    <div className="absolute inset-0 flex flex-col justify-between p-4 text-white bg-gray-800 bg-opacity-50">
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
                        <p className="text-xs">
                          {userName || "구매자 정보 없음"}
                        </p>
                        <p className="text-xs font-bold">{event.companyName}</p>
                      </div>
                      <button
                        className="absolute top-4 right-4 text-red-500 text-xs font-bold hover:underline"
                        onClick={() => postRefundMyTicket(event.orderId)}
                      >
                        환불하기
                      </button>
                    </div>
                  )}
                </div>

                {/* 모바일에서 아래로 펼쳐지는 티켓 정보 */}
                {isMobile && (
                  <div
                    className={`transition-max-height duration-500 ease-in-out overflow-hidden ${
                      isExpanded ? "max-h-[300px]" : "max-h-0"
                    }`}
                    style={{
                      backgroundColor: "white", // 흰색 배경
                      padding: isExpanded ? "16px" : "0", // 확장 시 여백 추가
                    }}
                  >
                    {isExpanded && (
                      <div className="text-gray-800">
                        <h2 className="text-lg font-bold">{event.eventName}</h2>
                        <p className="mt-1">
                          날짜: {new Date(event.eventDateTime).toLocaleString()}
                        </p>
                        <p className="mt-1">장소: {event.eventStageName}</p>
                        <p className="mt-1">
                          좌석 정보:{" "}
                          {event.mySeatInfoList
                            .map(
                              (seat) =>
                                `${seat.seatGrade} ${seat.seatRow}${seat.seatCol}`,
                            )
                            .join(", ")}
                        </p>
                        <p className="mt-4 text-xs">
                          구매자: {userName || "구매자 정보 없음"}
                        </p>
                        <p className="mt-1 text-xs font-bold">
                          {event.companyName}
                        </p>
                        <button
                          className="mt-4 text-red-500 text-xs font-bold hover:underline"
                          onClick={() => postRefundMyTicket(event.orderId)}
                        >
                          환불하기
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </InfiniteScroll>
    </div>
  );
};

export default MyTicketList;
