import Sidebar from "../../components/@common/Sidebar.tsx";
import EventList from "../../components/eventList/EventList.tsx";
import EventSearchBar from "../../components/eventList/EventSearchBar.tsx";

const AdminEventList = () => {
  return (
    <div className="flex h-screen bg-[#F0F2F5]">
      <div className="w-64 h-full">
        <Sidebar currentStep={"performance"} />
      </div>
      <div className="flex-1 p-8 overflow-auto space-y-6">
        <EventSearchBar />
        <EventList />
      </div>
    </div>
  );
};

export default AdminEventList;
