import {
  FaTheaterMasks,
  FaUserFriends,
  FaRegFileAlt,
  FaSignOutAlt,
} from "react-icons/fa";
import logo from "../../assets/logo.png";
import { adminStore } from "../../stores/AdminStore.tsx";
import { adminLogout } from "../../service/admin/admin.ts";
import { useStore } from "zustand";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";

function Sidebar({ currentStep }: { currentStep: string }) {
  const { adminName, isLogin, reset } = useStore(adminStore); // reset 메서드 가져오기
  const navigate = useNavigate();

  const menuItems = [
    {
      key: "performance",
      label: "공연관리",
      icon: <FaTheaterMasks />,
      path: "/admin/event-list",
    },
    {
      key: "customers",
      label: "고객관리",
      icon: <FaUserFriends />,
      path: "/customers",
    },
    {
      key: "finance",
      label: "정산관리",
      icon: <FaRegFileAlt />,
      path: "/finance",
    },
  ];

  const logout = async () => {
    await adminLogout(
      (response) => {
        if (response.status === 204) {
          // adminStore 상태 초기화 및 로컬스토리지 삭제
          reset();
          localStorage.removeItem("ADMIN_STORE");

          // 로그인 페이지로 이동
          alert("Logout success!");
          navigate("/admin/login");
        }
      },
      () => {
        alert("Logout failed: An unexpected error occurred.");
      },
    );
  };

  return (
    <div className="h-full p-6 flex flex-col items-center bg-[#5E6770] text-white">
      {/* 로고 및 제목 */}
      <div className="mb-10 flex items-center justify-center w-full">
        <div className="flex items-center justify-center">
          <img src={logo} alt="Logo" className="w-8 h-8 mr-2" />
          <h1 className="text-xl font-bold whitespace-nowrap">
            Ficket Manager
          </h1>
        </div>
      </div>

      {/* 프로필 섹션 */}
      <div className="flex flex-col items-center mb-12">
        <div className="w-20 h-20 rounded-full bg-orange-400 flex items-center justify-center mb-4">
          <span className="text-4xl font-semibold text-white">
            {adminName[0]}
          </span>
        </div>
        <h2 className="text-lg font-semibold">{adminName}</h2>
        <span className="text-sm text-gray-300">Manager</span>
      </div>

      {/* 네비게이션 메뉴 */}
      <nav className="flex flex-col space-y-6 w-full">
        {menuItems.map((item) => (
          <Link
            key={item.key}
            to={item.path}
            className={`flex items-center w-full px-4 py-2 text-base font-medium rounded-lg transition-colors ${
              currentStep === item.key
                ? "bg-gray-600 text-white"
                : "hover:bg-gray-600"
            }`}
          >
            {item.icon}
            <span className="ml-3">{item.label}</span>
            {currentStep === item.key && (
              <span className="ml-auto text-green-400 font-bold">✔</span>
            )}
          </Link>
        ))}
      </nav>

      {/* 로그아웃 버튼 */}
      {isLogin && (
        <button
          onClick={logout}
          className="absolute bottom-6 left-[130px] flex items-center px-4 py-2 text-base font-medium text-white bg-[#5E6770] rounded-lg hover:bg-gray-600 transition-colors"
        >
          <FaSignOutAlt className="mr-3" />
          Logout
        </button>
      )}
    </div>
  );
}

export default Sidebar;
