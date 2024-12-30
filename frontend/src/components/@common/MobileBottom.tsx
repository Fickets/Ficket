import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useCookies } from "react-cookie";
import { userLogout } from "../../service/user/userApi";
import userImg from '../../assets/bottomNav/User.png';
import ticketImg from '../../assets/bottomNav/Ticket.png';
import homeImg from '../../assets/bottomNav/Home.png';

const BottomNav = () => {
    const [cookies] = useCookies(['isLogin']);
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
        <div className="fixed bottom-0 left-0 w-full bg-white ">
            <hr className="border border-black" />
            <div className="flex justify-between px-[50px] mt-[5px]">
                <div className="flex flex-col"
                    onClick={() => navi("/my-ticket")}>
                    <img src={ticketImg} alt="" className="w-[50px] h-[50px]" />
                    <p>마이티켓</p>
                </div>
                <div className="flex flex-col"
                    onClick={() => navi("/")}>
                    <img src={homeImg} alt="" className="w-[50px] h-[50px]" />
                    <p>홈화면</p>
                </div>
                <div className="flex flex-col"
                    onClick={handleLoginToggle}>
                    <img src={userImg} alt="" className="w-[50px] h-[50px]" />
                    {Boolean(cookies.isLogin) ? (
                        <p>로그아웃</p>
                    ) : (
                        <p>로그인</p>
                    )}
                </div>
            </div>
        </div>
    )
}

export default BottomNav;