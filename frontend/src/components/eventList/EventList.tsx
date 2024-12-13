import {
  FaList,
  FaAngleDoubleLeft,
  FaAngleLeft,
  FaAngleRight,
  FaAngleDoubleRight,
} from 'react-icons/fa';
import { useMemo } from 'react';
import {
  useReactTable,
  getCoreRowModel,
  getPaginationRowModel,
  flexRender,
  ColumnDef,
} from '@tanstack/react-table';
import { useNavigate } from 'react-router-dom';
import { Event, EventListProps } from '../../types/eventList';

const EventList: React.FC<EventListProps> = ({ data, onPageChange }) => {
  const navigate = useNavigate();

  const columns: ColumnDef<Event>[] = useMemo(
    () => [
      {
        header: 'NO',
        cell: ({ row }) => row.index + 1 + data.page * data.size,
      },
      {
        header: '공연 제목',
        accessorKey: 'eventTitle',
      },
      {
        header: '공연장',
        accessorKey: 'stageName',
      },
      {
        header: '회사',
        accessorKey: 'companyName',
      },
      {
        header: '관리자',
        accessorKey: 'adminName',
      },
      {
        header: '공연 날짜',
        accessorKey: 'eventDates',
        cell: ({ getValue }) => {
          const value = getValue() as string[]; // 반환값을 string[]로 타입 단언
          if (!Array.isArray(value) || value.length === 0) return null;

          const uniqueDates = Array.from(
            new Set(
              value.map((date) => {
                const localDate = new Date(date);
                return new Date(
                  localDate.getFullYear(),
                  localDate.getMonth(),
                  localDate.getDate()
                ).toISOString();
              })
            )
          )
            .sort()
            .map((dateString) => new Date(dateString));

          const formattedDates: string[] = [];
          let start = uniqueDates[0];
          let end = uniqueDates[0];

          for (let i = 1; i <= uniqueDates.length; i++) {
            if (
              i === uniqueDates.length ||
              new Date(uniqueDates[i]).getTime() !==
                new Date(end.getTime() + 24 * 60 * 60 * 1000).getTime()
            ) {
              if (start.getTime() === end.getTime()) {
                formattedDates.push(start.toLocaleDateString('ko-KR'));
              } else {
                formattedDates.push(
                  `${start.toLocaleDateString(
                    'ko-KR'
                  )} ~ ${end.toLocaleDateString('ko-KR')}`
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
    [data.page, data.size]
  );

  const table = useReactTable({
    data: data.content,
    columns,
    state: {
      pagination: {
        pageIndex: data.page || 0,
        pageSize: data.size || 10,
      },
    },
    pageCount: data.totalPages, // 전체 페이지 수를 전달
    manualPagination: true, // 페이지네이션을 수동으로 처리
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
  });

  const renderPageControls = () => {
    const { pageIndex } = table.getState().pagination;

    return (
      <div className="flex justify-center items-center space-x-2 mt-4">
        {/* 처음 페이지로 */}
        <button
          onClick={() => onPageChange(0)}
          disabled={pageIndex === 0}
          className="px-3 py-2 rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 flex items-center"
        >
          <FaAngleDoubleLeft />
        </button>

        {/* 이전 페이지로 */}
        <button
          onClick={() => onPageChange(pageIndex - 1)}
          disabled={pageIndex === 0}
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
              pageIndex === i
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            {i + 1}
          </button>
        ))}

        {/* 다음 페이지로 */}
        <button
          onClick={() => onPageChange(pageIndex + 1)}
          disabled={pageIndex === data.totalPages - 1}
          className="px-3 py-2 rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 flex items-center"
        >
          <FaAngleRight />
        </button>

        {/* 마지막 페이지로 */}
        <button
          onClick={() => onPageChange(data.totalPages - 1)}
          disabled={pageIndex === data.totalPages - 1}
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
          onClick={() => navigate('/admin/register-event')}
        >
          신규
        </button>
      </div>
      <hr className="mb-6 border-gray-300" />

      <table className="table-auto w-full border-collapse border border-gray-300">
        <thead className="bg-gray-100">
          {table.getHeaderGroups().map((headerGroup) => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map((header) => (
                <th
                  key={header.id}
                  className="border border-gray-300 px-4 py-2 text-left text-sm font-medium text-gray-700"
                >
                  {flexRender(
                    header.column.columnDef.header,
                    header.getContext()
                  )}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody>
          {table.getRowModel().rows.map((row) => (
            <tr
              key={row.id}
              className="odd:bg-white even:bg-gray-50 cursor-pointer hover:bg-blue-50 hover:text-blue-700"
              onClick={() =>
                navigate(`/admin/event-detail/${row.original.eventId}`)
              }
            >
              {row.getVisibleCells().map((cell) => (
                <td
                  key={cell.id}
                  className="border border-gray-300 px-4 py-2 text-sm text-gray-800"
                >
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      {renderPageControls()}
    </div>
  );
};

export default EventList;
