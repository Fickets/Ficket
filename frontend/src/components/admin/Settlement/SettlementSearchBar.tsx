import { FaDatabase } from "react-icons/fa";
import "react-datepicker/dist/react-datepicker.css";
import Select from "react-select";
import DatePicker from "react-datepicker";
import { useState, useEffect } from "react";
import { SettlementSearchParams, SettlementSearchBarProps } from "../../../types/admins/Settlement/Settlement";
import { fetchEvents } from "../../../service/admin/settlement/settlementService";

const SettlementSearchBar = ({ onSearch }: SettlementSearchBarProps) => {
    const [localSearchParams, setLocalSearchParams] = useState<SettlementSearchParams>({
        eventName: null,
        settlementStatus: null,
        startDate: null,
        endDate: null
    });

    const [events, setEvents] = useState<string[]>([]);
    const [filteredEvents, setFilteredEvents] = useState<string[]>([]);

    useEffect(() => {
        fetchEvents().then((response) => {
            setEvents(response);
            setFilteredEvents(response.slice(0, 10)); // 처음 10개만 표시
        });
    }, []);

    const handleInputChange = (
        key: keyof SettlementSearchParams,
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
            eventName: null,
            settlementStatus: null,
            startDate: null,
            endDate: null,
        });
    };

    // 공연 검색 기능 추가 (10개까지만 표시)
    const handleSearchChange = (query: string) => {

        const filtered = events
            .filter(event => event.toLowerCase().includes(query.toLowerCase()))
            .slice(0, 10); // 검색된 결과 중 상위 10개만 유지

        setFilteredEvents(filtered);
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
            <form className="grid grid-cols-6 gap-x-4 gap-y-4">
                {/* 공연 선택 */}
                <div className="col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        공연
                    </label>
                    <Select
                        options={filteredEvents.map((event) => ({
                            label: event,
                            value: event,
                        }))}
                        value={
                            localSearchParams.eventName
                                ? { label: localSearchParams.eventName, value: localSearchParams.eventName }
                                : null
                        }
                        onInputChange={(inputValue) => handleSearchChange(inputValue)}
                        onChange={(option) => handleInputChange("eventName", option?.value || null)}
                        placeholder="공연을 선택하세요"
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

                {/* 정산 상태 */}
                <div className="col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        정산 상태
                    </label>
                    <select
                        value={localSearchParams.settlementStatus || ""}
                        onChange={(e) =>
                            handleInputChange("settlementStatus", e.target.value || null)
                        }
                        className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700"
                    >
                        <option value="">선택하세요</option>
                        <option value="SETTLEMENT">정산완료</option>
                        <option value="PARTIAL_SETTLEMENT">부분정산</option>
                        <option value="UNSETTLED">미정산</option>
                    </select>
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

export default SettlementSearchBar;
