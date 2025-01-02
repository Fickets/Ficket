import { useNavigate } from "react-router-dom";
import { useCookies } from "react-cookie";
import { userLogout } from "../../service/user/userApi";
import userImg from "../../assets/bottomNav/User.png";
import ticketImg from "../../assets/bottomNav/Ticket.png";
import homeImg from "../../assets/bottomNav/Home.png";

const BottomNav = () => {
  const [cookies] = useCookies(["isLogin"]);
  const navi = useNavigate();

  const handleLoginToggle = async () => {
    if (Boolean(cookies.isLogin)) {
      await userLogout(
        (response) => {
          console.log("LOGOUT");
          user.resetState();
          navi("/");
        },
        () => { },
      );
    } else {
      navi("/users/login");
    }
  };

  return (
    <div className="block md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-300 z-50">
      <div className="w-full max-w-[400px] mx-auto flex justify-around items-center py-2">
        {/* 마이티켓 */}
        <div
          className="flex flex-col items-center text-sm text-gray-700"
          onClick={() => navi("/my-ticket")}
        >
          <img src={ticketImg} alt="마이티켓" className="w-8 h-8" />
          <p>마이티켓</p>
        </div>

        {/* 홈화면 */}
        <div
          className="flex flex-col items-center text-sm text-gray-700"
          onClick={() => navi("/")}
        >
          <img src={homeImg} alt="홈화면" className="w-8 h-8" />
          <p>홈화면</p>
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
