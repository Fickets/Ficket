
import { useSearchParams } from "react-router-dom";
import OpenGenre from "../../components/event/openGenre.tsx";
import UserHeader from "../../components/@common/UserHeader";
import ViewRanking from "../../components/home/ViewRanking.tsx";
import GenreHeader from "../../components/@common/MobileGenreHeader.tsx";
import GenreSearch from "../../components/event/GenreSearch.tsx";
import BottomNav from "../../components/@common/MobileBottom.tsx";
import { useEffect } from "react";

const GenreChoice: React.FC = () => {
    const [searchParams] = useSearchParams();
    const choice = searchParams.get("choice"); // 쿼리 문자열에서 'choice' 값 추출
    useEffect(() => {
        window.scrollTo(0, 0); // 페이지 이동 후 스크롤을 맨 위로
    }, []);
    return (
        <div className="p-6">
            <UserHeader />
            <GenreHeader title={choice || ""} />
            {/* <UserHeader /> */}
            <div className="hidden md:block">
                <ViewRanking />
            </div>
            <OpenGenre genre={choice || ""} />

            <GenreSearch key={choice} genre={choice || ""} />
            <div className="block md:hidden">
                <BottomNav />
            </div>
        </div>
    );
};

export default GenreChoice;
