import { useEffect } from "react";
import { useCookies } from "react-cookie";
import { userStore } from "../../stores/UserStore";
import { userTokenRefresh, getMyInfo } from "../../service/user/userApi";
import { useStore } from "zustand";
import UserHeader from "../../components/@common/UserHeader";
import ViewRanking from "../../components/home/ViewRanking.tsx";

const HomePage = () => {
  const [cookies] = useCookies(["isLogin"]);
  const user = useStore(userStore);

  useEffect(() => {
    getAccess();
  }, []);

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

  const handleQueueTest = () => {
    // 새 창으로 대기열 화면 열기 (900x600 사이즈)
    window.open("/ticketing/queue", "_blank", "width=900,height=600");
  };

  return (
    <div className="p-6">
      <UserHeader />
      <ViewRanking />

      <button onClick={handleQueueTest} className="bg-red-500 mt-3">
        대기열 화면 테스트
      </button>
    </div>
  );
};

export default HomePage;
