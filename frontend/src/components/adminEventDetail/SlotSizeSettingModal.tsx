import { useState } from "react";
import { initializeSlotSize } from "../../service/admineventlist/api.ts";

const SlotSizeSettingModal = ({
  isOpen,
  onClose,
  eventId,
}: {
  isOpen: boolean;
  onClose: () => void;
  eventId: string;
}) => {
  const [newSlotSize, setNewSlotSize] = useState<number | "">("");

  if (!isOpen) return null; // 모달이 닫혀 있으면 렌더링하지 않음

  const handleSetSlotSize = async () => {
    if (typeof newSlotSize !== "number" || newSlotSize <= 0) {
      alert("유효한 슬롯 크기를 입력해주세요.");
      return;
    }

    try {
      const response = await initializeSlotSize(eventId, newSlotSize);
      if (response) {
        alert("슬롯이 성공적으로 설정되었습니다.");
        onClose(); // 모달 닫기
      } else {
        alert("슬롯 설정에 실패했습니다. 다시 시도해주세요.");
      }
    } catch (error) {
      console.error(error);
      alert("오류가 발생했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      <div className="bg-white rounded-lg shadow-lg w-[680px]">
        {/* 헤더 */}
        <div className="flex justify-between items-center border-b px-4 py-3">
          <h2 className="text-lg font-bold">슬롯 크기 설정</h2>
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
          {/* 안내 사항 */}
          <div className="mb-4">
            <h3 className="text-sm font-bold mb-1">안내 사항:</h3>
            <ul className="text-sm text-gray-600 list-disc list-inside">
              <li>슬롯 크기는 공연의 티켓을 관리하는 데 필요한 값입니다.</li>
              <li>슬롯 크기 변경 시, 기존 설정이 초기화될 수 있습니다.</li>
              <li>
                설정된 슬롯 크기는 공연의 예약 가능한 최대 수를 제한합니다.
              </li>
            </ul>
          </div>

          {/* 주의 사항 */}
          <div className="mb-4">
            <h3 className="text-sm font-bold mb-1">주의 사항:</h3>
            <ul className="text-sm text-gray-600 list-disc list-inside">
              <li>한 번 설정된 슬롯 크기는 변경이 불가할 수 있습니다.</li>
              <li>슬롯 크기를 설정하기 전에 정확한 값을 입력해주세요.</li>
              <li>
                공연의 예약 시스템에 영향을 미칠 수 있으므로 신중하게 설정해야
                합니다.
              </li>
            </ul>
          </div>

          {/* 슬롯 크기 입력 */}
          <div className="mb-4">
            <label className="block text-sm font-bold mb-2" htmlFor="slotSize">
              슬롯 크기
            </label>
            <input
              id="slotSize"
              type="number"
              value={newSlotSize}
              onChange={(e) =>
                setNewSlotSize(
                  e.target.value === "" ? "" : Number(e.target.value),
                )
              }
              className="w-full px-4 py-2 border border-gray-300 rounded-md text-sm"
              placeholder="슬롯 크기를 입력하세요"
            />
          </div>
        </div>

        {/* 버튼 */}
        <div className="flex justify-end space-x-2 border-t px-4 py-3">
          <button
            onClick={handleSetSlotSize}
            className="px-4 py-2 text-sm text-white bg-blue-500 rounded-md hover:bg-blue-600"
          >
            슬롯 설정
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

export default SlotSizeSettingModal;
