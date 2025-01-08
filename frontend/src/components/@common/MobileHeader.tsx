import { AiOutlineArrowLeft } from "react-icons/ai";
import { useNavigate } from "react-router-dom";

interface MobileHeaderProps {
  title: string; // 헤더 제목
}

const MobileHeader = ({ title }: MobileHeaderProps) => {
  const navigate = useNavigate();

  return (
    <div className="fixed top-0 left-0 right-0 bg-white flex items-center justify-between px-4 max-w-[400px] h-[50px] border-b border-gray-300 z-50">
      {/* 왼쪽 영역 */}
      <div className="flex items-center">
        <button onClick={() => navigate(-1)} className="text-black text-xl">
          <AiOutlineArrowLeft />
        </button>
      </div>

      {/* 가운데 제목 */}
      <h1 className="text-lg font-medium absolute left-1/2 transform -translate-x-1/2">
        {title}
      </h1>

      {/* 오른쪽 영역 (빈 공간) */}
      <div className="w-6"></div>
    </div>
  );
};

export default MobileHeader;
