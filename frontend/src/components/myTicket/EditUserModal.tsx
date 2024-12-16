import { useState } from "react";
import { useStore } from "zustand";
import { userStore } from "../../stores/UserStore";
import { updateUser } from "../../service/myTicket/api.ts";

interface EditUserModalProps {
  onClose: () => void;
}

const EditUserModal = ({ onClose }: EditUserModalProps) => {
  const { userId, userName, birth, gender, setUserName, setBirth, setGender } =
    useStore(userStore);

  const [updatedUserName, setUpdatedUserName] = useState<string>(userName);
  const [updatedBirth, setUpdatedBirth] = useState<number>(birth);
  const [updatedGender, setUpdatedGender] = useState<string>(gender);

  const handleSave = async () => {
    // JSON 데이터 준비
    const requestData = {
      userId: userId,
      userName: updatedUserName,
      birth: updatedBirth,
      gender: updatedGender,
    };
    console.log(requestData) 
    try {
      // API 요청
      const response = await updateUser(requestData);

      if (response === 204) {
        // 상태 업데이트
        setUserName(updatedUserName);
        setBirth(updatedBirth);
        setGender(updatedGender);

        alert("회원 정보가 수정되었습니다.");
        onClose();
      } else {
        alert("회원 정보 수정에 실패했습니다.");
      }
    } catch (error) {
      console.error("회원 정보 수정 요청 실패:", error);
      alert("서버와의 통신 중 오류가 발생했습니다.");
    }
  };

  const currentYear = new Date().getFullYear();
  const startYear = currentYear - 100; // 100년 전부터 시작
  const years: number[] = [];
  for (let year = currentYear; year >= startYear; year--) {
    years.push(year);
  }

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      <div className="bg-white rounded-md shadow-lg w-[400px] max-w-full">
        {/* Header */}
        <div className="flex justify-between items-center border-b px-6 py-4 bg-gray-50 rounded-t-md">
          <h2 className="text-lg font-bold text-gray-800">회원 정보 수정</h2>
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
          {/* Name Input */}
          <div className="mb-4">
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              이름
            </label>
            <input
              type="text"
              value={updatedUserName}
              onChange={(e) => setUpdatedUserName(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-md text-sm"
            />
          </div>

          {/* Birth Year Select */}
          <div className="mb-4 relative">
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              생년
            </label>
            <select
              value={updatedBirth}
              onChange={(e) => setUpdatedBirth(Number(e.target.value))}
              className="w-full px-4 py-2 border border-gray-300 rounded-md text-sm z-10 relative bg-white"
            >
              {years.map((year) => (
                <option key={year} value={year}>
                  {year}
                </option>
              ))}
            </select>
          </div>

          {/* Gender Radio Buttons */}
          <div className="mb-4">
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              성별
            </label>
            <div className="flex items-center space-x-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  name="gender"
                  value="MALE"
                  checked={updatedGender === "MALE"}
                  onChange={(e) => setUpdatedGender(e.target.value)}
                  className="form-radio text-blue-500 focus:ring-blue-500"
                />
                <span className="ml-2 text-sm text-gray-700">남자</span>
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  name="gender"
                  value="FEMALE"
                  checked={updatedGender === "FEMALE"}
                  onChange={(e) => setUpdatedGender(e.target.value)}
                  className="form-radio text-blue-500 focus:ring-blue-500"
                />
                <span className="ml-2 text-sm text-gray-700">여자</span>
              </label>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-2 border-t px-6 py-4 bg-gray-50 rounded-b-md">
          <button
            onClick={handleSave}
            className="px-4 py-2 text-sm text-white bg-purple-500 rounded-md hover:bg-purple-600"
          >
            수정
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

export default EditUserModal;
