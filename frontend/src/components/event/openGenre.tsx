import { useEffect, useState } from "react";
import { openRecent } from "../../service/home/api";
import { useNavigate } from "react-router";
import { SimpleEvent } from "../../types/home";
import NoContent from "../../assets/nocontent.png";

const OpenGenre = ({ genre }: { genre: string }) => {
  const navi = useNavigate();
  const [open6, setopen6] = useState<SimpleEvent[]>([]);
  useEffect(() => {
    getOpenRecent();
  }, [genre]);

  const firstRow = open6.slice(0, 3);
  const secondRow = open6.slice(3);

  const getOpenRecent = async () => {

    try {
      let data; // 조건문 밖에서 선언
      if (genre === "") {
        data = await openRecent(""); // null로 호출
      } else {
        data = await openRecent(genre); // genre로 호출
      }
      setopen6(data); // 데이터를 상태에 저장

    } catch (error) {
      console.error("Error while fetching open recent events:", error);
    }
  };

  const formatDate = (dateString: string): string => {
    const inputDate = new Date(dateString); // 입력된 날짜
    const now = new Date(); // 현재 시간

    // 오늘인지 확인 (연, 월, 일 비교)

    // 현재 시간과 비교
    if (inputDate > now) {
      // 입력된 시간이 현재 시간 이후라면 "오늘 HH시 mm분" 반환
      const hours = inputDate.getHours().toString().padStart(2, "0");
      const minutes = inputDate.getMinutes().toString().padStart(2, "0");
      return `오늘 ${hours}시 ${minutes}분`;
    } else {
      // 입력된 시간이 현재 시간 이전이라면 "예매 가능" 반환
      return "예매 가능";
    }
  };
  const open6Click = (eventId: number) => {
    navi(`/events/detail/${eventId}`);
  };
  const goOpenTicket = () => {
    navi("/contents/scheduled-open");
  };

  return (
    <div className="">
      {/**PC 화면 */}
      <div className="hidden md:block">
        <div>
          <h1 className="font-medium text-[35px] mb-[20px] mt-[100px] ml-[300px]">
            티켓오픈
          </h1>
        </div>
        {open6.length === 0 ? (
          <div className="mx-[300px]">
            <img src={NoContent} alt="" className="h-[187px]" />
          </div>
        ) : (
          <div>
            {/* 첫 번째 줄 */}
            <div className="flex mx-[300px]">
              {firstRow.map((event, index) => (
                <div
                  key={index}
                  className="  rounded shadow w-1/3 mx-[15px] border"
                  onClick={() => open6Click(event.eventId)}
                >
                  <div className="flex">
                    <img src={event.pcImg} alt="" />
                    <div className="flex flex-col ">
                      <p className="text-[#8E43E7] font-bold text-[18px] mt-[30px] ml-[30px]">
                        {formatDate(event.date)}
                      </p>
                      <p className="ml-[30px] mt-[20px] font-bold text-[18px]">
                        {event.title}
                      </p>
                      <p className="ml-[30px] mt-[10px] text-[#666666]">
                        일반예매
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* 두 번째 줄 */}
            <div className="flex mx-[300px]">
              {secondRow.map((event, index) => (
                <div
                  key={index}
                  className="  rounded shadow w-1/3 mx-[15px] border"
                  onClick={() => open6Click(event.eventId)}
                >
                  <div className="flex">
                    <img src={event.pcImg} alt="" />
                    <div className="flex flex-col ">
                      <p className="text-[#8E43E7] font-bold text-[18px] mt-[30px] ml-[30px]">
                        {formatDate(event.date)}
                      </p>
                      <p className="ml-[30px] mt-[20px] font-bold text-[18px]">
                        {event.title}
                      </p>
                      <p className="ml-[30px] mt-[10px] text-[#666666]">
                        일반예매
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="flex justify-center items-center mt-[20px]">
          <button
            className=" text-[18px] font-medium border border-2 rounded-full w-[200px] "
            onClick={goOpenTicket}
          >
            티켓오픈 전체 보기
          </button>
        </div>
      </div>
      {/**mobile */}
      <div className="block md:hidden scrollbar-hide overflow-y-hidden">
        <div className="flex justify-center mt-[30px]">
          <h1 className="font-medium text-[30px] ">티켓오픈</h1>
        </div>
        {/* 가로 스크롤 가능한 부분 */}
        <div className="flex overflow-x-auto whitespace-nowrap scrollbar-hide">
          <div className="flex">
            {open6.length === 0 ? (
              <div>
                <img src={NoContent} alt="" className="h-[187px]" />
              </div>
            ) : (
              <div className="flex">
                {open6.map((event, index) => (
                  <div
                    key={index}
                    className="rounded shadow w-[250px] mx-[15px] overflow-hidden" // overflow-hidden 추가
                    onClick={() => open6Click(event.eventId)}
                  >
                    <div className="flex flex-col">
                      <img
                        src={event.mobileImg}
                        alt=""
                        className="w-full h-auto"
                      />{" "}
                      {/* 이미지 크기 조정 */}
                      <div className="flex flex-col">
                        <p className="text-[#8E43E7] font-bold text-[18px] mt-[30px] ml-[30px] truncate">
                          {formatDate(event.date)}
                        </p>
                        <p className="ml-[30px] mt-[20px] font-bold text-[18px] truncate">
                          {event.title}
                        </p>
                        <p className="ml-[30px] mt-[10px] text-[#666666] truncate">
                          일반예매
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
        <div className="flex justify-center items-center">
          <button
            className=" text-[18px] mt-[30px] font-medium border border-2 rounded-full w-[200px] "
            onClick={goOpenTicket}
          >
            티켓오픈 전체 보기
          </button>
        </div>
      </div>
    </div>
  );
};

export default OpenGenre;
