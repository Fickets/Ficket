import {
  EventScheduledOpenResponse,
  SearchParams,
  PageDTO,
} from "../../types/eventScheduledOpen.ts";
import {
  FaSearch,
  FaAngleDoubleLeft,
  FaAngleLeft,
  FaAngleRight,
  FaAngleDoubleRight,
} from "react-icons/fa";
import { useEffect, useMemo, useState } from "react";
import {
  useReactTable,
  getCoreRowModel,
  getPaginationRowModel,
  flexRender,
  ColumnDef,
} from "@tanstack/react-table";
import { useNavigate } from "react-router-dom";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { Genre } from "../../types/ReservationRateRanking.ts";
import { searchEventScheduledOpen } from "../../service/eventScheduledOpen/api.ts";

const PcEventOpenList = () => {
  const navigate = useNavigate();
  const [inputValue, setInputValue] = useState<string>("");
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

  const handleSearchParamsChange = (newParams: Partial<SearchParams>) => {
    setSearchParams((prev) => ({
      ...prev,
      ...newParams,
      page: newParams.page !== undefined ? newParams.page : 0, // Reset page on search parameter change
    }));
  };

  const fetchEventScheduledOpen = async (
    searchCond: SearchParams,
  ): Promise<void> => {
    try {
      const data: PageDTO<EventScheduledOpenResponse> =
        await searchEventScheduledOpen(searchCond);
      setResponse(data);
    } catch (error) {
      console.error("Error fetching events:", error);
    }
  };

  const genres = ["전체", ...Object.entries(Genre)];

  const handleGenreSelect = (genre: string | [string, string]) => {
    setGenreDropdownOpen(false);

    if (genre === "전체") {
      handleSearchParamsChange({ genre: null, page: 0 });
    } else if (Array.isArray(genre)) {
      const [key] = genre;
      handleSearchParamsChange({ genre: key as Genre, page: 0 });
    }
  };

  const columns: ColumnDef<EventScheduledOpenResponse>[] = useMemo(
    () => [
      {
        header: "NO",
        cell: ({ row }) =>
          row.index +
          1 +
          (response?.pageNumber || 0) * (response?.pageSize || 0),
      },
      {
        header: () => (
          <div className="relative">
            <button
              onClick={() => setGenreDropdownOpen((prev) => !prev)}
              className="hover:underline"
            >
              장르
            </button>
            {isGenreDropdownOpen && (
              <div className="absolute z-10 bg-white border border-gray-300 rounded shadow-lg mt-2">
                {genres.map((genre) => {
                  if (genre === "전체") {
                    return (
                      <button
                        key="전체"
                        onClick={() => handleGenreSelect(genre)}
                        className="block px-4 py-2 text-left text-sm hover:bg-gray-100 w-full"
                      >
                        전체항목
                      </button>
                    );
                  } else {
                    const [key, value] = genre;
                    return (
                      <button
                        key={key}
                        onClick={() => handleGenreSelect(genre)}
                        className="block px-4 py-2 text-left text-sm hover:bg-gray-100 w-full"
                      >
                        {value}
                      </button>
                    );
                  }
                })}
              </div>
            )}
          </div>
        ),
        accessorKey: "genreList",
        cell: ({ getValue }) => {
          const genres = getValue<string[]>();
          return (
            genres?.map((genre) => genre.replace(/_/g, "/")).join(", ") || ""
          );
        },
      },
      {
        header: "제목",
        accessorKey: "title",
        cell: ({ row }) => (
          <div className="flex items-center">
            <a
              href="#"
              onClick={(e) => {
                e.preventDefault();
                navigate(`/events/detail/${row.original.eventId}`);
              }}
              className="text-black-600 hover:underline"
            >
              {row.original.title}
            </a>
            {row.original.newPostEvent && (
              <span className="ml-2 px-1 py-0.1 text-[10px] font-medium text-white bg-gradient-to-r from-red-500 to-pink-500 rounded-full shadow-sm">
                N
              </span>
            )}
          </div>
        ),
      },
      {
        header: "티켓오픈일시",
        accessorKey: "ticketStartTime",
        cell: ({ getValue }) => {
          const date = getValue<string>();
          return date
            ? format(new Date(date), "yyyy.MM.dd일(E) HH:mm", {
                locale: ko,
              })
            : "";
        },
      },
    ],
    [
      navigate,
      response?.pageNumber,
      response?.pageSize,
      genres,
      isGenreDropdownOpen,
    ],
  );

  const table = useReactTable({
    data: response?.content || [],
    columns,
    state: {
      pagination: {
        pageIndex: searchParams.page || 0,
        pageSize: searchParams.size || 10,
      },
    },
    pageCount: response?.totalPages || 0,
    manualPagination: true,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
  });

  const onPageChange = (newPage: number) => {
    handleSearchParamsChange({ page: newPage });
  };

  const handleSortChange = (newSort: string) => {
    handleSearchParamsChange({ sort: newSort, page: 0 });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
  };

  const handleInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleClickSearch();
    }
  };

  const handleClickSearch = () => {
    if (inputValue.trim() === "") {
      alert("검색어를 입력해주세요");
      return;
    }
    handleSearchParamsChange({ searchValue: inputValue.trim(), page: 0 });
  };

  const renderPageControls = () => {
    const { pageIndex } = table.getState().pagination;

    return (
      <div className="flex justify-center items-center space-x-2 mt-4 text-sm text-gray-700">
        <button
          onClick={() => onPageChange(0)}
          disabled={pageIndex === 0}
          className="px-2 py-1 rounded disabled:opacity-50"
        >
          <FaAngleDoubleLeft />
        </button>
        <button
          onClick={() => onPageChange(pageIndex - 1)}
          disabled={pageIndex === 0}
          className="px-2 py-1 rounded disabled:opacity-50"
        >
          <FaAngleLeft />
        </button>
        {Array.from({ length: response?.totalPages || 0 }, (_, i) => (
          <button
            key={i}
            onClick={() => onPageChange(i)}
            className={`px-2 py-1 rounded ${
              pageIndex === i ? "bg-gray-300 font-bold" : "hover:bg-gray-200"
            }`}
          >
            {i + 1}
          </button>
        ))}
        <button
          onClick={() => onPageChange(pageIndex + 1)}
          disabled={pageIndex === (response?.totalPages || 0) - 1}
          className="px-2 py-1 rounded disabled:opacity-50"
        >
          <FaAngleRight />
        </button>
        <button
          onClick={() => onPageChange((response?.totalPages || 0) - 1)}
          disabled={pageIndex === (response?.totalPages || 0) - 1}
          className="px-2 py-1 rounded disabled:opacity-50"
        >
          <FaAngleDoubleRight />
        </button>
      </div>
    );
  };

  useEffect(() => {
    fetchEventScheduledOpen(searchParams);
  }, [searchParams]);

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex justify-between items-center bg-[#2E5072] text-white px-4 h-14">
        <h2 className="font-bold text-lg">오픈 티켓</h2>
        <div className="flex space-x-20">
          <div className="space-x-4">
            <button
              onClick={() => handleSortChange("createdAt,DESC")}
              className={`h-full px-3 text-white ${
                searchParams.sort === "createdAt,DESC"
                  ? "bg-[#183D63]"
                  : "bg-[#2E5072]"
              }`}
            >
              등록순
            </button>
            <button
              onClick={() => handleSortChange("ticketingTime,DESC")}
              className={`h-full px-3 text-white ${
                searchParams.sort === "ticketingTime,DESC"
                  ? "bg-[#183D63]"
                  : "bg-[#2E5072]"
              }`}
            >
              오픈일순
            </button>
          </div>
          <div className="flex border border-gray-500 rounded-lg overflow-hidden">
            <input
              type="text"
              value={inputValue}
              onChange={handleInputChange}
              onKeyDown={handleInputKeyDown}
              className="px-4 py-2 w-[130px] bg-[#183D63] focus:outline-none"
            />
            <button
              className="px-4 py-2 bg-[#183D63]"
              onClick={handleClickSearch}
            >
              <FaSearch className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <div>
        <table className="w-full">
          <thead className="bg-[#EBEBEB] border-b border-gray-500">
            {table.getHeaderGroups().map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <th
                    key={header.id}
                    className="px-4 py-2 text-left text-sm font-medium text-black"
                  >
                    {flexRender(
                      header.column.columnDef.header,
                      header.getContext(),
                    )}
                  </th>
                ))}
              </tr>
            ))}
          </thead>
          <tbody>
            {table.getRowModel().rows.length > 0 ? (
              table.getRowModel().rows.map((row) => (
                <tr key={row.id}>
                  {row.getVisibleCells().map((cell, index) => (
                    <td
                      key={cell.id}
                      className={`px-4 py-2 text-sm text-gray-800 border-b border-gray-300 ${
                        index === 0
                          ? "w-1/12"
                          : index === 1
                            ? "w-3/12"
                            : index === 2
                              ? "w-6/12"
                              : "w-auto"
                      }`}
                    >
                      {flexRender(
                        cell.column.columnDef.cell,
                        cell.getContext(),
                      )}
                    </td>
                  ))}
                </tr>
              ))
            ) : (
              <tr>
                <td
                  colSpan={columns.length}
                  className="text-center text-gray-600 py-4"
                >
                  검색 결과가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        <div className="flex items-center justify-between mt-4">
          <div className="flex justify-center flex-grow">
            {renderPageControls()}
          </div>
          <span className="text-sm text-gray-700">
            총 {response?.totalPages || 0} 페이지
          </span>
        </div>
      </div>
    </div>
  );
};

export default PcEventOpenList;
