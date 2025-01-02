import { useState, useRef, useEffect } from "react";
import { AiOutlineArrowLeft, AiOutlineSearch } from "react-icons/ai";
import { useNavigate } from "react-router-dom";

interface MobileHeaderProps {
    title: string; // 헤더 제목
}

const GenreHeader = ({ title }: MobileHeaderProps) => {
    const [isSearchMode, setIsSearchMode] = useState(false); // 검색 모드 상태
    const [searchQuery, setSearchQuery] = useState(""); // 검색 입력값
    const searchRef = useRef<HTMLDivElement>(null);
    const navigate = useNavigate(); // 뒤로가기 처리

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                searchRef.current &&
                !searchRef.current.contains(event.target as Node)
            ) {
                setIsSearchMode(false); // 검색창 외부 클릭 시 검색 모드 종료
            }
        };

        // 문서 전체 클릭 이벤트 감지
        document.addEventListener("mousedown", handleClickOutside);

        // 정리 작업
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    return (
        <div
            className="block md:hidden fixed top-0 left-0 right-0 bg-white flex items-center px-4 h-[50px] border-b border-gray-300 z-50"
            ref={searchRef}
        >
            {!isSearchMode ? (
                <div className="flex justify-between items-center w-full">
                    {/* 뒤로가기 버튼 */}
                    <button
                        onClick={() => navigate(-1)}
                        className="text-black text-xl flex-shrink-0"
                    >
                        <AiOutlineArrowLeft />
                    </button>

                    {/* 제목 */}
                    <div className="flex-grow text-center mx-4">
                        <select
                            value={title}
                            onChange={(e) => navigate(`/events/genre-choice?choice=${e.target.value}`)}
                            className="text-lg font-medium border-none focus:outline-none"
                        >
                            <option className="text-[12px]" value="뮤지컬">뮤지컬</option>
                            <option className="text-[12px]" value="스포츠">스포츠</option>
                            <option className="text-[12px]" value="콘서트">콘서트</option>
                            <option className="text-[12px]" value="전시_행사">전시/행사</option>
                            <option className="text-[12px]" value="클래식_무용">클래식/무용</option>
                            <option className="text-[12px]" value="아동_가족">아동/가족</option>
                        </select>
                    </div>

                    {/* 검색 버튼 */}
                    <button
                        onClick={() => setIsSearchMode(true)}
                        className="text-black text-xl flex-shrink-0"
                    >
                        <AiOutlineSearch />
                    </button>
                </div>
            ) : (
                <>
                    {/* 검색창 모드 */}
                    <button
                        onClick={() => setIsSearchMode(false)} // 뒤로가기 클릭 시 종료
                        className="text-black text-xl"
                    >
                        <AiOutlineArrowLeft />
                    </button>

                    {/* 검색 입력창 */}
                    <input
                        type="text"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        placeholder="검색어를 입력하세요"
                        className="flex-1 ml-4 text-sm bg-gray-100 p-2 rounded-md"
                    />
                </>
            )}
        </div>
    );
};

export default GenreHeader;
