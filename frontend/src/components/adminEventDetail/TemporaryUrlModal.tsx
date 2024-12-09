import { useState } from 'react';
import { generateTemporaryUrl } from '../../service/temporaryUrl/api.ts';

const TemporaryUrlModal = ({
  isOpen,
  onClose,
  eventId,
}: {
  isOpen: boolean;
  onClose: () => void;
  eventId: string;
}) => {
  if (!isOpen) return null; // 모달이 닫혀 있으면 렌더링하지 않음

  const [temporaryUrl, setTemporaryUrl] = useState<string>('');

  const handleGenerateTemporaryUrl = async () => {
    const url = await generateTemporaryUrl(eventId);

    if (url) {
      setTemporaryUrl(url); // URL 상태 저장
      alert('임시 URL이 발급되었습니다!');
    } else {
      alert('URL 발급에 실패했습니다. 다시 시도해주세요.');
    }
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      <div className="bg-white rounded-lg shadow-lg w-[680px]">
        {/* 헤더 */}
        <div className="flex justify-between items-center border-b px-4 py-3">
          <h2 className="text-lg font-bold">티켓 검사 임시 URL 발급</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-black"
            aria-label="닫기"
          >
            ✕
          </button>
        </div>

        {/* 내용 */}
        <div className="p-4">
          <div className="mb-4">
            <h3 className="text-sm font-bold mb-1">안내 사항:</h3>
            <ul className="text-sm text-gray-600 list-disc list-inside">
              <li>이 링크는 특정 공연의 티켓 확인을 위한 임시 URL입니다.</li>
              <li>링크의 유효기간이 지나면 접근이 불가능합니다.</li>
            </ul>
          </div>
          <div className="mb-4">
            <h3 className="text-sm font-bold mb-1">주의 사항:</h3>
            <ul className="text-sm text-gray-600 list-disc list-inside">
              <li>이미 임시 URL이 발급된 상태라면 재발급됩니다.</li>
              <li>
                타인과 링크를 공유하지 마세요. 유출 시, 티켓 확인이 제한될 수
                있습니다.
              </li>
              <li>지정된 시간 내에만 사용 가능하며, 재발급은 불가합니다.</li>
            </ul>
          </div>
          <div className="relative">
            {temporaryUrl ? (
              <a
                href={temporaryUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="block w-full px-4 py-2 border border-gray-300 rounded-md text-sm text-blue-500 hover:underline"
              >
                {temporaryUrl}
              </a>
            ) : (
              <input
                type="text"
                value={temporaryUrl}
                readOnly
                className="w-full px-4 py-2 border border-gray-300 rounded-md text-sm text-gray-800"
              />
            )}
            <button
              onClick={() => navigator.clipboard.writeText(temporaryUrl)}
              className="absolute right-2 top-2 text-gray-500 hover:text-black"
              aria-label="복사"
            >
              📋
            </button>
          </div>
        </div>

        {/* 버튼 */}
        <div className="flex justify-end space-x-2 border-t px-4 py-3">
          <button
            onClick={handleGenerateTemporaryUrl}
            className="px-4 py-2 text-sm text-white bg-blue-500 rounded-md hover:bg-blue-600"
          >
            임시 URL 발급
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
};

export default TemporaryUrlModal;
