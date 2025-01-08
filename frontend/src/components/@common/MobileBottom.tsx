import { useNavigate, useLocation } from "react-router-dom";
import { useCookies } from "react-cookie";
import { userLogout } from "../../service/user/userApi";
import userImg from "../../assets/bottomNav/User.png";
import ticketImg from "../../assets/bottomNav/Ticket.png";
import homeImg from "../../assets/bottomNav/Home.png";
import searchImg from "../../assets/bottomNav/Search.png";

const BottomNav = () => {
  const [cookies] = useCookies(["isLogin"]);
  const navi = useNavigate();
  const location = useLocation(); // 현재 경로 가져오기

  const handleSearchNavigation = () => {
    const searchPath = "/contents/search?keyword=null"; // 검색 페이지 경로
    if (location.pathname + location.search !== searchPath) {
      navi(searchPath); // 중복 방지: 현재 경로와 다른 경우만 이동
    }
  };

  const handleLoginToggle = async () => {
    if (Boolean(cookies.isLogin)) {
      await userLogout(
        (response) => {
          console.log("LOGOUT");
          user.resetState();
          navi("/");
        },
        () => {},
      );
    } else {
      navi("/users/login");
    }
  };

  return (
    <div className="block md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-300 z-50">
      <div className="w-full max-w-[400px] mx-auto flex justify-around items-center py-2">
        {/* 홈화면 */}
        <div
          className="flex flex-col items-center text-sm text-gray-700"
          onClick={() => navi("/")}
        >
          <img src={homeImg} alt="홈화면" className="w-8 h-8" />
          <p>홈화면</p>
        </div>

        <div
          className="flex flex-col items-center text-sm text-gray-700 mt-1"
          onClick={handleSearchNavigation} // 검색 페이지로 이동
        >
          <img src={searchImg} alt="검색" className="w-7 h-7" />
          <p>검색</p>
        </div>

        {/* 마이티켓 */}
        <div
          className="flex flex-col items-center text-sm text-gray-700"
          onClick={() => navi("/my-ticket")}
        >
          <img src={ticketImg} alt="마이티켓" className="w-8 h-8" />
          <p>마이티켓</p>
        </div>

        {/* 로그인/로그아웃 */}
        <div
          className="flex flex-col items-center text-sm text-gray-700"
          onClick={handleLoginToggle}
        >
          <img src={userImg} alt="유저" className="w-8 h-8" />
          {Boolean(cookies.isLogin) ? <p>로그아웃</p> : <p>로그인</p>}
        </div>
      </div>
    </div>
  );
};

export default BottomNav;
