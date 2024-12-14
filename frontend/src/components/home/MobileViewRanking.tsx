import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom"; // useNavigate import
import { ViewRankResponse } from "../../types/home.ts";
import { fetchViewRanking } from "../../service/home/api.ts";
import { useSwipeable } from "react-swipeable";

const MobileViewRanking = () => {
  const [viewRankingResponse, setViewRankingResponse] = useState<
    ViewRankResponse[]
  >([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const navigate = useNavigate(); // Initialize navigate

  const fetchViewTopTen = async () => {
    try {
      const response = await fetchViewRanking(10);
      setViewRankingResponse(response);
    } catch (error) {
      console.error(error);
      alert("Failed to fetch view rankings.");
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

  const handleImageClick = () => {
    const eventId = viewRankingResponse[currentIndex]?.eventId;
    if (eventId) {
      navigate(`/events/detail/${eventId}`); // Navigate to the event details page
    }
  };

  const swipeHandlers = useSwipeable({
    onSwipedLeft: handleNext,
    onSwipedRight: handlePrev,
    preventScrollOnSwipe: true,
    trackTouch: true,
    trackMouse: false,
  });

  return (
    <div
      className="relative w-full h-auto flex items-center justify-center"
      style={{ height: "calc(80vh - 50px)" }} // 화면의 높이를 더 줄임
      {...swipeHandlers}
    >
      {viewRankingResponse.length > 0 && (
        <div className="relative w-full max-w-[360px] h-auto">
          {/* 배너 이미지 */}
          <div
            className="relative w-full h-[50vh] rounded-lg overflow-hidden shadow-md cursor-pointer"
            onClick={handleImageClick} // Click event handler added
          >
            <img
              src={viewRankingResponse[currentIndex]?.eventOriginBannerUrl}
              alt={viewRankingResponse[currentIndex]?.eventTitle}
              className="w-full h-full object-cover"
            />
            {/* 텍스트 오버레이 */}
            <div className="absolute inset-0 flex flex-col justify-end p-4 text-white">
              <div className="mb-4">
                <h1 className="text-lg font-bold">
                  {viewRankingResponse[currentIndex]?.eventTitle}
                </h1>
                <p className="text-sm">
                  {viewRankingResponse[currentIndex]?.eventSubTitle}
                </p>
                <p className="text-xs mt-1">
                  {viewRankingResponse[currentIndex]?.eventDateList[0]} ~{" "}
                  {viewRankingResponse[currentIndex]?.eventDateList[1]}
                </p>
              </div>
            </div>

            {/* 슬라이드 인덱스 표시 */}
            <div className="absolute bottom-3 right-3 bg-black/60 text-white text-xs px-2 py-1 rounded-md">
              {currentIndex + 1} / {viewRankingResponse.length}
            </div>
          </div>
        </div>
      )}

      {/* 로딩 메시지 */}
      {viewRankingResponse.length === 0 && (
        <div className="flex items-center justify-center h-full text-gray-700 text-sm">
          Loading rankings...
        </div>
      )}
    </div>
  );
};

export default MobileViewRanking;
