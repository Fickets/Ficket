import { useEffect } from "react";
import { useCookies } from "react-cookie";
import { userStore } from "../../stores/UserStore";
import { userTokenRefresh, getMyInfo } from "../../service/user/userApi";
import { useStore } from "zustand";
import UserHeader from "../../components/@common/UserHeader";
import ViewRanking from "../../components/home/ViewRanking.tsx";
import OpenRecent from "../../components/home/OpenRecent.tsx";
import GenreRank from "../../components/home/GenreRank.tsx";
import BottomNav from "../../components/@common/MobileBottom.tsx";
import Logo from "../../assets/logo.png";
import SearchBar from "../../components/@common/SearchBar.tsx";
import { useNavigate } from "react-router-dom";

const HomePage = () => {
  const navi = useNavigate();
  const [cookies] = useCookies(["isLogin"]);
  const user = useStore(userStore);

  useEffect(() => {
    getAccess();
  }, []);

  const LogoClick = () => {
    navi("/");
  };

  const getAccess = async () => {
    if (Boolean(cookies.isLogin) && user.accessToken === "") {
      user.resetState();
      await userTokenRefresh(
        (response) => {
          console.log("HERE");
          user.setAccessToken(response.headers["authorization"]);
          user.setIsLogin(true);
        },
        () => {},
      );
      await getMyInfo(
        (response) => {
          const res = response.data;
          console.log(res);
          user.setUserName(res["userName"]);
          user.setBirth(res["birth"]);
          user.setGender(res["gender"]);
          user.setUserId(res["userId"]);
          user.setIsLogin(true);
        },
        () => {},
      );
    } else {
      user.setIsLogin(false);
    }
  };

  return (
    <div className="p-6">
      <UserHeader />

      <div className="block md:hidden flex">
        <div onClick={LogoClick} className="flex items-end">
          {/** 로고 */}
          <img src={Logo} alt="" className="w-[40px] h-[40px]" />
          <p className="text-[20px] font-semibold ml-[5px]">Ficket</p>
        </div>
        <div className=" ml-[40px] mt-[4px]">
          <SearchBar />
        </div>
      </div>
      <ViewRanking />
      <OpenRecent />
      <GenreRank />
      <div className="block md:hidden">
        <BottomNav />
      </div>
    </div>
  );
};

export default HomePage;
