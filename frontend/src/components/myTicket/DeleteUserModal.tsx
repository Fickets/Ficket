interface DeleteUserModalProps {
  onClose: () => void;
  onConfirm: () => void;
}

const DeleteUserModal = ({ onClose, onConfirm }: DeleteUserModalProps) => (
  <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
    {/* 모달 컨테이너 */}
    <div className="bg-white rounded-md shadow-lg w-[500px] max-w-full">
      {/* Header */}
      <div className="flex justify-between items-center border-b px-6 py-4 bg-gray-50 rounded-t-md">
        <h2 className="text-lg font-bold text-gray-800">회원 탈퇴</h2>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600"
          aria-label="닫기"
        >
          ✕
        </button>
      </div>

      {/* Content */}
      <div className="p-6">
        <h3 className="text-sm font-semibold text-gray-700 mb-4">
          회원 탈퇴 전에 꼭 확인해 주세요
        </h3>
        <ol className="text-sm text-gray-600 list-decimal list-inside space-y-2">
          <li>
            데이터 삭제: 회원 탈퇴 시, 회원님의 개인정보와 서비스 이용 기록이
            모두 삭제됩니다.
          </li>
          <li>
            탈퇴 후 계정 복구 불가: 탈퇴 후에는 계정 복구가 불가하며, 재가입 시
            새 계정으로 이용하셔야 됩니다.
          </li>
        </ol>
      </div>

      {/* Actions */}
      <div className="flex justify-end space-x-2 border-t px-6 py-4 bg-gray-50 rounded-b-md">
        <button
          onClick={onConfirm}
          className="px-4 py-2 text-sm text-white bg-red-500 rounded-md hover:bg-red-600"
        >
          탈퇴
        </button>
        <button
          onClick={onClose}
          className="px-4 py-2 text-sm text-gray-500 bg-gray-100 rounded-md hover:bg-gray-200"
        >
          취소
        </button>
      </div>
    </div>
  </div>
);

export default DeleteUserModal;
