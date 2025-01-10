import Sidebar from "../../components/@common/Sidebar";
import EventForm from "../../components/register/EventForm";
import CalendarWithSchedule from "../../components/register/CalendarWithSchedule";
import SeatSetting from "../../components/register/SeatSetting";
import { useState, useEffect } from "react";
import { EventData } from "../../types/register";
import { registerEvent, fetchStages } from "../../service/register/api";
import ImageUploader from "../../components/register/ImageUploader";
import TinyEditor from "../../components/register/TinyEditor";
import { useNavigate } from "react-router-dom";
import { Helmet } from "react-helmet-async";

interface Stage {
  stageId: number;
  stageName: string;
  eventStageImg: string;
}

const RegisterEvent = () => {
  const navigate = useNavigate();
  const [eventData, setEventData] = useState<EventData | null>({
    companyId: 0,
    stageId: 0, // 행사장 ID 초기값 설정
    genre: [],
    age: "",
    content: "",
    title: "",
    subTitle: "",
    runningTime: 0,
    ticketingTime: "",
    reservationLimit: 0,
    eventDate: [],
    seats: [],
  });

  const [poster, setPoster] = useState<File | null>(null);
  const [banner, setBanner] = useState<File | null>(null);
  const [stageImg, setStageImg] = useState<string | null>(null);

  // Fetch stage image when stageId changes
  useEffect(() => {
    const fetchStageImage = async () => {
      if (!eventData?.stageId) {
        setStageImg(null);
        return;
      }
      try {
        const stages: Stage[] = await fetchStages();
        const selectedStage = stages.find(
          (stage) => stage.stageId === eventData.stageId,
        );
        setStageImg(selectedStage?.eventStageImg || null);
      } catch (error) {
        console.error("Error fetching stage image:", error);
      }
    };

    fetchStageImage();
  }, [eventData?.stageId]);

  const handleFormChange = (formData: Partial<EventData>) => {
    setEventData((prevData) =>
      prevData ? { ...prevData, ...formData } : null,
    );
  };

  const handlePosterChange = (selectedImage: File | null) => {
    setPoster(selectedImage);
  };

  const handleBannerChange = (selectedImage: File | null) => {
    setBanner(selectedImage);
  };

  const handleRegister = async () => {
    if (!eventData) {
      alert("입력된 데이터가 없습니다.");
      return;
    }
    const formData = new FormData();
    console.log(JSON.stringify(eventData));
    formData.append(
      "req",
      new Blob([JSON.stringify(eventData)], { type: "application/json" }),
    );
    if (poster) {
      formData.append("poster", poster);
    }
    if (banner) {
      formData.append("banner", banner);
    }
    try {
      const response = await registerEvent(formData);
      alert(response);
      navigate("/admin/event-list"); // 성공 시 이동
    } catch (error) {
      console.error("공연 등록 실패:", error);
      alert("공연 등록 중 오류가 발생했습니다."); // 실패 시 알림창 표시
    }
  };

  return (
    <div className="flex h-screen bg-[#F0F2F5]">
      <Helmet>
        <title>공연 생성</title>
      </Helmet>
      {/* 사이드바 */}
      <div className="w-64 h-full">
        <Sidebar currentStep={"performance"} />
      </div>

      {/* 메인 컨텐츠 */}
      <div className="flex-1 p-8 overflow-auto">
        <div
          className="bg-white p-8 rounded-xl shadow-lg mx-auto"
          style={{ width: "1580px" }}
        >
          <h2 className="text-2xl font-bold mb-8">공연 등록</h2>

          {/* 메인 레이아웃 */}
          <div className="flex flex-wrap justify-between gap-8">
            {/* 왼쪽 섹션 */}
            <div
              className="space-y-6"
              style={{
                flex: "0 0 500px", // 고정 너비
                maxWidth: "500px",
              }}
            >
              <EventForm onChange={handleFormChange} />
              <ImageUploader
                title="포스터 업로드"
                aspectRatio={2960 / 3520}
                onChange={handlePosterChange}
              />
              <ImageUploader
                title="배너 업로드"
                aspectRatio={1920 / 620}
                onChange={handleBannerChange}
              />
            </div>

            {/* 오른쪽 섹션 */}
            <div
              className="space-y-6 flex-1"
              style={{
                minWidth: "0", // 플렉스 항목 오버플로우 방지
              }}
            >
              <CalendarWithSchedule onChange={handleFormChange} />
              <SeatSetting
                stageId={eventData?.stageId || 0}
                stageImg={stageImg || ""}
                onChange={handleFormChange}
              />
              <TinyEditor
                onChange={(content) => handleFormChange({ content })}
              />
            </div>
          </div>

          {/* 등록 버튼 */}
          <div className="flex justify-end mt-8">
            <button
              onClick={handleRegister}
              className="bg-blue-500 text-white px-6 py-3 rounded-lg shadow hover:bg-blue-600 transition"
            >
              등록
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterEvent;
