import { useState } from "react";
import { useNavigate } from "react-router-dom";
import PictureBox from "../../components/registerface/PictureBox";
import PolicyAgree from "../../components/registerface/PolicyAgree";
import { unLockSeats } from "../../service/selectseat/api";
import TicketingHeader from "../../components/ticketing/TicketingHeader.tsx";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { useStore } from "zustand";
import { uploadUserFace } from "../../service/uploadFace/api.ts";
import { Helmet } from "react-helmet-async";
import { checkTicketingStatus } from "../../service/queue/api.ts";

function RegisterFace() {
  const navigate = useNavigate();

  const event = useStore(eventDetailStore);
  const eventId = event.eventId;
  const setPersistFaceId = event.setFaceId;
  const setPersistFaceImg = event.setFaceImg;
  const selectedSeats = event.selectedSeats;
  const setSelectedSeats = event.setSelectedSeats;
  const eventScheduleId = event.scheduleId;

  const [faceImg, setFaceImg] = useState<File | null>(null);
  const [allAgreed, setAllAgreed] = useState<boolean>(false); // 약관 동의 상태

  const handleBeforeStep = async () => {
    try {
      const payload = {
        eventScheduleId: eventScheduleId,
        seatMappingIds: selectedSeats.map((seat) => seat.seatMappingId),
      };

      await unLockSeats(payload); // 좌석 선점 해제 API 호출

      setSelectedSeats([]);
      setPersistFaceId(0);
      setPersistFaceImg("");

      const isInTicketing = await checkTicketingStatus(eventId);

      if (!isInTicketing) {
        alert("예매 가능 시간이 만료되었습니다. 다시 대기열에 진입해주세요.");
        navigate(`/queues/${eventId}`);
        return;
      }

      navigate(`/ticketing/select-seat`);
    } catch (error) {
      console.error("Error unlocking seats:", error);
      alert("좌석 선점 해제에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const handleNextStep = async () => {
    if (!allAgreed) {
      alert("모든 항목에 동의해야 합니다.");
      return;
    }
    if (!faceImg) {
      alert("이미지를 업로드해야 합니다.");
      return;
    }

    const isInTicketing = await checkTicketingStatus(eventId);

    if (!isInTicketing) {
      alert("예매 가능 시간이 만료되었습니다. 다시 대기열에 진입해주세요.");
      navigate(`/queues/${eventId}`);
      return;
    }

    try {
      const response = await uploadUserFace(faceImg, eventScheduleId);

      const { faceId, faceUrl } = response.data as {
        faceId: number;
        faceUrl: string;
      };

      setPersistFaceId(faceId);
      setPersistFaceImg(faceUrl);

      navigate("/ticketing/order");
    } catch (error: any) {
      alert(error.message);
    }
  };

  return (
    <div className="relative w-full h-auto min-h-screen bg-[#F0F0F0]">
      <Helmet>
        <title>티켓팅 - 얼굴 인식</title>
      </Helmet>
      {/* 상단 바 */}
      <div className="relative z-10 md:h-[192px] sm:bg-black">
        <TicketingHeader step={3} />
      </div>

      {/* 약관 및 이미지 박스 */}
      <div className="relative sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-start space-y-4 sm:space-y-0 sm:space-x-8 px-4 z-10">
        {/* PictureBox */}
        <div className="flex-1 max-w-[400px]">
          <PictureBox onChange={(selectedImage) => setFaceImg(selectedImage)} />
        </div>

        {/* PolicyAgree */}
        <div className="flex-1 max-w-[400px]">
          <PolicyAgree onAgreeChange={(value) => setAllAgreed(value)} />
        </div>
      </div>

      {/* 하단 버튼 */}
      <div className="w-full px-4 sm:px-8 py-3 flex justify-between items-center border-gray-300 z-10">
        <button
          className="bg-[#666666] w-[45%] sm:w-auto px-4 py-2 text-white border border-black text-sm"
          onClick={handleBeforeStep}
        >
          이전단계
        </button>
        <button
          className="bg-[#CF1212] w-[45%] sm:w-auto px-4 py-2 text-white border border-black text-sm"
          onClick={handleNextStep}
        >
          다음단계
        </button>
      </div>
    </div>
  );
}

export default RegisterFace;
