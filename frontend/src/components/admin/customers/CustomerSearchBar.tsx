import { FaDatabase } from "react-icons/fa";
import "react-datepicker/dist/react-datepicker.css";
import DatePicker from "react-datepicker";
import { useState, useEffect } from "react";
import { CustomerSearchParams, CustomerSearchBarProps, Customer } from "../../../types/admins/Customers.ts";
import Select from "react-select";
import { fetchCustomers } from "../../../service/admin/customer/customerService.ts";

const CustomerSearchBar = ({ onSearch }: CustomerSearchBarProps) => {
    const [localSearchParams, setLocalSearchParams] = useState<CustomerSearchParams>({
        userId: null,
        userName: null,
        startDate: null,
        endDate: null,
    });
    const [customers, setCustomers] = useState<String[]>([]);

    useEffect(() => {
        fetchCustomers().then((response) => setCustomers(response));
    }, []);

    const handleInputChange = (
        key: keyof CustomerSearchParams,
        value: string | number | null,
    ) => {
        setLocalSearchParams((prev) => ({
            ...prev,
            [key]: value,
        }));
    };

    const handleSearchClick = () => {
        onSearch({ ...localSearchParams });
    };

    const handleReset = () => {
        setLocalSearchParams({
            userId: null,
            userName: null,
            startDate: null,
            endDate: null,
        });
    };

    return (
        <div className="w-full bg-white rounded-lg shadow-md p-6 border border-gray-200">
            {/* 헤더 */}
            <div className="flex items-center justify-between mb-4">
                <div className="flex items-center">
                    <FaDatabase className="text-xl" />
                    <span className="ml-2 text-lg font-semibold">조건별 검색</span>
                </div>
                <div className="space-x-2">
                    <button
                        type="button"
                        onClick={handleReset}
                        className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 text-sm"
                    >
                        초기화
                    </button>
                    <button
                        type="button"
                        onClick={handleSearchClick}
                        className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 text-sm"
                    >
                        조회
                    </button>
                </div>
            </div>
            <hr className="mb-6 border-gray-300" />

            {/* 폼 */}
            <form className="grid grid-cols-6 gap-x-4 gap-y-4">
                {/* 고객 식별번호 */}
                <div className="col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        고객 식별번호
                    </label>
                    <input
                        type="number"
                        placeholder="고객 식별번호 입력"
                        value={localSearchParams.userId || ""}
                        onChange={(e) =>
                            handleInputChange("userId", parseInt(e.target.value, 10) || null)
                        }
                        className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 placeholder-gray-500"
                    />
                </div>
                {/* 고객 이름 */}
                <div className="col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        고객 이름
                    </label>
                    <Select
                        options={customers.map((customer) => ({
                            // label: `${customer.userName}`,
                            label: `${customer}`,
                            // value: customer.userName, // 고객 이름을 value로 설정
                            value: customer, // 고객 이름을 value로 설정
                        }))}
                        value={
                            localSearchParams.userName
                                ? {
                                    label: `${localSearchParams.userName}`,
                                    value: localSearchParams.userName,
                                }
                                : null
                        }
                        onChange={(option) => {
                            // 고객 이름 선택 시 userId를 업데이트하지 않도록 설정
                            handleInputChange("userName", option?.value || null);
                        }}
                        placeholder="고객을 선택하세요"
                        isClearable
                        styles={{
                            placeholder: (base) => ({
                                ...base,
                                fontSize: "0.875rem",
                                color: "#6B7280",
                            }),
                            control: (base) => ({
                                ...base,
                                height: "40px",
                                borderColor: "#D1D5DB",
                                fontSize: "0.875rem",
                            }),
                        }}
                    />
                </div>
                {/* 기간 조회 */}
                <div className="col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        기간 조회
                    </label>
                    <div className="flex items-center space-x-2">
                        <DatePicker
                            selected={
                                localSearchParams.startDate
                                    ? new Date(localSearchParams.startDate)
                                    : null
                            }
                            onChange={(date) =>
                                handleInputChange(
                                    "startDate",
                                    date ? date.toISOString().split("T")[0] : null,
                                )
                            }
                            dateFormat="yyyy-MM-dd"
                            placeholderText="시작 날짜"
                            className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm"
                        />
                        <span className="text-gray-500">-</span>
                        <DatePicker
                            selected={
                                localSearchParams.endDate
                                    ? new Date(localSearchParams.endDate)
                                    : null
                            }
                            onChange={(date) =>
                                handleInputChange(
                                    "endDate",
                                    date ? date.toISOString().split("T")[0] : null,
                                )
                            }
                            minDate={
                                localSearchParams.startDate
                                    ? new Date(localSearchParams.startDate)
                                    : undefined
                            }
                            dateFormat="yyyy-MM-dd"
                            placeholderText="종료 날짜"
                            className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm"
                        />
                    </div>
                </div>
            </form>
        </div>
    );
};

export default CustomerSearchBar;
