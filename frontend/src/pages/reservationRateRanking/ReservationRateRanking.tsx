import UserHeader from "../../components/@common/UserHeader.tsx";
import RankingTop50ByGenre from "../../components/reservatinRateRanking/RankingTop50ByGenre.tsx";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import MobileHeader from "../../components/@common/MobileHeader.tsx";
import { Genre, Period } from "../../types/ReservationRateRanking.ts";

const ReservationRateRanking = () => {
  const [isMobile, setIsMobile] = useState<boolean>(
    window.matchMedia("(max-width: 760px)").matches,
  );

  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = (searchParams.get("tab") as Genre) || Genre.뮤지컬;
  const initialPeriod = (searchParams.get("period") as Period) || Period.DAILY;

  const [activeTab, setActiveTab] = useState<Genre>(initialTab);
  const [period, setPeriod] = useState<Period>(initialPeriod);

  const periods = [
    { label: "일간", value: Period.DAILY },
    { label: "주간", value: Period.WEEKLY },
    { label: "월간", value: Period.MONTHLY },
  ];

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.matchMedia("(max-width: 760px)").matches);
    };

    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  const handleTabChange = (tab: Genre) => {
    setActiveTab(tab);
    setSearchParams({ tab });
  };

  const getPeriodLabel = () => {
    const now = new Date();
    if (period === Period.DAILY) {
      return `${now.getFullYear()}년 ${now.getMonth() + 1}월 ${now.getDate()}일 ${now.getHours()}시 ${now.getMinutes()}분 기준`;
    } else if (period === Period.WEEKLY) {
      const monday = new Date();
      monday.setDate(now.getDate() - now.getDay() + 1);
      return `${monday.getFullYear()}년 ${monday.getMonth() + 1}월 ${monday.getDate()}일 ~ ${now.getFullYear()}년 ${now.getMonth() + 1}월 ${now.getDate()}일 기준`;
    } else if (period === Period.MONTHLY) {
      return `${now.getFullYear()}년 ${now.getMonth() + 1}월 1일 ~ ${now.getDate()}일 기준`;
    }
  };

  return (
    <div className="p-6">
      <UserHeader />

      {/* 메인 컨테이너 */}
      <div className="container mx-auto px-4 lg:px-8 xl:px-20 max-w-[1440px]">
        {/* PC 헤더 */}
        {!isMobile && (
          <div className="text-center mb-4">
            <h1 className="text-[30px] font-medium">장르별 랭킹</h1>
          </div>
        )}
        {/* 모바일 헤더 */}
        {isMobile && <MobileHeader title="장르별 랭킹" />}

        {/* 탭 메뉴 */}
        <div className="mt-4">
          <div
            className={`${
              isMobile
                ? "flex overflow-x-auto whitespace-nowrap scrollbar-hide border-b mt-10"
                : "grid grid-cols-6 border"
            }`}
          >
            {Object.values(Genre).map((tab) => (
              <button
                key={tab}
                onClick={() => handleTabChange(tab)}
                className={`py-2 px-4 text-sm font-medium flex-shrink-0 ${
                  activeTab === tab
                    ? "text-white bg-purple-500 rounded-full"
                    : "text-gray-600 hover:bg-gray-100"
                } ${isMobile ? "mx-1 border rounded-full" : "border-r"}`}
              >
                {tab}
              </button>
            ))}
          </div>
        </div>

        {/* 기준 시간과 필터 메뉴 */}
        <div className="flex justify-between items-center mt-4 text-sm">
          {/* 기준 시간 */}
          <div className="text-gray-500 text-xs">{getPeriodLabel()}</div>

          {/* 필터 메뉴 */}
          <div className="flex space-x-4">
            {/* 모바일 드롭다운 */}
            {isMobile ? (
              <select
                value={period}
                onChange={(e) => setPeriod(e.target.value as Period)}
                className="border rounded px-2 py-1 text-gray-600"
              >
                {periods.map((p) => (
                  <option key={p.value} value={p.value}>
                    {p.label}
                  </option>
                ))}
              </select>
            ) : (
              // PC 버튼 필터
              periods.map((p) => (
                <button
                  key={p.value}
                  onClick={() => setPeriod(p.value)}
                  className={`${
                    period === p.value
                      ? "text-purple-600 font-medium border-b-2 border-purple-500"
                      : "text-gray-500"
                  }`}
                >
                  {p.label}
                </button>
              ))
            )}
          </div>
        </div>

        {/* 콘텐츠 */}
        <div className="mt-6">
          <RankingTop50ByGenre genre={activeTab} period={period} />
        </div>
      </div>
    </div>
  );
};

export default ReservationRateRanking;
