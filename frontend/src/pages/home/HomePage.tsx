import { useEffect } from "react";
import { useCookies } from "react-cookie";
import { userStore } from "../../stores/UserStore";
import { userTokenRefresh, getMyInfo } from "../../service/user/userApi";
import { eventDetailStore } from "../../stores/EventStore";
import { useStore } from "zustand";
import UserHeader from "../../components/@common/UserHeader";
import ViewRanking from "../../components/home/ViewRanking.tsx";

const HomePage = () => {
  const [cookies] = useCookies(["isLogin"]);
  const user = useStore(userStore);
  const event = useStore(eventDetailStore);

  useEffect(() => {
    getAccess();
  }, []);

  const getAccess = async () => {
    if (Boolean(cookies.isLogin) && user.accessToken === "") {
      localStorage.removeItem("USER_STORE");
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
      <ViewRanking />
    </div>
  );
};

export default HomePage;
