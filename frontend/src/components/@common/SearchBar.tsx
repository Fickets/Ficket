import React, { useEffect, useState } from 'react';

import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore } from 'zustand';

const SearchBar = () => {
    const [query, setQuery] = useState<string>(""); // 검색 입력 값
    const [results, setResults] = useState<SearchResult[]>([]); // 검색 결과
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            handleSearch(query);
        }
    };
    const handleSearch = async (searchTerm: string) => {
        // 예: API를 통해 검색 결과를 가져오는 로직
        const data: SearchResult[] = [
            { pk: 1, title: "Event 1" },
            { pk: 2, title: "Event 2" },
            { pk: 3, title: "Event 3" },
        ].filter((item) =>
            item.title.toLowerCase().includes(searchTerm.toLowerCase())
        );
        setResults(data);
    };
    const handleClear = () => {
        setQuery("");
        setResults([]);
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setQuery(value);

        if (value === "") {
            // 입력 값이 없으면 결과를 초기화
            setResults([]);
        } else {
            // 입력 중일 때 실시간 검색
            handleSearch(value);
        }
    };


    return (
        <div>
            <div className="relative z-50 w-full max-w-lg mx-auto p-4">
                {/* 입력창 */}
                <div className="flex items-center border border-gray-300 rounded-full px-4 py-2 shadow-sm bg-white">
                    <input
                        type="text"
                        placeholder="검색어를 입력하세요..."
                        value={query}
                        onChange={handleInputChange}
                        onKeyDown={handleKeyDown}
                        className="flex-1 outline-none text-gray-700 bg-transparent"
                    />
                    {query && (
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
                                key={result.pk}
                                // onClick={() => navigate(`/event/detail/${result.pk}`)}
                                className="w-full text-left px-4 py-2 hover:bg-gray-100 border-b last:border-none"
                            >
                                {result.title}
                            </button>
                        ))}
                    </div>
                )}
                {/** 헤더 버튼 */}
                <div>

                </div>

            </div>
        </div>
    )
}

export default SearchBar