import {
  FaList,
  FaAngleDoubleLeft,
  FaAngleLeft,
  FaAngleRight,
  FaAngleDoubleRight,
} from "react-icons/fa";
import { useMemo } from "react";
import { useTable, usePagination, Column, TableOptions } from "react-table";
import { useNavigate } from "react-router-dom";
import { Event, EventListProps } from "../../types/eventList";

const EventList = ({ data, onPageChange }: EventListProps) => {
  const navigate = useNavigate();

  const columns: Column<Event>[] = useMemo(
    () => [
      {
        Header: "ID",
        accessor: "eventId",
      },
      {
        Header: "공연 제목",
        accessor: "eventTitle",
      },
      {
        Header: "공연장",
        accessor: "stageName",
      },
      {
        Header: "회사",
        accessor: "companyName",
      },
      {
        Header: "관리자",
        accessor: "adminName",
      },
      {
        Header: "공연 날짜",
        accessor: "eventDates",
        Cell: ({ value }) => {
          if (!value || value.length === 0) return null;

          const uniqueDates = Array.from(
            new Set(
              value.map((date) => {
                const localDate = new Date(date);
                return new Date(
                  localDate.getFullYear(),
                  localDate.getMonth(),
                  localDate.getDate(),
                ).toISOString();
              }),
            ),
          )
            .sort()
            .map((dateString) => new Date(dateString));

          const formattedDates = [];
          let start = uniqueDates[0];
          let end = uniqueDates[0];

          for (let i = 1; i <= uniqueDates.length; i++) {
            if (
              i === uniqueDates.length ||
              new Date(uniqueDates[i]).getTime() !==
                new Date(end.getTime() + 24 * 60 * 60 * 1000).getTime()
            ) {
              if (start.getTime() === end.getTime()) {
                formattedDates.push(start.toLocaleDateString("ko-KR"));
              } else {
                formattedDates.push(
                  `${start.toLocaleDateString(
                    "ko-KR",
                  )} ~ ${end.toLocaleDateString("ko-KR")}`,
                );
              }
              start = uniqueDates[i];
              end = uniqueDates[i];
            } else {
              end = uniqueDates[i];
            }
          }

          return formattedDates.map((dateRange, index) => (
            <span key={index} className="block">
              {dateRange}
            </span>
          ));
        },
      },
    ],
    [], // 의존성 배열: 빈 배열로 설정하여 한 번만 생성
  );

  const tableOptions: TableOptions<Event> = {
    columns,
    data: data.content,
    manualPagination: true,
    pageCount: data.totalPages,
    initialState: { pageIndex: data.page, pageSize: data.size },
  };

  const { getTableProps, getTableBodyProps, headerGroups, rows, prepareRow } =
    useTable<Event>(tableOptions, usePagination);

  const renderPageControls = () => {
    return (
      <div className="flex justify-center items-center space-x-2 mt-4">
        {/* 처음 페이지로 */}
        <button
          onClick={() => onPageChange(0)}
          disabled={data.page === 0}
          className="px-3 py-2 rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 flex items-center"
        >
          <FaAngleDoubleLeft />
        </button>

        {/* 이전 페이지로 */}
        <button
          onClick={() => onPageChange(data.page - 1)}
          disabled={data.page === 0}
          className="px-3 py-2 rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 flex items-center"
        >
          <FaAngleLeft />
        </button>

        {/* 페이지 번호 */}
        {Array.from({ length: data.totalPages }, (_, i) => (
          <button
            key={i}
            onClick={() => onPageChange(i)}
            className={`px-3 py-2 rounded-md text-sm ${
              data.page === i
                ? "bg-blue-500 text-white"
                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
            }`}
          >
            {i + 1}
          </button>
        ))}

        {/* 다음 페이지로 */}
        <button
          onClick={() => onPageChange(data.page + 1)}
          disabled={data.page === data.totalPages - 1}
          className="px-3 py-2 rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 flex items-center"
        >
          <FaAngleRight />
        </button>

        {/* 마지막 페이지로 */}
        <button
          onClick={() => onPageChange(data.totalPages - 1)}
          disabled={data.page === data.totalPages - 1}
          className="px-3 py-2 rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 flex items-center"
        >
          <FaAngleDoubleRight />
        </button>
      </div>
    );
  };

  return (
    <div className="w-full bg-white rounded-lg shadow-md p-6 border border-gray-200">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center">
          <FaList className="text-xl" />
          <span className="ml-2 text-lg font-semibold">공연 목록</span>
        </div>
        <button
          type="button"
          className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 text-sm"
          onClick={() => navigate("/admin/register-event")}
        >
          신규
        </button>
      </div>
      <hr className="mb-6 border-gray-300" />

      <table
        {...getTableProps()}
        className="table-auto w-full border-collapse border border-gray-300"
      >
        <thead className="bg-gray-100">
          {headerGroups.map((headerGroup) => (
            <tr {...headerGroup.getHeaderGroupProps()}>
              {headerGroup.headers.map((column) => (
                <th
                  {...column.getHeaderProps()}
                  className="border border-gray-300 px-4 py-2 text-left text-sm font-medium text-gray-700"
                >
                  {column.render("Header")}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody {...getTableBodyProps()}>
          {rows.map((row) => {
            prepareRow(row);
            return (
              <tr
                {...row.getRowProps()}
                className="odd:bg-white even:bg-gray-50 cursor-pointer hover:bg-blue-50 hover:text-blue-700"
                onClick={() =>
                  navigate(`/admin/event-detail/${row.original.eventId}`)
                }
              >
                {row.cells.map((cell) => (
                  <td
                    {...cell.getCellProps()}
                    className="border border-gray-300 px-4 py-2 text-sm text-gray-800"
                  >
                    {cell.render("Cell")}
                  </td>
                ))}
              </tr>
            );
          })}
        </tbody>
      </table>

      {renderPageControls()}
    </div>
  );
};

export default EventList;
