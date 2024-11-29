import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import logo from '../../assets/logo.png';
import PictureBox from '../../components/registerface/PictureBox';
import PolicyAgree from '../../components/registerface/PolicyAgree';

function RegisterFace() {
  const navigate = useNavigate();
  const [allAgreed, setAllAgreed] = useState<boolean>(false);
  const [faceImg, setFaceImg] = useState<File | null>(null);
  const location = useLocation();
  const { eventScheduleId } = location.state || {}; // 데이터 가져오기

  const handleNextStep = () => {
    if (allAgreed && faceImg) {
      navigate('/ticketing/order', {
        state: {
          faceImg, // 이미지 정보
          event_schedule_id: 7, // event_schedule_id 값
        },
      });
    } else {
      alert('모든 항목에 동의하고 이미지를 업로드해야 합니다.');
    }
  };

  return (
    <div className="relative w-full h-auto min-h-screen bg-[#F0F0F0]">
      {/* 상단 바 */}
      <div className="relative z-10 h-[192px] bg-black text-white">
        {/* 데스크탑: 로고와 텍스트 */}
        <div className="hidden sm:flex items-center justify-between px-4 sm:px-8 py-3">
          <div className="flex items-center">
            <img
              src={logo}
              alt="Logo"
              className="w-8 h-8 sm:w-10 sm:h-10 mr-2"
            />
            <h3 className="text-white text-sm sm:text-lg font-semibold">
              Ficket 티켓예매
            </h3>
          </div>
        </div>

        {/* 데스크탑: 기존 단계 표시 */}
        <div className="hidden sm:flex justify-center py-4 -mt-4">
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#D9D9D9] border border-black font-bold flex items-center justify-center text-xs sm:text-base">
            <span>01 관람일 / 회차선택</span>
          </div>
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#D9D9D9] border border-black font-bold flex items-center justify-center text-xs sm:text-base">
            <span>02 좌석 선택</span>
          </div>
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#E94343] border border-black text-white font-bold flex items-center justify-center text-xs sm:text-base">
            <span>03 얼굴 인식</span>
          </div>
          <div className="w-[100px] h-[40px] sm:w-[210px] sm:h-[50px] bg-[#D9D9D9] border border-black font-bold flex items-center justify-center text-xs sm:text-base">
            <span>04 결제하기</span>
          </div>
        </div>

        {/* 모바일: 동그라미 네비게이션 */}
        <div className="flex sm:hidden justify-center py-4">
          <div className="flex space-x-4">
            <div className="w-4 h-4 bg-gray-500 rounded-full"></div>{' '}
            {/* Step 1 */}
            <div className="w-4 h-4 bg-gray-500 rounded-full"></div>{' '}
            {/* Step 2 */}
            <div className="w-4 h-4 bg-white rounded-full border border-gray-500"></div>{' '}
            {/* Step 3 */}
            <div className="w-4 h-4 bg-gray-500 rounded-full"></div>{' '}
            {/* Step 4 */}
          </div>
        </div>
      </div>

      {/* 약관 및 이미지 박스 */}
      <div className="relative -mt-8 sm:-mt-[60px] flex flex-col sm:flex-row justify-center items-start space-y-4 sm:space-y-0 sm:space-x-8 px-4 z-10">
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
      <div className="w-full px-4 sm:px-8 py-3 mb-8 flex justify-between items-center border-gray-300 z-10">
        <button
          className="bg-[#666666] w-[45%] sm:w-auto px-4 py-2 text-white border border-black text-sm"
          onClick={() => navigate(`/ticketing/select-seat/${eventScheduleId}`)}
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
