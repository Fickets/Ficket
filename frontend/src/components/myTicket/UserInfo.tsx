import { useState } from "react";
import DeleteUserModal from "./DeleteUserModal";
import EditUserModal from "./EditUserModal";
import { deleteUser } from "../../service/myTicket/api.ts";
import { useStore } from "zustand";
import { userStore } from "../../stores/UserStore.tsx";
import { useNavigate } from "react-router-dom";

const UserInfo = () => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const user = useStore(userStore);
  const navi = useNavigate();
  const handleOpenDeleteModal = () => {
    setShowDeleteModal(true);
  };

  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
  };

  const handleConfirmDelete = async () => {
    try {
      await deleteUser();
      alert("회원 탈퇴가 완료되었습니다.");
      user.resetState();
      setShowDeleteModal(false);
      navi("/");
    } catch (error: any) {
      if (error.status === 409) {
        alert("모든 예약 티켓에 대한 환불을 진행해 주세요.");
        setShowDeleteModal(false);
      }
    }
  };

  const handleOpenEditModal = () => {
    setShowEditModal(true);
  };

  const handleCloseEditModal = () => {
    setShowEditModal(false);
  };

  return (
    <div className="flex items-center justify-between mx-[300px] mt-[50px] border-b pb-4 hidden sm:flex">
      {/* Left Section: User Info */}
      <div className="flex flex-col space-y-3">
        <div className="flex items-center">
          <span className="font-medium w-[80px] text-gray-600">이름</span>
          <span className="text-gray-800">{user.userName}</span>
        </div>
        <div className="flex items-center">
          <span className="font-medium w-[80px] text-gray-600">생년</span>
          <span className="text-gray-800">{user.birth}</span>
        </div>
        <div className="flex items-center">
          <span className="font-medium w-[80px] text-gray-600">성별</span>
          <span className="text-gray-800">
            {user.gender === "MALE" ? "남성" : "여성"}
          </span>
        </div>
      </div>

      {/* Right Section: Buttons */}
      <div className="flex space-x-3 -mt-[70px]">
        <button
          onClick={handleOpenEditModal}
          className="px-3 py-1.5 text-sm text-white bg-purple-500 rounded-md hover:bg-purple-600"
        >
          회원 정보 수정
        </button>
        <button
          onClick={handleOpenDeleteModal}
          className="px-3 py-1.5 text-sm text-white bg-red-500 rounded-md hover:bg-red-600"
        >
          회원 탈퇴
        </button>
      </div>

      {/* DeleteUserModal */}
      {showDeleteModal && (
        <DeleteUserModal
          onClose={handleCloseDeleteModal}
          onConfirm={handleConfirmDelete}
        />
      )}

      {/* EditUserModal */}
      {showEditModal && <EditUserModal onClose={handleCloseEditModal} />}
    </div>
  );
};

export default UserInfo;
