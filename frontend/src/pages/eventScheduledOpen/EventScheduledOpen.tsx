import { useMediaQuery } from "react-responsive";
import MobileEventOpenList from "../../components/eventScheduledOpen/MobileOpenEventList.tsx";
import PcEventOpenList from "../../components/eventScheduledOpen/PcOpenEventList.tsx";
import MobileHeader from "../../components/@common/MobileHeader.tsx";
import UserHeader from "../../components/@common/UserHeader.tsx";
import MobileBottom from "../../components/@common/MobileBottom.tsx";
import { Helmet } from "react-helmet-async";
import { useEffect } from "react";
const EventScheduledOpen = () => {
  const isMobile: boolean = useMediaQuery({ query: "(max-width: 768px)" });
  useEffect(() => {
    window.scrollTo(0, 0); // 페이지 이동 후 스크롤을 맨 위로
  }, []);
  return (
    <div>
      <Helmet>
        <title>오픈 티켓</title>
      </Helmet>
      {isMobile ? (
        <div>
          <MobileHeader title={"오픈 티켓"} />
          <MobileEventOpenList />
          <MobileBottom />
        </div>
      ) : (
        <div>
          <div className="mt-6">
            <UserHeader />
          </div>
          <PcEventOpenList />
        </div>
      )}
    </div>
  );
};

export default EventScheduledOpen;
