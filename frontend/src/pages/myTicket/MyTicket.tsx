import UserHeader from "../../components/@common/UserHeader.tsx";
import UserInfo from "../../components/myTicket/UserInfo.tsx";
import MyTicketList from "../../components/myTicket/MyTicketList.tsx";

const MyTicket = () => {
  return (
    <div className="p-6">
      <UserHeader />
      <UserInfo />
      <MyTicketList />
    </div>
  );
};

export default MyTicket;
