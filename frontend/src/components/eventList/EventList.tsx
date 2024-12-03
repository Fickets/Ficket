import { FaList } from "react-icons/fa";
import { useNavigate } from "react-router-dom";

const EventList = () => {
  const navigate = useNavigate();
  return (
    <div className="w-full bg-white rounded-lg shadow-md p-6 border border-gray-200">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-4">
        {/* 왼쪽: 제목 */}
        <div className="flex items-center">
          <FaList className="text-xl" />
          <span className="ml-2 text-lg font-semibold">공연 목록</span>
        </div>

        {/* 오른쪽: 버튼 */}
        <button
          type="button"
          onClick={() => navigate("/admin/register-event")}
          className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 text-sm"
        >
          신규
        </button>
      </div>

      <hr className="mb-6 border-gray-300" />
    </div>
  );
};

export default EventList;
