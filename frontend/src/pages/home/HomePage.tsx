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
import musical from "../../assets/navi/musical.png";
import concert from "../../assets/navi/concert.png";
import sports from "../../assets/navi/sports.png";
import events from "../../assets/navi/event.png";
import classic from "../../assets/navi/classic.png";
import family from "../../assets/navi/family.png";
import Logo from "../../assets/logo.png";
import SearchBar from "../../components/@common/SearchBar.tsx";
import { useNavigate } from "react-router-dom";
import { Helmet } from "react-helmet-async";

const HomePage = () => {
  const navi = useNavigate();
  const [cookies] = useCookies(["isLogin"]);
  const user = useStore(userStore);
  useEffect(() => {
    window.scrollTo(0, 0);
    getAccess();
  }, []);

  const LogoClick = () => {
    navi("/");
  };

  const goGenreTicket = (genre: string) => {
    navi(`/events/genre-choice?choice=${genre}`);
  };

  const getAccess = async () => {
    if (Boolean(cookies.isLogin) && user.accessToken == "") {
      user.resetState();
      await userTokenRefresh(
        (response) => {
          console.log("HERE");
          user.setAccessToken(response.headers["authorization"]);
          // user.setIsLogin(true);
        },
        () => { },
      );
      await getMyInfo(
        (response) => {
          const res = response.data;
          console.log(res);
          changeMyInfo(res);
        },

        () => { },
      );
    } else if (!Boolean(cookies.isLogin)) {
      user.setIsLogin(false);
    }
  };

  const changeMyInfo = (res: Record<string, any>) => {
    user.setUserName(res["userName"]);
    user.setBirth(res["birth"]);
    user.setGender(res["gender"]);
    user.setUserId(res["userId"]);
    user.setIsLogin(true);
  };


  return (
    <div className="p-6">
      <Helmet>
        <title>Ficket</title>
      </Helmet>
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
      <div className="block md:hidden">
        <div className="flex mx-[40px]">
          <div
            className="flex flex-col items-center w-1/3"
            onClick={() => goGenreTicket("뮤지컬")}
          >
            <img src={musical} alt="" className="w-[50px]" />
            <p>뮤지컬</p>
          </div>
          <div
            className="flex flex-col items-center w-1/3"
            onClick={() => goGenreTicket("콘서트")}
          >
            <img src={concert} alt="" className="w-[50px]" />
            <p>콘서트</p>
          </div>
          <div
            className="flex flex-col items-center w-1/3"
            onClick={() => goGenreTicket("스포츠")}
          >
            <img src={sports} alt="" className="w-[50px]" />
            <p>스포츠</p>
          </div>
        </div>
        <div className="flex mx-[40px]">
          <div
            className="flex flex-col items-center w-1/3"
            onClick={() => goGenreTicket("전시_행사")}
          >
            <img src={events} alt="" className="w-[50px]" />
            <p>전시/행사</p>
          </div>
          <div
            className="flex flex-col items-center w-1/3"
            onClick={() => goGenreTicket("클래식_무용")}
          >
            <img src={classic} alt="" className="w-[50px]" />
            <p>클래식/무용</p>
          </div>
          <div
            className="flex flex-col items-center w-1/3"
            onClick={() => goGenreTicket("아동_가족")}
          >
            <img src={family} alt="" className="w-[50px]" />
            <p>아동/가족</p>
          </div>
        </div>
      </div>
      <OpenRecent genre="" />
      <GenreRank />
      <div className="block md:hidden">
        <BottomNav />
      </div>
    </div>
  );
};

export default HomePage;
