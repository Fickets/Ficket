import { useState, useEffect } from "react";
import {
  generateTemporaryUrl,
  checkTemporaryUrl,
} from "../../service/temporaryUrl/api.ts";

const TemporaryUrlModal = ({
  isOpen,
  onClose,
  eventId,
}: {
  isOpen: boolean;
  onClose: () => void;
  eventId: string;
}) => {
  const [temporaryUrl, setTemporaryUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  // 모달이 열릴 때 기존 URL 체크
  useEffect(() => {
    const fetchTemporaryUrl = async () => {
      if (!isOpen) return;

      setLoading(true);
      try {
        const existingUrl = await checkTemporaryUrl(eventId);
        if (existingUrl) {
          const userConfirmed = window.confirm(
            "이미 임시 URL이 존재합니다.\n\n✅ 기존 URL 사용: [취소] 버튼\n🔄 새로 발급: [확인] 버튼",
          );

          if (!userConfirmed) {
            setTemporaryUrl(existingUrl); // 기존 URL 유지
            return;
          }
        }
      } catch (error) {
        console.error("임시 URL 확인 중 오류 발생:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchTemporaryUrl();
  }, [isOpen, eventId]);

  // 임시 URL 발급
  const handleGenerateTemporaryUrl = async () => {
    try {
      setLoading(true);
      const newUrl = await generateTemporaryUrl(eventId);
      setTemporaryUrl(newUrl);
      alert("임시 URL이 발급되었습니다!");
    } catch (error) {
      console.error("임시 URL 발급 중 오류 발생:", error);
      alert("오류가 발생했습니다. 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className={`fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50 ${
        isOpen ? "" : "hidden"
      }`}
    >
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
            {loading ? (
              <p className="text-gray-500 text-sm">⏳ URL 확인 중...</p>
            ) : temporaryUrl ? (
              <a
                href={temporaryUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="block w-full px-4 py-2 border border-gray-300 rounded-md text-sm text-blue-500 hover:underline"
              >
                {temporaryUrl}
              </a>
            ) : (
              <p className="text-gray-500 text-sm">
                ❌ 발급된 임시 URL이 없습니다.
              </p>
            )}
            {temporaryUrl && (
              <button
                onClick={() => {
                  navigator.clipboard.writeText(temporaryUrl);
                  alert("URL이 클립보드에 복사되었습니다!");
                }}
                className="absolute right-2 top-2 text-gray-500 hover:text-black"
                aria-label="복사"
              >
                📋
              </button>
            )}
          </div>
        </div>

        {/* 버튼 */}
        <div className="flex justify-end space-x-2 border-t px-4 py-3">
          <button
            onClick={handleGenerateTemporaryUrl}
            className="px-4 py-2 text-sm text-white bg-blue-500 rounded-md hover:bg-blue-600"
          >
            {temporaryUrl ? "새로 발급" : "임시 URL 발급"}
          </button>
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm text-gray-500 bg-gray-100 rounded-md hover:bg-gray-200"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
};

export default TemporaryUrlModal;
