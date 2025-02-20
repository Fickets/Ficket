import React, { useEffect, useState } from "react";

import SelectDateComponent from "../../components/ticketing/SelectDateComponent";
import TicketingHeader from "../../components/ticketing/TicketingHeader";
import BackgroundImg from "../../assets/ticketing/FicketingBg.png";
import { eventDetailStore } from "../../stores/EventStore";
import { useNavigate } from "react-router";
import { useStore } from "zustand";
import { releaseSlot } from "../../service/queue/api.ts";
import { Helmet } from "react-helmet-async";

const SelectDate: React.FC = () => {
  const eventDetail = useStore(eventDetailStore);
  const eventDate = Object.keys(eventDetail.scheduleMap);
  const [round, setRound] = useState<number | null>(null); // 선택된 날짜 상태
  const navi = useNavigate();


  const handleRoundSelect = (round: number) => {
    setRound(round); // 날짜 선택 시 choiceDate 상태 업데이트
  };

  const nextClick = () => {
    navi("/ticketing/select-seat");
  };

  useEffect(() => {
    // 창 닫힘 이벤트 처리
    const handleReleaseSlot = async () => {
      try {
        await releaseSlot(eventDetail.eventId);
        console.log("Slot released successfully.");
      } catch (error) {
        console.error("Error releasing slot:", error);
      }
    };

    window.addEventListener("unload", handleReleaseSlot);

    return () => {
      window.removeEventListener("unload", handleReleaseSlot);
    };
  }, []);

  return (
    <div>
      <Helmet>
        <title>티켓팅 - 관람일 선택</title>
      </Helmet>
      {/* pc 버전 */}
      <div
        className="w-[900px] h-[599px] min-w-[900px] min-h-[599px] bg-white hidden md:block"
        style={{
          backgroundImage: `url(${BackgroundImg})`,
          overflow: "hidden",
        }}
      >
        <div className="pb-3.5">
          <TicketingHeader step={1} />
        </div>
        <div className="flex w-full h-full justify-between overflow-hidden flex-grow">
          {/* 왼쪽 컴포넌트 */}
          <div className="w-[600px] h-[460px] bg-white  ml-[30px] mr-[10px]">
            <SelectDateComponent onRoundselected={handleRoundSelect} />
          </div>
          {/* 오른쪽 컴포넌트 */}
          <div className="w-[260px] h-[475px]  hidden md:block">
            <div className="w-[260px] bg-gray-10 shadow-md ">
              {/* 이미지 및 제목 */}
              <div className="flex border-b-[1px] border-gray-300">
                {/* 이미지 */}
                <img src={eventDetail.posterPcUrl} alt="" />
                {/* 제목 및 기간 */}
                <div className="ml-4 flex flex-col flex w-[100px]">
                  <p className="text-[13px] font-bold text-white break-words h-[70px]">
                    {eventDetail.title}
                  </p>
                  <p className="text-xs text-gray-500 mt-2">
                    {eventDate.at(-1)} ~ {eventDate[0]}
                    <br />
                    {eventDetail.stageName}
                    <br />
                    {eventDetail.age}
                    <br />
                    {eventDetail.runningTime} 분
                  </p>
                </div>
              </div>

              {/* 예약 정보 */}
              <div className="mt-4">
                <h3 className="text-sm font-semibold text-gray-800 border-b-[1px] border-gray-300 pb-2">
                  My예매정보
                </h3>
                <div className="flex flex-col w-[258px] h-[165px]">
                  <table>
                    <tr>
                      <td className="py-2 border-b-[1px] border-gray-300 w-[80px] text-center">
                        날짜
                      </td>
                      <td className="py-2 border-b-[1px] border-gray-300 bg-white w-[160px] text-center">
                        {eventDetail.choiceDate}
                      </td>
                    </tr>
                    <tr>
                      <td className="py-2 border-b-[1px] border-gray-300 w-[80px] text-center">
                        회차
                      </td>
                      <td className="py-2 border-b-[1px] border-gray-300 bg-white w-[160px] text-center">
                        {round}
                      </td>
                    </tr>
                    <tr>
                      <td className="border-b-[1px] border-gray-300" />
                      <td className="border-b-[1px] border-gray-300 bg-white h-[90px]" />
                    </tr>
                  </table>
                </div>
              </div>
              {/* 다음 단계 버튼 */}
              <div className="mt-4">
                <button
                  onClick={nextClick}
                  className="w-[255px]   bg-red-500 text-white py-2 shadow-lg text-[15px] font-semibold hover:bg-red-600 transition"
                >
                  다음단계
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      {/* 모바일 */}
      <div className="block md:hidden flex flex-col justify-center items-center">
        <TicketingHeader step={1} />
        <hr className="w-[350px] mt-[5px] border-t-2 border-gray-300" />
        <SelectDateComponent onRoundselected={handleRoundSelect} />
        <hr className="w-[350px] mt-[10px] border-t-2 border-gray-300" />
      </div>
    </div>
  );
};

export default SelectDate;
