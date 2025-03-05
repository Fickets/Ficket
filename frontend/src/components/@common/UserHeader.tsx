import { userLogout } from "../../service/user/userApi";
import { MouseEvent } from 'react';
import { useCookies } from "react-cookie";
import { useNavigate } from "react-router";
import { useStore } from "zustand";
import { userStore } from "../../stores/UserStore";
import Logo from "../../assets/logo.png";
import SearchBar from "./SearchBar";

const UserHeader = () => {
  const [cookies] = useCookies(["isLogin"]);
  const navi = useNavigate();
  const user = useStore(userStore);

  const LogoClick = () => {
    navi("/");
  };

  const handleLoginToggle = async () => {
    if (Boolean(cookies.isLogin)) {
      await userLogout(
        (_response) => {
          user.resetState();
          navi("/");
        },
        () => { },
      );
    } else {
      navi("/users/login");
    }
  };

  const genreChoice = (e: MouseEvent<HTMLButtonElement>) => {
    navi(`/events/genre-choice?choice=${e.currentTarget.value}`);
  };

  const myTicketClick = async () => {
    if (Boolean(cookies.isLogin)) {
      navi("/my-ticket")
    } else {
      navi("/users/login");
    }
  }

  return (
    <div>
      {/* PC */}
      <div className="hidden md:block">
        <div className="flex items-center mx-[300px] justify-between">
          <div className="flex">
            <div onClick={LogoClick} className="flex cursor-pointer">
              {/** 로고 */}
              <img src={Logo} alt="" className="w-[100px] h-auto" />
              <p className="text-[50px] font-semibold mt-3 ml-[5px]">Ficket</p>
            </div>
            {/* 검색창 */}
            <div className="w-[500px] ml-[100px] mt-[5px] p-4">
              <SearchBar />
            </div>
          </div>
          <div className="flex">
            {/* 로그인 상태에 따라 버튼 변경 */}
            {Boolean(cookies.isLogin) ? (
              <div>
                <button onClick={handleLoginToggle}>로그아웃</button>
              </div>
            ) : (
              <div>
                <button onClick={handleLoginToggle}>로그인</button>
                <button onClick={() => navi("/users/login")} className="ml-[10px]">회원가입</button>
              </div>
            )}
            <button onClick={myTicketClick} className="ml-[10px]">
              마이티켓
            </button>
          </div>
        </div>
        {/** navBar */}
        <div className="flex mx-[300px] mt-[20px]">
          <button
            className="mr-[50px] text-[16px] font-medium"
            value="뮤지컬"
            onClick={genreChoice}
          >
            뮤지컬
          </button>
          <button
            className="mr-[50px] text-[16px] font-medium"
            value="콘서트"
            onClick={genreChoice}
          >
            콘서트
          </button>
          <button
            className="mr-[50px] text-[16px] font-medium"
            value="스포츠"
            onClick={genreChoice}
          >
            스포츠
          </button>
          <button
            className="mr-[50px] text-[16px] font-medium"
            value="전시_행사"
            onClick={genreChoice}
          >
            전시/행사
          </button>
          <button
            className="mr-[50px] text-[16px] font-medium"
            value="클래식_무용"
            onClick={genreChoice}
          >
            클래식/무용
          </button>
          <button
            className="mr-[50px] text-[16px] font-medium"
            value="아동_가족"
            onClick={genreChoice}
          >
            아동/가족
          </button>
        </div>
        <hr className="mt-[15px] mb-[50px]" />
      </div>
    </div>
  );
};

export default UserHeader;
