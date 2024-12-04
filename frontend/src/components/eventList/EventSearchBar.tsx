import { FaDatabase } from "react-icons/fa";
import "react-datepicker/dist/react-datepicker.css";
import DatePicker from "react-datepicker";
import { useState, useEffect } from "react";
import Select from "react-select";
import {
  Admin,
  EventSearchBarProps,
  SearchParams,
} from "../../types/eventList.ts";
import { fetchCompanies, fetchStages } from "../../service/register/api.ts";
import { fetchAdmins } from "../../service/admineventlist/api.ts";
import { Company, Stage } from "../../types/register.ts";

const EventSearchBar = ({ onSearch }: EventSearchBarProps) => {
  const [localSearchParams, setLocalSearchParams] = useState<SearchParams>({
    eventId: null,
    eventTitle: null,
    companyId: null,
    adminId: null,
    eventStageId: null,
    startDate: null,
    endDate: null,
  });

  const [stages, setStages] = useState<Stage[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [admins, setAdmins] = useState<Admin[]>([]);

  useEffect(() => {
    // Fetch stages, companies, and admins data on component mount
    fetchStages().then((response) => setStages(response));
    fetchCompanies().then((response) => setCompanies(response));
    fetchAdmins().then((response) => setAdmins(response));
  }, []);

  const handleInputChange = (
    key: keyof SearchParams,
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
      eventId: null,
      eventTitle: null,
      companyId: null,
      adminId: null,
      eventStageId: null,
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
        {/* 공연 식별번호 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            공연 식별번호
          </label>
          <input
            type="number"
            placeholder="공연 식별번호 입력"
            value={localSearchParams.eventId || ""}
            onChange={(e) =>
              handleInputChange("eventId", parseInt(e.target.value, 10) || null)
            }
            className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 placeholder-gray-500"
          />
        </div>

        {/* 공연 제목 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            공연 제목
          </label>
          <input
            type="text"
            placeholder="공연 제목 입력"
            value={localSearchParams.eventTitle || ""}
            onChange={(e) => handleInputChange("eventTitle", e.target.value)}
            className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 placeholder-gray-500"
          />
        </div>

        {/* 담당 관리자 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            담당 관리자
          </label>
          <Select
            options={admins.map((admin) => ({
              label: `${admin.adminId} - ${admin.adminName}`,
              value: admin.adminId,
            }))}
            value={
              localSearchParams.adminId
                ? {
                    label: `${localSearchParams.adminId} - ${
                      admins.find(
                        (admin) => admin.adminId === localSearchParams.adminId,
                      )?.adminName || ""
                    }`,
                    value: localSearchParams.adminId,
                  }
                : null
            }
            onChange={(option) =>
              handleInputChange("adminId", option?.value || null)
            }
            placeholder="관리자를 선택하세요"
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

        {/* 회사 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            회사
          </label>
          <Select
            options={companies.map((company) => ({
              label: company.companyName,
              value: company.companyId,
            }))}
            value={
              localSearchParams.companyId
                ? {
                    label: companies.find(
                      (company) =>
                        company.companyId === localSearchParams.companyId,
                    )?.companyName,
                    value: localSearchParams.companyId,
                  }
                : null
            }
            onChange={(option) =>
              handleInputChange("companyId", option?.value || null)
            }
            placeholder="회사를 선택하세요"
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

        {/* 공연장 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            공연장
          </label>
          <Select
            options={stages.map((stage) => ({
              label: stage.stageName,
              value: stage.stageId,
            }))}
            value={
              localSearchParams.eventStageId
                ? {
                    label: stages.find(
                      (stage) =>
                        stage.stageId === localSearchParams.eventStageId,
                    )?.stageName,
                    value: localSearchParams.eventStageId,
                  }
                : null
            }
            onChange={(option) =>
              handleInputChange("eventStageId", option?.value || null)
            }
            placeholder="공연장을 선택하세요"
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

export default EventSearchBar;
