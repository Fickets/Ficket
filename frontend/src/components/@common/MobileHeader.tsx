import { useState, useRef, useEffect } from "react";
import { AiOutlineArrowLeft, AiOutlineSearch } from "react-icons/ai";
import { useNavigate } from "react-router-dom";

interface MobileHeaderProps {
  title: string; // 헤더 제목
}

const MobileHeader = ({ title }: MobileHeaderProps) => {
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
      className="fixed top-0 left-0 right-0 bg-white flex items-center px-4 max-w-[400px] h-[50px] border-b border-gray-300 z-50"
      ref={searchRef}
    >
      {!isSearchMode ? (
        <>
          {/* 뒤로가기 버튼 */}
          <button
            onClick={() => navigate(-1)} // navigate로 뒤로가기 처리
            className="text-black text-xl"
          >
            <AiOutlineArrowLeft />
          </button>

          {/* 제목 */}
          <h1 className="text-lg font-medium flex-1 text-center">{title}</h1>

          {/* 검색 버튼 */}
          <button
            onClick={() => setIsSearchMode(true)} // 검색 모드로 전환
            className="text-black text-xl"
          >
            <AiOutlineSearch />
          </button>
        </>
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

export default MobileHeader;
