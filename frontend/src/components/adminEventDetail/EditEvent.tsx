import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import EditCalendarWithSchedule from "../edit/EditCalendarWithSchedule";
import EditSeatSetting from "../edit/EditSeatSetting";
import EventForm from "../edit/EditEventForm";
import ImageUploader from "../edit/EditImageUploader";
import TinyEditor from "../edit/EditTinyEditor";
import { fetchEventDetail, updateEvent } from "../../service/edit/api";
import { EventDetailData } from "../../types/edit";
import { EventData } from "../../types/register";

const EditEvent = ({ eventId }: { eventId: string }) => {
  const navigate = useNavigate();

  const numericEventId = eventId ? Number(eventId) : null;

  const [originalEventData, setOriginalEventData] =
    useState<EventDetailData | null>(null);
  const [updatedEventData, setUpdatedEventData] = useState<
    Partial<EventDetailData>
  >({});
  const [poster, setPoster] = useState<File | null>(null);
  const [banner, setBanner] = useState<File | null>(null);

  // Fetch the event details on page load
  useEffect(() => {
    const fetchEventData = async () => {
      if (!numericEventId) return;
      try {
        const eventDetails = await fetchEventDetail(numericEventId);
        setOriginalEventData(eventDetails);
      } catch (error) {
        console.error("Error fetching event details:", error);
        alert("행사 정보를 불러오는데 실패했습니다.");
      }
    };

    fetchEventData();
  }, [numericEventId]);

  const handleFormChange = (formData: Partial<EventData>) => {
    setUpdatedEventData((prevData) => ({
      ...prevData,
      ...formData,
    }));
  };

  const handlePosterChange = (selectedImage: File | null) => {
    setPoster(selectedImage);
  };

  const handleBannerChange = (selectedImage: File | null) => {
    setBanner(selectedImage);
  };

  const handleUpdate = async () => {
    if (!originalEventData || numericEventId === null) {
      alert("기존 데이터를 불러오지 못했습니다.");
      return;
    }

    const mergedData = {
      ...updatedEventData, // Updated fields
    };

    const formData = new FormData();
    formData.append(
      "req",
      new Blob([JSON.stringify(mergedData)], { type: "application/json" }),
    );
    if (poster) {
      formData.append("poster", poster);
    }
    if (banner) {
      formData.append("banner", banner);
    }

    try {

      await updateEvent(numericEventId, formData);
      alert("행사가 성공적으로 수정되었습니다.");
      navigate(`/admin/event-detail/${eventId}`); // 수정 후 리스트 페이지로 이동
    } catch (error) {
      console.error("Error updating event:", error);
      alert("행사 수정에 실패했습니다.");
    }
  };

  if (!originalEventData) {
    return <div>로딩 중...</div>;
  }

  return (
    <>
      <h2 className="text-2xl font-bold mb-8">공연 수정</h2>

      <div className="flex gap-8">
        <div className="w-[500px] space-y-6">
          <EventForm
            initialData={originalEventData}
            onChange={handleFormChange}
          />
          <ImageUploader
            title="포스터 업로드"
            aspectRatio={2960 / 3520}
            initialImage={originalEventData.poster} // Initial poster image
            onChange={handlePosterChange}
          />
          <ImageUploader
            title="배너 업로드"
            aspectRatio={1920 / 620}
            initialImage={originalEventData.banner} // Initial banner image
            onChange={handleBannerChange}
          />
        </div>
        <div className="flex-1 space-y-6">
          <EditCalendarWithSchedule
            initialData={originalEventData.eventSchedules}
            onChange={handleFormChange}
          />

          <EditSeatSetting
            stageId={originalEventData.stageId}
            stageImg={originalEventData.stageImg}
            initialData={originalEventData.stageSeats}
            onChange={handleFormChange}
          />
          <TinyEditor
            initialContent={originalEventData.content}
            onChange={(content) => handleFormChange({ content })}
          />
        </div>
      </div>

      <div className="flex justify-end mt-8">
        <button
          onClick={handleUpdate}
          className="bg-blue-500 text-white px-6 py-3 rounded-lg shadow hover:bg-blue-600 transition"
        >
          수정
        </button>
      </div>
    </>
  );
};

export default EditEvent;
