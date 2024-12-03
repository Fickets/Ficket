import { FaDatabase } from "react-icons/fa";
import { useState } from "react";
import "react-datepicker/dist/react-datepicker.css";
import DatePicker from "react-datepicker";

const EventSearchBar = () => {
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

  const handleReset = () => {
    // 초기화 로직
    setStartDate(null);
    setEndDate(null);
    console.log("폼 초기화!");
  };

  const handleSearch = () => {
    // 조회 로직
    console.log("조회 버튼 클릭!");
  };

  return (
    <div className="w-full bg-white rounded-lg shadow-md p-6 border border-gray-200">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-4">
        {/* 왼쪽: 제목 */}
        <div className="flex items-center">
          <FaDatabase className="text-xl" />
          <span className="ml-2 text-lg font-semibold">조건별 검색</span>
        </div>

        {/* 오른쪽: 버튼 */}
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
            onClick={handleSearch}
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
            공연 식별번호<span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            placeholder="공연 식별번호 입력해 주세요."
            className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 공연 제목 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            공연 제목<span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            placeholder="공연 제목을 입력해 주세요."
            className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 담당 관리자 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            담당 관리자<span className="text-red-500">*</span>
          </label>
          <select className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">담당 관리자를 검색해 주세요</option>
            <option value="manager1">홍길동</option>
            <option value="manager2">김철수</option>
          </select>
        </div>

        {/* 공연장 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            공연장<span className="text-red-500">*</span>
          </label>
          <select className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">공연장을 검색해 주세요</option>
            <option value="venue1">서울 공연장</option>
            <option value="venue2">부산 공연장</option>
          </select>
        </div>

        {/* 거래처 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            거래처<span className="text-red-500">*</span>
          </label>
          <select className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">거래처를 검색해 주세요</option>
            <option value="client1">ABC 회사</option>
            <option value="client2">DEF 회사</option>
          </select>
        </div>

        {/* 기간 조회 */}
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            기간 조회<span className="text-red-500">*</span>
          </label>
          <div className="flex items-center space-x-2">
            <DatePicker
              selected={startDate}
              onChange={(date) => setStartDate(date || null)}
              dateFormat="yyyy-MM-dd"
              placeholderText="시작 날짜"
              className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <span className="text-gray-500">-</span>
            <DatePicker
              selected={endDate}
              onChange={(date) => setEndDate(date || null)}
              minDate={startDate || undefined}
              dateFormat="yyyy-MM-dd"
              placeholderText="종료 날짜"
              className="w-full h-10 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      </form>
    </div>
  );
};

export default EventSearchBar;
