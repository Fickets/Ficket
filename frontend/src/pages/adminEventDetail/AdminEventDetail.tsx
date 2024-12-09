import { useState } from 'react';
import TemporaryUrlModal from '../../components/adminEventDetail/TemporaryUrlModal.tsx';
import Sidebar from '../../components/@common/Sidebar.tsx';
import { useParams, useSearchParams } from 'react-router-dom';
import NotFound from '../errorpage/NotFound.tsx';
import EditEvent from '../../components/adminEventDetail/EditEvent.tsx';

const AdminEventDetail = () => {
  const [isModalOpen, setModalOpen] = useState(false);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = searchParams.get('tab') || 'info'; // 기본값 'info'

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

  return (
    <div className="flex h-screen bg-[#F0F2F5]">
      {/* 사이드바 */}
      <div className="w-64 bg-[#5E6770] text-white h-full">
        <Sidebar currentStep={'performance'} />
      </div>

      {/* 메인 컨텐츠 */}
      <div className="flex-1 p-6">
        {/* 버튼 그룹 */}
        <div className="flex justify-end space-x-4 mb-6">
          <button
            onClick={openUrlModal}
            className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"
          >
            임시 URL 발급
          </button>
          <button
            onClick={() => handleTabChange('monitoring')}
            className={`px-4 py-2 ${
              activeTab === 'monitoring' ? 'bg-blue-600' : 'bg-blue-500'
            } text-white rounded-md hover:bg-blue-600`}
          >
            모니터링
          </button>
          <button
            onClick={() => handleTabChange('info')}
            className={`px-4 py-2 ${
              activeTab === 'info' ? 'bg-blue-600' : 'bg-blue-500'
            } text-white rounded-md hover:bg-blue-600`}
          >
            공연 정보
          </button>
          <button
            onClick={() => handleTabChange('edit')}
            className={`px-4 py-2 ${
              activeTab === 'edit' ? 'bg-blue-600' : 'bg-blue-500'
            } text-white rounded-md hover:bg-blue-600`}
          >
            수정
          </button>
          <button
            onClick={openDeleteModal}
            className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"
          >
            삭제
          </button>
        </div>

        {/* 콘텐츠 영역 */}
        <div className="bg-white p-6 rounded-md shadow-md">
          {activeTab === 'monitoring' && (
            <div>
              <h2 className="text-xl font-bold mb-4">모니터링</h2>
              <p>공연 모니터링 데이터를 볼 수 있는 화면입니다.</p>
            </div>
          )}
          {activeTab === 'info' && (
            <div>
              <h2 className="text-xl font-bold mb-4">공연 정보</h2>
              <p>공연 세부 정보를 확인할 수 있는 화면입니다.</p>
            </div>
          )}
          {activeTab === 'edit' && (
            <div className="bg-white w-full rounded-xl">
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
          <div className="bg-white p-6 rounded-lg shadow-lg">
            <h2 className="text-lg font-bold mb-4">삭제 확인</h2>
            <p>정말 이 공연 정보를 삭제하시겠습니까?</p>
            <div className="flex justify-end space-x-2 mt-6">
              <button
                onClick={() => {
                  alert('삭제되었습니다!');
                  closeDeleteModal();
                }}
                className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
              >
                삭제
              </button>
              <button
                onClick={closeDeleteModal}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400"
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
