import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom"; // useNavigate import
import { genreRank } from "../../service/home/api";
import { SimpleEvent } from "../../types/home";
import NoContent from '../../assets/nocontent.png';

const GenreRank = () => {
    const navi = useNavigate();
    const [rankGenre, setRankGenre] = useState<SimpleEvent[]>([])
    const [genre, setGenre] = useState<string>("뮤지컬")

    const genres = [
        "뮤지컬",
        "콘서트",
        "스포츠",
        "전시_행사",
        "클래식_무용",
        "아동_가족",
    ];
    useEffect(() => {
        getGenreRank(genre);
    }, [genre]);

    const getGenreRank = async (param: string) => {
        try {
            const data = await genreRank(param);
            setRankGenre(data);
            console.log("Fetched data:", data); // 필요 시 데이터 확인
        } catch (error) {
            console.error("Error while fetching open recent events:", error);
        }
    }


    return (
        <div>
            {/**pc */}
            <div className="hidden md:block">
                <div className="flex mb-[20px] mt-[100px] mx-[300px] justify-between">
                    <h1 className="font-medium text-[35px]">장르별 랭킹</h1>
                    <div className="flex ">
                        {genres.map((genreName) => (
                            <button
                                key={genreName}
                                onClick={() => setGenre(genreName)}
                                className={`border rounded-full m-[5px] px-[20px] h-[40px] mt-[10px] ${genre === genreName
                                    ? "bg-blue-500 text-white" // 선택된 버튼 스타일
                                    : "bg-white text-black"   // 기본 버튼 스타일
                                    }`}
                            >
                                {genreName}
                            </button>
                        ))}
                    </div>
                    <div></div>
                </div>
                <div className="mx-[300px] h-[35vb 0px]">
                    {rankGenre.length === 0 ? (<div>
                        <img src={NoContent} alt="" />
                    </div>) : (
                        <div className="flex ">
                            {rankGenre.map((event, index) => (
                                <div key={index} className="relative mx-[10px] border rounded-full"
                                    onClick={() => navi(`events/detail/${event.eventId}`)}>
                                    {/* 이미지 */}
                                    <img
                                        src={event.pcImg}
                                        alt=""
                                        className="rounded-lg object-cover"
                                    />
                                    {/* Index 표시 */}
                                    <div className="">
                                        <p className="absolute bottom-2 left-2 bg-[] text-shadow text-white text-[70px] font-bold px-[5px] py-[2px] rounded">
                                            {index + 1}
                                        </p>
                                        <p className="font-bold">{event.title}</p>
                                    </div>

                                </div>
                            ))}
                        </div>
                    )}
                </div>
                <div className="flex justify-center items-center">
                    <button
                        className=" text-[18px] mt-[30px] font-medium border border-2 rounded-full w-[200px] "
                        onClick={() => navi("/")}
                    >
                        {genre.replace("_", "/")} 전체보기
                    </button>
                </div>

            </div>
            {/**mobile */}
            <div className="block md:hidden scrollbar-hide overflow-y-hidden">
                <div className="flex justify-center mt-[30px]">
                    <h1 className="font-medium text-[30px] ">장르별 랭킹</h1>
                </div>
                <div className="flex overflow-x-auto whitespace-nowrap scrollbar-hide">
                    {genres.map((genreName) => (
                        <button
                            key={genreName}
                            onClick={() => setGenre(genreName)}
                            className={`border rounded-full m-[5px] px-[20px] h-[40px] mt-[10px] ${genre === genreName
                                ? "bg-blue-500 text-white" // 선택된 버튼 스타일
                                : "bg-white text-black"   // 기본 버튼 스타일
                                }`}
                        >
                            {genreName}
                        </button>
                    ))}
                </div>
                <div className="flex overflow-x-auto whitespace-nowrap scrollbar-hide h-[250px]">
                    {rankGenre.length === 0 ? (<div>
                        <img src={NoContent} alt="" className="h-[187px]" />
                    </div>) : (
                        <div className="flex">
                            {rankGenre.map((event, index) => (
                                <div key={index} className="relative mx-[10px]  rounded-full"
                                    onClick={() => navi(`events/detail/${event.eventId}`)}>
                                    {/* 이미지 */}
                                    <img
                                        src={event.mobileImg}
                                        alt=""
                                        className="rounded-lg object-cover"
                                    />
                                    {/* Index 표시 */}
                                    <div className="">
                                        <p className="absolute bottom-2 left-2 bg-[] text-shadow text-white text-[50px] font-bold px-[5px] py-[50px] rounded">
                                            {index + 1}
                                        </p>
                                        <p className="font-bold text-clip overflow-hidden">{event.title}</p>
                                    </div>

                                </div>
                            ))}
                        </div>
                    )}
                </div>
                <div className="flex justify-center items-center pb-[100px]">
                    <button
                        className=" text-[18px] mt-[30px] font-medium border border-2 rounded-full w-[200px] "
                        onClick={() => navi("/")}
                    >
                        {genre.replace("_", "/")} 전체보기
                    </button>
                </div>
            </div>
        </div>
    )
}

export default GenreRank;