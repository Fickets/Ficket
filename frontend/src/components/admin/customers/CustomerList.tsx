import {
    FaList,
    FaAngleDoubleLeft,
    FaAngleLeft,
    FaAngleRight,
    FaAngleDoubleRight,
} from 'react-icons/fa';
import { useMemo, useState } from 'react';
import {
    useReactTable,
    getCoreRowModel,
    getPaginationRowModel,
    flexRender,
    ColumnDef,
} from '@tanstack/react-table';

import CustomerDetailModal from './CustomerDetail';
import { CustomerListProps2, userSimpleDto } from '../../../types/eventList';
const CustomerList: React.FC<CustomerListProps2> = ({ data, onPageChange }) => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedData, setSelectedData] = useState<any>(null); // 선택된 고객 데이터

    const columns: ColumnDef<userSimpleDto>[] = useMemo(
        () => [
            {
                header: 'NO',
                cell: ({ row }) => row.index + 1 + data.page * data.size,
            },
            {
                header: '고객 식별번호',
                accessorKey: 'userId',
            },
            {
                header: '고객 이름',
                accessorKey: 'userName',
            },
            {
                header: '성별',
                accessorKey: 'gender',
            },
            {
                header: '출생연도',
                accessorKey: 'birth',
            },
            {
                header: '소셜 아이디',
                accessorKey: 'socialId',
                cell: ({ getValue }) => {
                    const value = getValue() as any; // number로 예상되는 값

                    if (!value) return null; // socialId가 없으면 빈 값 리턴

                    // socialId를 하나의 값으로 변환하여 표시
                    return (
                        <span className="block">{value}</span>
                    );
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
        const maxPagesToShow = 5;
        const startPage = Math.max(0, pageIndex - Math.floor(maxPagesToShow / 2));
        const endPage = Math.min(startPage + maxPagesToShow, data.totalPages);

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
                {Array.from(
                    { length: endPage - startPage },
                    (_, i) => i + startPage,
                ).map((page) => (
                    <button
                        key={page}
                        onClick={() => onPageChange(page)}
                        className={`px-3 py-2 rounded-md text-sm ${pageIndex === page
                            ? "bg-blue-500 text-white"
                            : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                            }`}
                    >
                        {page + 1}
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
    const userListClick = (rowId: string) => {
        const row = table.getRowModel().rows.find(r => r.id === rowId);
        if (row) {
            setSelectedData(row.original); // 선택된 데이터 저장
            setIsModalOpen(true); // 모달 열기
        } else {
            console.log("Row not found");
        }
    };

    const closeModal = () => {
        setIsModalOpen(false); // 모달 닫기
        setSelectedData(null); // 선택된 데이터 초기화
    };

    return (
        <div className="w-full bg-white rounded-lg shadow-md p-6 border border-gray-200">
            <div className="flex items-center justify-between mb-4">
                <div className="flex items-center">
                    <FaList className="text-xl" />
                    <span className="ml-2 text-lg font-semibold">고객 목록</span>
                </div>
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
                    <CustomerDetailModal isOpen={isModalOpen} onClose={closeModal} data={selectedData} />
                    {table.getRowModel().rows.map((row) => (
                        <tr
                            key={row.id}
                            className="odd:bg-white even:bg-gray-50 cursor-pointer hover:bg-blue-50 hover:text-blue-700"
                            onClick={() =>
                                userListClick(row.id)
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
        </div >
    );
};

export default CustomerList;
