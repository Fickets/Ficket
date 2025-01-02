import { useEffect } from "react";
import { useCookies } from "react-cookie";
import { useNavigate, useParams } from "react-router-dom";
import { userStore } from "../../stores/UserStore";
import { useSearchParams } from "react-router-dom";
import OpenGenre from "../../components/event/openGenre.tsx";
import { userTokenRefresh, getMyInfo } from "../../service/user/userApi";
import { useStore } from "zustand";
import UserHeader from "../../components/@common/UserHeader";
import ViewRanking from "../../components/home/ViewRanking.tsx";
import GenreHeader from "../../components/@common/MobileGenreHeader.tsx";
import OpenRecent from "../../components/home/OpenRecent.tsx";
import GenreRank from "../../components/home/GenreRank.tsx";
import GenreSearch from "../../components/event/GenreSearch.tsx";
import BottomNav from "../../components/@common/MobileBottom.tsx";

const GenreChoice: React.FC = () => {
    const [searchParams] = useSearchParams();
    const choice = searchParams.get("choice"); // 쿼리 문자열에서 'choice' 값 추출

    return (
        <div className="p-6">
            <UserHeader />
            <GenreHeader title={choice} className />
            {/* <UserHeader /> */}
            <div className="hidden md:block">
                <ViewRanking />
            </div>
            <OpenGenre genre={choice} />
            
            <GenreSearch key={choice} genre={choice} />
            <div className="block md:hidden">
                <BottomNav />
            </div>
        </div>
    );
};

export default GenreChoice;
