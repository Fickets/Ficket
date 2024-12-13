import React, { useEffect, useState } from "react";

import { userLogout } from '../../service/user/userApi';
import { useCookies } from "react-cookie";
import { useNavigate } from "react-router";
import { useStore } from "zustand";
import { userStore } from "../../stores/UserStore";
import Logo from "../../assets/logo.png";
import SearchBar from "./SearchBar";

const UserHeader = () => {
  const [query, setQuery] = useState<string>(""); // 검색 입력 값
  const [results, setResults] = useState<SearchResult[]>([]); // 검색 결과
  const navi = useNavigate();
  const user = useStore(userStore);

  const LogoClick = () => {
    navi("/");
  };

  const handleLoginToggle = async () => {
    if (user.isLogin) {
      localStorage.removeItem('USER_STORE');
      await userLogout(
        (response) => {
          user.setIsLogin(false);
          console.log("LOGOUT");
          navi("/")
        }, () => { }
      )

    } else {
      // user.setIsLogin(true);
      navi("/users/login")

    }
  };

  return (
    <div className="hidden md:block">
      {" "}
      {/* 모바일에서 숨기기 */}
      <div className="flex items-center mx-[300px] justify-between">
        <div className="flex">
          <div onClick={LogoClick} className="flex">
            {/** 로고 */}
            <img src={Logo} alt="" className="w-[100px] h-auto" />
            <p className="text-[50px] font-semibold ml-[5px]">Ficket</p>
          </div>
          {/* 검색창 */}
          <div className="w-[500px] ml-[100px] mt-[5px]">
            <SearchBar />
          </div>
        </div>
        <div className="flex">
          {/* 로그인 상태에 따라 버튼 변경 */}
          {user.isLogin ? (
            <div>
              <button onClick={handleLoginToggle}>로그아웃</button>
            </div>
          ) : (
            <div>
              <button onClick={handleLoginToggle}>로그인</button>
              <button className="ml-[10px]">회원가입</button>
            </div>
          )}
          <button onClick={() => navi("/my-ticket")} className="ml-[10px]">
            마이티켓
          </button>
        </div>
      </div>
      {/** navBar */}
      <div className="flex mx-[300px] mt-[20px]">
        <button className="mr-[50px] text-[16px] font-medium" value={0}>
          뮤지컬
        </button>
        <button className="mr-[50px] text-[16px] font-medium" value={1}>
          콘서트
        </button>
        <button className="mr-[50px] text-[16px] font-medium" value={2}>
          스포츠
        </button>
        <button className="mr-[50px] text-[16px] font-medium" value={3}>
          전시/행사
        </button>
        <button className="mr-[50px] text-[16px] font-medium" value={4}>
          클래식/무용
        </button>
        <button className="mr-[50px] text-[16px] font-medium" value={5}>
          아동/가족
        </button>
      </div>
      <hr className="mt-[15px] mb-[50px]" />
    </div>
  );
};

export default UserHeader;
