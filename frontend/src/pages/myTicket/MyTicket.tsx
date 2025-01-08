import UserHeader from "../../components/@common/UserHeader.tsx";
import UserInfo from "../../components/myTicket/UserInfo.tsx";
import MyTicketList from "../../components/myTicket/MyTicketList.tsx";
import MobileBottom from "../../components/@common/MobileBottom.tsx";

const MyTicket = () => {
  return (
    <div className="p-6">
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
