import UserHeader from "../../components/@common/UserHeader.tsx";
import UserInfo from "../../components/myTicket/UserInfo.tsx";
import MyTicketList from "../../components/myTicket/MyTicketList.tsx";
import MobileBottom from "../../components/@common/MobileBottom.tsx";
import { Helmet } from "react-helmet-async";

const MyTicket = () => {
  return (
    <div className="p-6">
      <Helmet>
        <title>마이 티켓</title>
      </Helmet>
      <UserHeader />
      <UserInfo />
      <MyTicketList />
      <div className="mb-[60px]">
        <MobileBottom />
      </div>
    </div>
  );
};

export default MyTicket;
