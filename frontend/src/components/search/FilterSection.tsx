import { useState } from "react";
import {
  GenreType,
  Location,
  SaleType,
  SearchParams,
} from "../../types/search.ts";

interface FilterSectionProps {
  onFilterChange: (filters: Partial<SearchParams>) => void;
}

const FilterSection = ({ onFilterChange }: FilterSectionProps) => {
  const [selectedGenres, setSelectedGenres] = useState<GenreType[]>([]);
  const [selectedSaleTypes, setSelectedSaleTypes] = useState<SaleType[]>([
    SaleType.ON_SALE,
    SaleType.TO_BE_SALE,
  ]);
  const [selectedLocations, setSelectedLocations] = useState<Location[]>([]);

  const [currentYear, setCurrentYear] = useState(2025);
  const [currentMonth, setCurrentMonth] = useState(1); // 1월부터 시작
  const [startDate, setStartDate] = useState<string | null>(null);
  const [endDate, setEndDate] = useState<string | null>(null);
  const today = new Date();

  // 월 이동 핸들러
  const handleMonthChange = (direction: "prev" | "next") => {
    if (direction === "prev") {
      if (currentMonth === 1) {
        setCurrentYear((prev) => prev - 1);
        setCurrentMonth(12);
      } else {
        setCurrentMonth((prev) => prev - 1);
      }
    } else {
      if (currentMonth === 12) {
        setCurrentYear((prev) => prev + 1);
        setCurrentMonth(1);
      } else {
        setCurrentMonth((prev) => prev + 1);
      }
    }
  };

  // 날짜 클릭 핸들러
  const handleDateClick = (day: number) => {
    const clickedDate = `${currentYear}-${String(currentMonth).padStart(2, "0")}-${String(
      day,
    ).padStart(2, "0")}`;

    if (!startDate || (startDate && endDate)) {
      setStartDate(clickedDate);
      setEndDate(null);
    } else if (!endDate) {
      if (new Date(clickedDate) < new Date(startDate)) {
        setEndDate(startDate);
        setStartDate(clickedDate);
      } else {
        setEndDate(clickedDate);
      }
      onFilterChange({ startDate, endDate: clickedDate });
    }
  };

  // 해당 월의 날짜 생성 (빈칸 포함)
  const generateDates = () => {
    const firstDayOfMonth = new Date(currentYear, currentMonth - 1, 1).getDay(); // 첫 번째 날짜의 요일
    const daysInMonth = new Date(currentYear, currentMonth, 0).getDate(); // 해당 월의 마지막 날
    const dates = Array.from({ length: daysInMonth }, (_, i) => i + 1);
    const emptyDays = Array.from({ length: firstDayOfMonth }, () => null); // 첫 주의 빈 칸
    return [...emptyDays, ...dates];
  };

  // 날짜가 범위 안에 있는지 확인
  const isInRange = (day: number) => {
    const currentDate = `${currentYear}-${String(currentMonth).padStart(2, "0")}-${String(
      day,
    ).padStart(2, "0")}`;
    return (
      startDate && endDate && currentDate >= startDate && currentDate <= endDate
    );
  };

  // 날짜가 선택 가능한지 확인
  const isDateSelectable = (day: number) => {
    const currentDate = new Date(
      `${currentYear}-${String(currentMonth).padStart(2, "0")}-${String(day).padStart(2, "0")}`,
    );
    return currentDate >= today;
  };

  const handleGenreChange = (genre: GenreType) => {
    setSelectedGenres((prev) => {
      const updated = prev.includes(genre)
        ? prev.filter((g) => g !== genre)
        : [...prev, genre];
      onFilterChange({ genreList: updated });
      return updated;
    });
  };

  const handleSaleStatusChange = (saleType: SaleType) => {
    setSelectedSaleTypes((prev) => {
      const updated = prev.includes(saleType)
        ? prev.filter((s) => s !== saleType)
        : [...prev, saleType];
      onFilterChange({ saleTypeList: updated });
      return updated;
    });
  };

  const handleRegionChange = (region: Location) => {
    setSelectedLocations((prev) => {
      const updated = prev.includes(region)
        ? prev.filter((r) => r !== region)
        : [...prev, region];
      onFilterChange({ locationList: updated });
      return updated;
    });
  };

  const handleReset = () => {
    setSelectedGenres([]);
    setSelectedSaleTypes([]);
    setSelectedLocations([]);
    onFilterChange({
      genreList: undefined,
      saleTypeList: undefined,
      startDate: undefined,
      endDate: undefined,
      locationList: undefined,
    });
  };

  return (
    <div className="w-64 bg-white border border-gray-200 rounded-lg p-6">
      <h2 className="text-lg font-bold mb-4 text-gray-800 border-b border-gray-300 pb-2">
        필터
      </h2>

      {/* 장르 */}
      <div className="mb-6 border-b border-gray-300 pb-4">
        <h3 className="font-medium mb-3 text-gray-700">장르</h3>
        <div className="flex flex-wrap gap-2">
          {Object.values(GenreType).map((genre) => (
            <button
              key={genre}
              className={`px-3 py-1 rounded-md border text-sm  ${
                selectedGenres.includes(genre)
                  ? "bg-purple-500 text-white border-purple-500"
                  : "text-gray-600 border-gray-300 hover:bg-gray-100"
              }`}
              onClick={() => handleGenreChange(genre)}
            >
              {genre}
            </button>
          ))}
        </div>
      </div>

      {/* 판매 상태 */}
      <div className="mb-6 border-b border-gray-300 pb-4">
        <h3 className="font-medium mb-3 text-gray-700">판매 상태</h3>
        <div className="flex flex-wrap gap-2">
          {Object.values(SaleType).map((saleType) => (
            <button
              key={saleType}
              className={`px-3 py-1 rounded-md text-sm border ${
                selectedSaleTypes.includes(saleType)
                  ? "bg-purple-500 text-white border-purple-500"
                  : "text-gray-600 border-gray-300 hover:bg-gray-100"
              }`}
              onClick={() => handleSaleStatusChange(saleType)}
            >
              {saleType === SaleType.ON_SALE
                ? "판매중"
                : saleType === SaleType.TO_BE_SALE
                  ? "판매예정"
                  : "판매종료"}
            </button>
          ))}
        </div>
      </div>

      {/* 날짜 */}
      <div className="mb-6 border-b border-gray-300 pb-4">
        <h3 className="font-medium mb-3 text-gray-700">날짜</h3>
        <div className="flex items-center justify-between mb-2">
          <button
            className={`text-gray-600 hover:text-black ${
              currentYear === today.getFullYear() &&
              currentMonth === today.getMonth() + 1
                ? "cursor-not-allowed text-gray-400"
                : ""
            }`}
            onClick={() =>
              currentMonth > today.getMonth() + 1 ||
              currentYear > today.getFullYear()
                ? handleMonthChange("prev")
                : null
            }
            disabled={
              currentYear === today.getFullYear() &&
              currentMonth === today.getMonth() + 1
            }
          >
            &lt;
          </button>
          <div className="text-lg font-bold text-gray-800">
            {currentYear}.{String(currentMonth).padStart(2, "0")}
          </div>
          <button
            className="text-gray-600 hover:text-black"
            onClick={() => handleMonthChange("next")}
          >
            &gt;
          </button>
        </div>
        <div className="grid grid-cols-7 gap-2 text-center text-gray-600 text-sm">
          <div className="font-bold text-red-500">일</div>
          <div>월</div>
          <div>화</div>
          <div>수</div>
          <div>목</div>
          <div>금</div>
          <div className="font-bold">토</div>
          {generateDates().map((day, index) =>
            day === null ? (
              <div key={index}></div>
            ) : (
              <button
                key={day}
                className={`py-1 rounded-md ${
                  isInRange(day)
                    ? "bg-purple-200 text-purple-700"
                    : startDate ===
                        `${currentYear}-${String(currentMonth).padStart(2, "0")}-${String(
                          day,
                        ).padStart(2, "0")}`
                      ? "bg-purple-500 text-white"
                      : isDateSelectable(day)
                        ? "hover:bg-gray-200"
                        : "text-gray-400 cursor-not-allowed"
                }`}
                onClick={() => isDateSelectable(day) && handleDateClick(day)}
                disabled={!isDateSelectable(day)}
              >
                {day}
              </button>
            ),
          )}
        </div>
      </div>

      {/* 지역 */}
      <div className="mb-6 border-b border-gray-300 pb-4">
        <h3 className="font-medium mb-3 text-gray-700">지역</h3>
        <div className="flex flex-wrap gap-2">
          {Object.entries(Location).map(([shortName, fullName]) => (
            <button
              key={fullName}
              className={`px-3 py-1 rounded-md border text-sm ${
                selectedLocations.includes(fullName as Location)
                  ? "bg-purple-500 text-white border-purple-500"
                  : "text-gray-600 border-gray-300 hover:bg-gray-100"
              }`}
              onClick={() => handleRegionChange(fullName as Location)}
            >
              {shortName}
            </button>
          ))}
        </div>
      </div>

      {/* 초기화 버튼 */}
      <button
        className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700"
        onClick={handleReset}
      >
        초기화
      </button>
    </div>
  );
};

export default FilterSection;
