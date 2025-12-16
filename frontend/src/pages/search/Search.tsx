import UserHeader from "../../components/@common/UserHeader.tsx";
import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { SearchAfterResult, SearchParams, SortBy } from "../../types/search.ts";
import FilterSection from "../../components/search/FilterSection.tsx";
import TicketList from "../../components/search/TicketList.tsx";
import { searchByFilter } from "../../service/search/api.ts";
import { useMediaQuery } from "react-responsive";
import { FaFilter } from "react-icons/fa";
import Logo from "../../assets/logo.png";
import SearchBar from "../../components/@common/SearchBar.tsx";
import MobileBottom from "../../components/@common/MobileBottom.tsx";
import { Helmet } from "react-helmet-async";

const Search = () => {
  const isMobile: boolean = useMediaQuery({ query: "(max-width: 768px)" });
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get("keyword") || "";
  useEffect(() => {
    window.scrollTo(0, 0); // 페이지 이동 후 스크롤을 맨 위로
  }, []);
  // 티켓 데이터 상태
  const [tickets, setTickets] = useState<SearchAfterResult>({
    totalSize: 0,
    events: [],
    nextSearchAfter: undefined,
  });

  // 필터 상태
  const [filters, setFilters] = useState<Partial<SearchParams>>({
    genreList: undefined,
    locationList: undefined,
    startDate: undefined,
    endDate: undefined,
    saleTypeList: undefined,
    sortBy: SortBy.SORT_BY_ACCURACY,
    pageSize: 20,
    searchAfter: undefined, // ⭐ 핵심
  });

  // 필터 변경 핸들러
  const handleFilterChange = (newFilters: Partial<SearchParams>) => {
    setFilters((prev) => ({
      ...prev,
      ...newFilters,
      searchAfter: undefined,
    }));
  };

  // 페이지 변경 핸들러
  const handlePageChange = () => {
    if (!tickets.nextSearchAfter) return;

    setFilters((prev) => ({
      ...prev,
      searchAfter: tickets.nextSearchAfter,
    }));
  };

  const [isFilterOpen, setIsFilterOpen] = useState(false);

  // 팝업 열기/닫기 핸들러
  const toggleFilterSection = () => {
    setIsFilterOpen((prev) => !prev);
  };

  useEffect(() => {
    const fetchFilteredTickets = async () => {
      try {
        const params: SearchParams = {
          ...filters,
          title: keyword.toLowerCase(),
        };

        const response = await searchByFilter(params);

        if (!filters.searchAfter) {
          // 첫 페이지
          setTickets(response);
        } else {
          // 다음 페이지
          setTickets((prev) => ({
            totalSize: response.totalSize,
            events: [...prev.events, ...response.events],
            nextSearchAfter: response.nextSearchAfter,
          }));
        }
      } catch (error) {
        console.error("티켓 데이터 가져오기 실패:", error);
      }
    };

    if (keyword.trim()) {
      fetchFilteredTickets();
    }
  }, [filters, keyword]);

  const navigate = useNavigate();

  return (
    <div>
      <Helmet>
        <title>{keyword} - Ficket Search</title>
      </Helmet>
      {isMobile ? (
        <div className="bg-gray-50 min-h-screen">
          {/* 헤더 */}
          <div className="bg-white shadow-md">
            <div className="flex items-center justify-between px-4 py-3 max-w-4xl mx-auto">
              {/* 로고 섹션 */}
              <div
                onClick={() => navigate("/")}
                className="flex items-center cursor-pointer"
              >
                <img src={Logo} alt="Logo" className="w-[30px] h-[30px]" />
                <p className="text-[15px] font-semibold ml-[10px] text-gray-800">
                  Ficket
                </p>
              </div>
              {/* 검색창 */}
              <div className="relative w-full max-w-[230px]">
                <SearchBar />
              </div>
            </div>
          </div>

          {/* 콘텐츠 영역 */}
          <div className="relative">
            {/* 버튼 */}
            <button
              className="bg-gray-100 p-2 rounded-full shadow hover:bg-gray-200 flex items-center justify-center absolute top-3 right-4"
              onClick={toggleFilterSection}
            >
              <FaFilter className="text-gray-600 text-lg" />
            </button>

            {/* FilterSection 팝업 */}
            <div
              className={`fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center z-50 transition-opacity ${
                isFilterOpen ? "opacity-100 visible" : "opacity-0 invisible"
              }`}
            >
              <div className="bg-white rounded-lg shadow-lg p-6 w-[85%] max-w-md h-[80%] overflow-y-auto relative">
                {/* 닫기 버튼 */}
                <button
                  className="absolute top-7 right-14 text-gray-500 hover:text-gray-700 mt-2"
                  onClick={toggleFilterSection}
                >
                  &#10005;
                </button>
                {/* FilterSection 컴포넌트 */}
                <FilterSection onFilterChange={handleFilterChange} />
              </div>
            </div>
          </div>

          <div className="mt-7">
            <TicketList
              ticketList={tickets}
              hasNext={!!tickets.nextSearchAfter}
              onPageChange={handlePageChange}
            />
          </div>

          {/* 모바일 하단 메뉴 */}
          <div className="mb-20">
            <MobileBottom />
          </div>
        </div>
      ) : (
        <div className="mt-6">
          <UserHeader />
          <div className="mx-auto max-w-screen-xl">
            <div className="flex flex-col lg:flex-row">
              {/* Filter Section */}
              <aside className="w-full lg:w-1/4 lg:pr-6 mb-6 lg:mb-0">
                <FilterSection onFilterChange={handleFilterChange} />
              </aside>

              {/* Ticket Section */}
              <main className="w-full lg:w-3/4">
                {/* Header and Sort Buttons */}
                <div className="flex items-center justify-between mb-4">
                  <h1 className="text-2xl font-bold">
                    티켓 ({tickets.totalSize})
                  </h1>
                  <div className="flex space-x-6">
                    <button
                      className={`${
                        filters.sortBy === SortBy.SORT_BY_ACCURACY
                          ? "text-purple-500 font-bold border-b-2 border-purple-500"
                          : "text-gray-500 hover:text-black"
                      }`}
                      onClick={() =>
                        handleFilterChange({ sortBy: SortBy.SORT_BY_ACCURACY })
                      }
                    >
                      정확도순
                    </button>
                    <button
                      className={`${
                        filters.sortBy === SortBy.SORT_BY_PERFORMANCE_IMMINENT
                          ? "text-purple-500 font-bold border-b-2 border-purple-500"
                          : "text-gray-500 hover:text-black"
                      }`}
                      onClick={() =>
                        handleFilterChange({
                          sortBy: SortBy.SORT_BY_PERFORMANCE_IMMINENT,
                        })
                      }
                    >
                      공연임박순
                    </button>
                  </div>
                </div>

                <div className="border-b border-gray-300 mb-6"></div>

                <TicketList
                  ticketList={tickets}
                  hasNext={!!tickets.nextSearchAfter}
                  onPageChange={handlePageChange}
                />
              </main>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Search;
