import UserHeader from "../../components/@common/UserHeader.tsx";
import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import {
  SearchResult,
  SearchParams,
  SortBy,
  SaleType,
} from "../../types/search.ts";
import FilterSection from "../../components/search/FilterSection.tsx";
import TicketList from "../../components/search/TicketList.tsx";
import { searchByFilter } from "../../service/search/api.ts";
import { useMediaQuery } from "react-responsive";

const Search = () => {
  const isMobile: boolean = useMediaQuery({ query: "(max-width: 768px)" });
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get("keyword") || "";

  // 티켓 데이터 상태
  const [tickets, setTickets] = useState<SearchResult>({
    totalSize: 0,
    totalPages: 0,
    results: [],
  });

  // 필터 상태
  const [filters, setFilters] = useState<Partial<SearchParams>>({
    genreList: undefined,
    locationList: undefined,
    startDate: undefined,
    endDate: undefined,
    saleTypeList: [SaleType.ON_SALE, SaleType.TO_BE_SALE],
    sortBy: SortBy.SORT_BY_ACCURACY,
    pageNumber: 1,
    pageSize: 20,
  });

  // 필터 변경 핸들러
  const handleFilterChange = (newFilters: Partial<SearchParams>) => {
    setFilters((prev) => ({
      ...prev,
      ...newFilters,
      pageNumber: 1, // 필터 변경 시 페이지 초기화
    }));
  };

  // 페이지 변경 핸들러
  const handlePageChange = () => {
    setFilters((prev) => ({
      ...prev,
      pageNumber: (prev.pageNumber || 1) + 1, // 다음 페이지로 이동
    }));
  };

  useEffect(() => {
    const fetchFilteredTickets = async () => {
      try {
        const searchParams: SearchParams = {
          ...filters,
          title: keyword,
        };
        const response = await searchByFilter(searchParams);

        if (filters.pageNumber === 1) {
          // 첫 페이지일 경우 초기화
          setTickets(response);
        } else {
          // 다음 페이지일 경우 results 추가
          setTickets((prev) => ({
            ...response,
            results: [...prev.results, ...response.results],
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

  return (
    <div>
      {isMobile ? (
        <h2>ef</h2>
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

                {/* Ticket List */}
                <TicketList
                  ticketList={tickets}
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
