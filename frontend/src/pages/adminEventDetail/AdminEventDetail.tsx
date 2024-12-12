import { useState } from "react";
import EventStatistics from "../../components/adminEventDetail/EventStatistics.tsx";
import EditEvent from "../../components/adminEventDetail/EditEvent.tsx";
import TemporaryUrlModal from "../../components/adminEventDetail/TemporaryUrlModal.tsx";
import Sidebar from "../../components/@common/Sidebar.tsx";
import { useParams, useSearchParams } from "react-router-dom";
import NotFound from "../errorpage/NotFound.tsx";
import { deleteEvent } from "../../service/admineventlist/api.ts";
import AdminEventInfo from "../../components/adminEventDetail/AdminEventInfo.tsx";

const AdminEventDetail = () => {
  const [isModalOpen, setModalOpen] = useState(false);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = searchParams.get("tab") || "info"; // 기본값 'info'

  const { eventId } = useParams<{ eventId: string }>();

  // 숫자만 허용
  if (!eventId || isNaN(Number(eventId))) {
    return <NotFound />;
  }

  const handleTabChange = (tab: string) => {
    setSearchParams({ tab }); // URL 쿼리 파라미터 업데이트
  };

  const openUrlModal = () => setModalOpen(true);
  const closeUrlModal = () => setModalOpen(false);

  const openDeleteModal = () => setDeleteModalOpen(true);
  const closeDeleteModal = () => setDeleteModalOpen(false);

  const handleDeleteEvent = async (eventId: string) => {
    try {
      await deleteEvent(eventId);
    } catch (error: any) {
      alert(error.message);
    }
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* 사이드바 */}
      <div className="w-64 h-full">
        <Sidebar currentStep={"performance"} />
      </div>

      {/* 메인 컨텐츠 */}
      <div className="flex-1 p-5">
        {/* 버튼 그룹 */}
        <div className="flex justify-end space-x-4 mb-5">
          <button
            onClick={openUrlModal}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            임시 URL 발급
          </button>
          <button
            onClick={() => handleTabChange("info")}
            className={`px-4 py-2 rounded text-white ${
              activeTab === "info"
                ? "bg-blue-600"
                : "bg-blue-500 hover:bg-blue-600"
            }`}
          >
            공연 정보
          </button>
          <button
            onClick={() => handleTabChange("monitoring")}
            className={`px-4 py-2 rounded text-white ${
              activeTab === "monitoring"
                ? "bg-blue-600"
                : "bg-blue-500 hover:bg-blue-600"
            }`}
          >
            공연 통계
          </button>
          <button
            onClick={() => handleTabChange("edit")}
            className={`px-4 py-2 rounded text-white ${
              activeTab === "edit"
                ? "bg-blue-600"
                : "bg-blue-500 hover:bg-blue-600"
            }`}
          >
            수정
          </button>
          <button
            onClick={openDeleteModal}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            삭제
          </button>
        </div>

        {/* 콘텐츠 영역 */}
        <div className="bg-white p-6 rounded shadow">
          {activeTab === "monitoring" && (
            <div className="bg-white w-full rounded">
              <EventStatistics eventId={eventId} />
            </div>
          )}
          {activeTab === "info" && (
            <div>
              <h2 className="text-xl font-bold mb-4">공연 정보</h2>
              <AdminEventInfo eventId={eventId} />
            </div>
          )}
          {activeTab === "edit" && (
            <div className="bg-white w-full rounded">
              <EditEvent eventId={eventId} />
            </div>
          )}
        </div>
      </div>

      {/* 임시 URL 모달 */}
      <TemporaryUrlModal
        isOpen={isModalOpen}
        onClose={closeUrlModal}
        eventId={eventId!}
      />

      {/* 삭제 모달 */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-white p-6 rounded shadow">
            <h2 className="text-lg font-bold mb-4">삭제 확인</h2>
            <p>정말 이 공연 정보를 삭제하시겠습니까?</p>
            <div className="flex justify-end space-x-4 mt-6">
              <button
                onClick={() => {
                  handleDeleteEvent(eventId).then(() => {
                    alert("삭제되었습니다!");
                    closeDeleteModal();
                  });
                }}
                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
              >
                삭제
              </button>
              <button
                onClick={closeDeleteModal}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded hover:bg-gray-400"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminEventDetail;
