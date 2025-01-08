import React, { useState, useCallback, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { searchAutoComplete } from "../../service/search/api.ts";
import { autoCompleteRes } from "../../types/search.ts";
import { debounce } from "lodash";

const SearchBar = () => {
  const navigate = useNavigate();
  const [title, setTitle] = useState<string>(""); // 검색 입력 값
  const [results, setResults] = useState<autoCompleteRes[]>([]);
  const searchBarRef = useRef<HTMLDivElement>(null); // 검색창 컨테이너 Ref

  // 디바운스된 검색 함수
  const debouncedSearch = useCallback(
    debounce(async (query: string) => {
      if (query.trim() === "") {
        setResults([]);
        return;
      }
      try {
        const response = await searchAutoComplete(query);
        console.log("검색 결과:", response);
        setResults(response);
      } catch (error) {
        console.error("검색 요청 실패:", error);
        setResults([]);
      }
    }, 300), // 300ms 지연
    [],
  );

  // 입력 변경 핸들러
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setTitle(value);
    debouncedSearch(value); // 디바운스된 검색 호출
  };

  // 입력창 포커스 핸들러
  const handleInputFocus = () => {
    if (title.trim() !== "") {
      debouncedSearch(title); // 기존 검색어로 다시 검색
    }
  };

  // Enter 키 동작 핸들러
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      navigate(`/contents/search?keyword=${title}`);
    }
  };

  // 검색어 초기화 핸들러
  const handleClear = () => {
    setTitle("");
    setResults([]);
  };

  // 검색 결과 선택 핸들러
  const handleResultClick = (eventId: string) => {
    navigate(`/events/detail/${eventId}`);
  };

  // 강조 표시된 텍스트 렌더링 함수
  const highlightText = (text: string, highlight: string) => {
    if (!highlight) return text;

    const parts = text.split(new RegExp(`(${highlight})`, "gi")); // 검색어로 문자열 분리
    return parts.map((part, index) =>
      part.toLowerCase() === highlight.toLowerCase() ? (
        <span key={index} style={{ color: "purple", fontWeight: "bold" }}>
          {part}
        </span>
      ) : (
        part
      ),
    );
  };

  // 외부 클릭 감지 핸들러
  const handleClickOutside = (e: MouseEvent) => {
    if (
      searchBarRef.current &&
      !searchBarRef.current.contains(e.target as Node)
    ) {
      setResults([]); // 결과 리스트 숨기기
    }
  };

  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div ref={searchBarRef} className="relative z-50 w-full max-w-lg mx-auto">
      {/* 입력창 */}
      <div className="flex items-center border border-gray-300 rounded-full px-4 py-2 shadow-sm bg-white">
        <input
          type="text"
          placeholder="행사를 검색하세요"
          value={title}
          onChange={handleInputChange}
          onFocus={handleInputFocus} // 포커스 시 검색 결과 다시 표시
          onKeyDown={handleKeyDown}
          className="flex-1 outline-none text-gray-700 bg-transparent"
        />
        {title && (
          <button
            onClick={handleClear}
            className="text-gray-500 hover:text-gray-700 ml-2"
          >
            &#10005; {/* 'X' 아이콘 */}
          </button>
        )}
      </div>

      {/* 검색 결과 (오버레이) */}
      {results.length > 0 && (
        <div className="absolute top-full left-0 w-full mt-2 bg-white border border-gray-300 rounded-lg shadow-lg z-50">
          {results.map((result) => (
            <button
              key={result.EventId}
              onClick={() => handleResultClick(result.EventId)}
              className="flex items-center w-full text-left px-4 py-2 hover:bg-gray-100 border-b last:border-none space-x-2"
            >
              {/* 강조된 텍스트와 위치 */}
              <div className="flex-1">{highlightText(result.Title, title)}</div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default SearchBar;
