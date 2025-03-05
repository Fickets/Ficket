import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ViewRankResponse } from "../../types/home.ts";
import { fetchViewRanking } from "../../service/home/api.ts";

const PCViewRanking = () => {
  const [viewRankingResponse, setViewRankingResponse] = useState<
    ViewRankResponse[]
  >([]);
  const [currentIndex, setCurrentIndex] = useState(0); // 현재 슬라이드 인덱스
  const [showThumbnails, setShowThumbnails] = useState(false); // 썸네일 표시 여부
  const navigate = useNavigate();

  const fetchViewTopTen = async () => {
    try {
      const response = await fetchViewRanking(10);
      setViewRankingResponse(response);
      console.log(response);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    fetchViewTopTen();
  }, []);

  const handlePrev = () => {
    setCurrentIndex((prevIndex) =>
      prevIndex === 0 ? viewRankingResponse.length - 1 : prevIndex - 1,
    );
  };

  const handleNext = () => {
    setCurrentIndex((prevIndex) =>
      prevIndex === viewRankingResponse.length - 1 ? 0 : prevIndex + 1,
    );
  };

  const handleBannerClick = (eventId: number) => {
    navigate(`/events/detail/${eventId}`); // 배너 클릭 시 페이지 이동
  };

  // 날짜 포맷팅 함수
  const formatEventDateRange = (eventDateList: string[]) => {
    if (eventDateList.length === 0) return "";

    const sortedDates = [...eventDateList].sort(); // 날짜를 문자열 기준으로 정렬
    const startDate = sortedDates[0]; // 가장 첫 날짜
    const endDate = sortedDates[sortedDates.length - 1]; // 가장 마지막 날짜

    // 시작 날짜와 끝 날짜가 같으면 하나만 반환
    if (startDate === endDate) {
      return startDate;
    }

    // 시작 날짜 ~ 끝 날짜 형식 반환
    return `${startDate} ~ ${endDate}`;
  };

  return (
    <div
      className="relative w-screen h-[540px] bg-black overflow-hidden -mt-[50px]"
      style={{
        transform: "translateX(-24px)", // 전체를 왼쪽으로 24px 이동
      }}
    >
      {viewRankingResponse.length > 0 && (
        <>
          {/* 메인 슬라이드 */}
          <div
            className="w-full h-full bg-cover bg-center transition-transform duration-500"
            style={{
              transform: `translateX(-${currentIndex * 100}%)`,
              whiteSpace: "nowrap",
            }}
          >
            {viewRankingResponse.map((item) => (
              <div
                key={item.eventId}
                className="inline-block w-full h-full bg-cover bg-center relative cursor-pointer"
                style={{
                  backgroundImage: `url(${item.eventOriginBannerUrl})`,
                }}
                onClick={() => handleBannerClick(item.eventId)}
              >
                <div className="absolute inset-0 bg-gradient-to-r from-black/60 to-transparent flex items-center">
                  <div className="text-white ml-[325px] -mt-[110px]">
                    <h1 className="text-5xl font-bold">{item.eventTitle}</h1>
                    <h2 className="text-2xl font-medium mt-7">
                      {item.eventSubTitle}
                    </h2>
                    <p className="mt-8 text-lg">{item.eventStageName}</p>
                    <p className="text-sm">
                      {formatEventDateRange(item.eventDateList)}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* 이전 버튼 */}
          <button
            className="absolute left-6 top-1/2 transform -translate-y-1/2 text-white bg-gray-800 bg-opacity-60 hover:bg-opacity-80 p-6 rounded-full text-2xl"
            onClick={handlePrev}
          >
            ❮
          </button>

          {/* 다음 버튼 */}
          <button
            className="absolute right-6 top-1/2 transform -translate-y-1/2 text-white bg-gray-800 bg-opacity-60 hover:bg-opacity-80 p-6 rounded-full text-2xl"
            onClick={handleNext}
          >
            ❯
          </button>

          {/* 구분선 + 썸네일 영역 */}
          <div
            className="absolute bottom-0 left-0 w-full transition-transform duration-300"
            onMouseEnter={() => setShowThumbnails(true)}
            onMouseLeave={() => setShowThumbnails(false)}
          >
            {/* 썸네일 배경 */}
            <div
              className={`absolute bottom-0 left-0 w-full h-[200px] bg-black/60 transition-opacity duration-300 ${
                showThumbnails ? "opacity-100" : "opacity-0"
              }`}
            ></div>

            {/* 구분선 */}
            <div
              className={`absolute left-0 w-full h-[3px] bg-gray-400 transition-transform duration-300 ${
                showThumbnails ? "translate-y-[-200px]" : ""
              }`}
            ></div>

            {/* 썸네일 영역 */}
            <div
              className={`absolute bottom-0 left-0 w-full transition-transform duration-300 ${
                showThumbnails ? "translate-y-[-3px]" : "translate-y-[200px]"
              }`}
            >
              <div className="flex items-center justify-center gap-6 overflow-x-auto px-8 py-4 scrollbar-hide">
                {viewRankingResponse.map((item, index) => (
                  <div
                    key={item.eventId}
                    onClick={() => setCurrentIndex(index)} // 클릭으로 슬라이드 이동
                    className={`cursor-pointer w-[100px] h-[140px] flex-shrink-0 rounded-md overflow-hidden relative transition-all ${
                      currentIndex === index ? "border-4 border-orange-500" : ""
                    }`}
                  >
                    {/* 썸네일 이미지 */}
                    <img
                      src={item.eventPcPosterUrl}
                      alt={item.eventTitle}
                      className="w-full h-full object-cover"
                    />
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}

      {/* 로딩 메시지 */}
      {viewRankingResponse.length === 0 && (
        <div className="text-center text-lg text-gray-700">
          Loading rankings...
        </div>
      )}
    </div>
  );
};

export default PCViewRanking;
