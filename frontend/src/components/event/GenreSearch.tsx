import { useEffect, useState } from "react";
import { useNavigate } from "react-router";

import {
    Period,
} from "../../types/ReservationRateRanking";
import { getGenreAreaPeriod } from "../../service/event/genreChoice";

import { getArea } from "../../service/event/genreChoice";
import { SimpleEvent } from "../../types/home";


const GenreSearch = ({ genre }: { genre: string }) => {
    const navi = useNavigate();
    const [period, setPeriod] = useState<Period>(Period.DAILY);
    const [area, setArea] = useState("전체");
    const [areas, setAreas] = useState<string[]>([]);  // areas를 string[] 타입으로 지정
    const [page, setPage] = useState(0);
    const [events, setEvents] = useState<SimpleEvent[]>([]);
    const [isLoading, setIsLoading] = useState(false); // 로딩 상태

    // select 요소에서 값 변경 시 실행되는 함수
    const periodChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setPeriod(e.target.value as Period); // value를 Period로 타입 캐스팅
    };

    const areaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        console.log("AREA CHAGNE", e.target.value, " : ", page)
        setArea(e.target.value);
    };

    useEffect(() => {
        getAllArea();
    }, []);


    useEffect(() => {
        if (page === 0) {
            genreChoiceList(); // page가 0으로 리셋될 때마다 genreChoiceList 호출
        }
    }, [page]); // page가 변경될 때마다 실행

    useEffect(() => {
        console.log("CHANGE", area, period, genre);
        setPage(0); // 페이지 리셋
        setEvents([]); // 이벤트 초기화

    }, [area, period, genre]);

    const getAllArea = async () => {
        await getArea(
            (response) => { setAreas(response.data); },
            (error) => { console.log(error); }
        );
    };

    // 스크롤 이벤트 등록
    useEffect(() => {
        const handleScroll = () => {
            if (
                window.innerHeight + document.documentElement.scrollTop >=
                document.documentElement.offsetHeight - 10
            ) {
                if (!isLoading && page !== -1) {
                    genreChoiceList(); // 페이지 끝에 도달하면 데이터 요청
                }
            }
        };

        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll); // 이벤트 정리
    }, [isLoading, page]); // page나 isLoading 상태 변경 시에만 동작하도록 설정

    const genreChoiceList = async () => {
        console.log("PAGE" + page)
        if (page === -1) {
            return; // 마지막 페이지일 경우 더 이상 요청하지 않음
        }
        setIsLoading(true); // 데이터 요청 전에 로딩 시작
        try {
            const res = await getGenreAreaPeriod(genre, period, area, page); // 데이터 가져오기
            const newContent = res.content; // 새 데이터 추출
            const newLast = res.last; // 마지막 페이지 여부

            // 상태 업데이트
            setEvents((prevEvents) => [...prevEvents, ...newContent]);

            if (newLast) {
                setPage(-1); // 마지막 페이지일 경우 더 이상 페이지 증가를 막음
            } else {
                setPage((prevPage) => prevPage + 1); // 페이지 번호를 이전 상태에서 증가
            }
        } catch (error) {
            console.error("Error loading events:", error);
        } finally {
            setIsLoading(false); // 데이터 요청 완료 후 로딩 종료
        }
    };

    return (
        <div>
            <div className="hidden md:block">
                <div className="mx-[300px]">
                    <hr className="border-2 my-[40px]" />
                    <div className="flex mt-4">
                        <select
                            value={period}
                            onChange={periodChange}
                            className="text-lg font-medium border rounded-full px-[20px] focus:outline-none"
                        >
                            <option className="text-[16px]" value={Period.DAILY}>일간 랭킹순</option>
                            <option className="text-[16px]" value={Period.WEEKLY}>주간 랭킹순</option>
                            <option className="text-[16px]" value={Period.MONTHLY}>월간 랭킹순</option>
                        </select>
                        <select
                            value={area}
                            onChange={areaChange}
                            className="ml-[30px] text-lg font-medium border rounded-full px-[20px] focus:outline-none"
                        >
                            <option className="text-[16px]" value="전체">전체</option>
                            {areas.map((data, index) => (
                                <option key={index} className="text-[16px]" value={data}>
                                    {data}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="grid grid-cols-5 gap-4 p-4">
                        {events.length > 0 ? (
                            events.map((event, index) => (
                                <div
                                    className="bg-white border rounded-md shadow p-4 flex flex-col"
                                    key={index}
                                    onClick={() => { navi(`/events/detail/${event.eventId}`) }}
                                >
                                    <img src={event.pcImg} alt="" />
                                    <h3 className="font-bold text-lg">{event.title}</h3>
                                    <p className="text-sm text-gray-500">{event.eventStage}</p>
                                    <p className="text-sm text-gray-500">{event.date}</p>
                                </div>
                            ))
                        ) : (
                            <div className="text-center text-gray-500 py-8">
                                데이터가 없습니다.
                            </div>
                        )}
                    </div>

                    {/* 로딩 표시 */}
                    {isLoading && <div className="text-center py-4">로딩 중...</div>}
                </div>
            </div>
            {/**mobile */}
            <div className="block md:hidden scrollbar-hide overflow-y-hidden">
                <hr className="border-2 my-[40px]" />
                <div className="mx-[0px]">
                    <div className="flex mt-4">
                        <select
                            value={period}
                            onChange={periodChange}
                            className="text-lg font-medium border rounded-full  focus:outline-none"
                        >
                            <option className="text-[12px]" value={Period.DAILY}>일간 랭킹순</option>
                            <option className="text-[12px]" value={Period.WEEKLY}>주간 랭킹순</option>
                            <option className="text-[12px]" value={Period.MONTHLY}>월간 랭킹순</option>
                        </select>
                        <select
                            value={area}
                            onChange={areaChange}
                            className="ml-[15px] text-lg font-medium border rounded-full px-[20px] focus:outline-none"
                        >
                            <option className="text-[12px]" value="전체">전체</option>
                            {areas.map((data, index) => (
                                <option key={index} className="text-[12px]" value={data}>
                                    {data}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="flex flex-col mt-[30px]">
                        {events.length > 0 ? (
                            events.map((event, index) => (
                                <div
                                    className="bg-white border rounded-md shadow p-4 flex"
                                    key={index}
                                    onClick={() => { navi(`/events/detail/${event.eventId}`) }}
                                >
                                    <img src={event.mobileImg} alt="" className="w-[90px]" />
                                    <div className="flex flex-col m-[30px]">
                                        <h3 className="font-bold text-lg">{event.title}</h3>
                                        <p className="text-sm text-gray-500">{event.eventStage}</p>
                                        <p className="text-sm text-gray-500">{event.date}</p>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="text-center text-gray-500 py-8">
                                데이터가 없습니다.
                            </div>
                        )}
                    </div>
                    <div className="mt-[60px]"></div>
                </div>
            </div>
        </div>
    );
};

export default GenreSearch;
