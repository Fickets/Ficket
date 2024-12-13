import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCookies } from 'react-cookie';
import { userStore } from '../../stores/UserStore';
import { userTokenRefresh, getMyInfo } from '../../service/user/userApi';
import { eventDetailStore } from '../../stores/EventStore';
import { useStore } from "zustand";
import UserHeader from '../../components/@common/UserHeader';
const HomePage: React.FC = () => {
  const [cookies] = useCookies(['isLogin']);
  const user = useStore(userStore);
  const event = useStore(eventDetailStore);
  useEffect(() => {
    getAccess();
  }, [])

  const getAccess = async () => {
    if (Boolean(cookies.isLogin) && user.accessToken === "") {
      localStorage.removeItem('USER_STORE');
      await userTokenRefresh(
        (response) => {
          console.log("HERE")
          user.setAccessToken(response.headers['authorization']);
          user.setIsLogin(true);

        },
        () => {
        }
      )
      await getMyInfo(
        (response) => {
          const res = response.data;
          console.log(res);
          user.setUserName(res["userName"])
          user.setBirth(res["birth"])
          user.setGender(res['gender'])
          user.setUserId(res['userId'])
          user.setIsLogin(true)
        }, () => { }
      )


    } else {
      user.setIsLogin(false);
    }
  }


  const openWindow = () => {
    const data = { eventId: "1" }; // 전달할 데이터
    const query = new URLSearchParams(data).toString(); // 쿼리스트링 생성

    // 창 옵션 설정
    const windowFeatures = "width=900,height=600,top=100,left=100";

    window.open(
      `ticketing/select-session/${query}`, // 새 창에서 열 URL
      "_blank", // 새 창으로 열기
      windowFeatures, // 창 옵션
    );
  };

  return (
    <div className='p-6'>
      <UserHeader />
      <h1 className={"font-sans"}>메인페이지 입니다.</h1>
      <button onClick={openWindow}>새 창 열기</button>
    </div>
  );
};

export default HomePage;
