import React from "react";
import LoginLogo from "../../assets/logo.png";
import LoginBtn from "../../assets/Login/kakaoLoginBtn.png";
import { Helmet } from "react-helmet-async";

const KAKAO_AUTHORIZATION_URL: string = import.meta.env.VITE_KAKAO_AUTHORIZATION_URL;

const UserLogin: React.FC = () => {
  const login = async () => {
    console.log(KAKAO_AUTHORIZATION_URL)
    window.location.href = KAKAO_AUTHORIZATION_URL;
  };

  return (
    <div className="w-screen h-screen flex flex-col justify-center items-center bg-white">
      <Helmet>
        <title>로그인</title>
      </Helmet>
      <img src={LoginLogo} className="-mt-11 mb-5" />
      <h1 className="font-black text-5xl pb-8">FICKET</h1>
      &nbsp;
      <img src={LoginBtn} onClick={() => login()} />
    </div>
  );
};

export default UserLogin;
