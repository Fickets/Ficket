import { useMediaQuery } from "react-responsive";
import MobileEventOpenList from "../../components/eventScheduledOpen/MobileOpenEventList.tsx";
import PcEventOpenList from "../../components/eventScheduledOpen/PcOpenEventList.tsx";
import MobileHeader from "../../components/@common/MobileHeader.tsx";
import UserHeader from "../../components/@common/UserHeader.tsx";
import MobileBottom from "../../components/@common/MobileBottom.tsx";

const EventScheduledOpen = () => {
  const isMobile: boolean = useMediaQuery({ query: "(max-width: 768px)" });

  return (
    <div>
      {isMobile ? (
        <div>
          <MobileHeader title={"오픈 티켓"} />
          <MobileEventOpenList />
          <MobileBottom />
        </div>
      ) : (
        <div>
          <UserHeader />
          <PcEventOpenList />
        </div>
      )}
    </div>
  );
};

export default EventScheduledOpen;
