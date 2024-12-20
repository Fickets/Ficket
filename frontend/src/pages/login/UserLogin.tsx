import React from 'react';
import LoginLogo from "../../assets/logo.png";
import LoginBtn from "../../assets/Login/kakaoLoginBtn.png";


const UserLogin: React.FC = () => {


    const login = async () => {
        window.location.href = "http://localhost:8777/oauth2/authorization/kakao"
    }

    return (
        <div className='w-screen h-screen flex flex-col justify-center items-center bg-white'>
            <img src={LoginLogo} className="-mt-11 mb-5" />
            <h1 className='font-black text-5xl pb-8'>FICKET</h1>
            &nbsp;
            <img src={LoginBtn} onClick={() => login()} />
        </div>
    )
};

export default UserLogin;