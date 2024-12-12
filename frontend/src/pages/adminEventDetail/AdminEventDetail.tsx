import { useState } from 'react';
import EventStatistics from '../../components/adminEventDetail/EventStatistics.tsx';
import EditEvent from '../../components/adminEventDetail/EditEvent.tsx';
import TemporaryUrlModal from '../../components/adminEventDetail/TemporaryUrlModal.tsx';
import Sidebar from '../../components/@common/Sidebar.tsx';
import { useParams, useSearchParams } from 'react-router-dom';
import NotFound from '../errorpage/NotFound.tsx';
import { deleteEvent } from '../../service/admineventlist/api.ts';
import AdminEventInfo from '../../components/adminEventDetail/AdminEventInfo.tsx';

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

  const handleDeleteEvent = async (eventId: string) => {
    try {
      await deleteEvent(eventId);
    } catch (error: any) {
      alert(error.message);
    }
  };

  return (
    <div
      style={{ display: 'flex', height: '100vh', backgroundColor: '#F0F2F5' }}
    >
      {/* 사이드바 */}
      <div style={{ width: '250px', height: '100%' }}>
        <Sidebar currentStep={'performance'} />
      </div>

      {/* 메인 컨텐츠 */}
      <div style={{ flex: 1, padding: '20px' }}>
        {/* 버튼 그룹 */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: '10px',
            marginBottom: '20px',
          }}
        >
          <button
            onClick={openUrlModal}
            style={{
              padding: '10px 15px',
              backgroundColor: '#007BFF',
              color: '#FFF',
              borderRadius: '5px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            임시 URL 발급
          </button>
          <button
            onClick={() => handleTabChange('info')}
            style={{
              padding: '10px 15px',
              backgroundColor: activeTab === 'info' ? '#0056b3' : '#007BFF',
              color: '#FFF',
              borderRadius: '5px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            공연 정보
          </button>
          <button
            onClick={() => handleTabChange('monitoring')}
            style={{
              padding: '10px 15px',
              backgroundColor:
                activeTab === 'monitoring' ? '#0056b3' : '#007BFF',
              color: '#FFF',
              borderRadius: '5px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            공연 통계
          </button>
          <button
            onClick={() => handleTabChange('edit')}
            style={{
              padding: '10px 15px',
              backgroundColor: activeTab === 'edit' ? '#0056b3' : '#007BFF',
              color: '#FFF',
              borderRadius: '5px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            수정
          </button>
          <button
            onClick={openDeleteModal}
            style={{
              padding: '10px 15px',
              backgroundColor: '#007BFF',
              color: '#FFF',
              borderRadius: '5px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            삭제
          </button>
        </div>

        {/* 콘텐츠 영역 */}
        <div
          style={{
            backgroundColor: '#FFF',
            padding: '20px',
            borderRadius: '10px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
          }}
        >
          {activeTab === 'monitoring' && (
            <div
              style={{
                backgroundColor: '#FFF',
                width: '100%',
                borderRadius: '10px',
              }}
            >
              <EventStatistics eventId={eventId} />
            </div>
          )}
          {activeTab === 'info' && (
            <div>
              <h2
                style={{
                  fontSize: '1.25rem',
                  fontWeight: 'bold',
                  marginBottom: '10px',
                }}
              >
                공연 정보
              </h2>
              <AdminEventInfo eventId={eventId} />
            </div>
          )}
          {activeTab === 'edit' && (
            <div
              style={{
                backgroundColor: '#FFF',
                width: '100%',
                borderRadius: '10px',
              }}
            >
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
        <div
          style={{
            position: 'fixed',
            inset: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            zIndex: 50,
          }}
        >
          <div
            style={{
              backgroundColor: '#FFF',
              padding: '20px',
              borderRadius: '10px',
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
            }}
          >
            <h2
              style={{
                fontSize: '1.25rem',
                fontWeight: 'bold',
                marginBottom: '10px',
              }}
            >
              삭제 확인
            </h2>
            <p>정말 이 공연 정보를 삭제하시겠습니까?</p>
            <div
              style={{
                display: 'flex',
                justifyContent: 'flex-end',
                gap: '10px',
                marginTop: '20px',
              }}
            >
              <button
                onClick={() => {
                  handleDeleteEvent(eventId).then(() => {
                    alert('삭제되었습니다!');
                    closeDeleteModal();
                  });
                }}
                style={{
                  padding: '10px 15px',
                  backgroundColor: '#FF0000',
                  color: '#FFF',
                  borderRadius: '5px',
                  border: 'none',
                  cursor: 'pointer',
                }}
              >
                삭제
              </button>
              <button
                onClick={closeDeleteModal}
                style={{
                  padding: '10px 15px',
                  backgroundColor: '#D3D3D3',
                  color: '#000',
                  borderRadius: '5px',
                  border: 'none',
                  cursor: 'pointer',
                }}
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
