import React, { useEffect, useState } from "react";

import SelectDateComponent from "../../components/ticketing/SelectDateComponent";
import TicketingHeader from "../../components/ticketing/TicketingHeader";
import BackgroundImg from "../../assets/ticketing/FicketingBg.png";
import { eventDetailStore } from "../../stores/EventStore";
import { useNavigate } from "react-router";
import { useStore } from "zustand";
import { releaseSlot } from "../../service/queue/api.ts";
import { Helmet } from "react-helmet-async";
import { WorkStatus } from "../../types/queue.ts";
import { userStore } from "../../stores/UserStore.tsx";

const WORK_WEBSOCKET_URL: string = import.meta.env.VITE_WORK_WEBSOCKET_URL;

const SelectDate: React.FC = () => {
  const eventDetail = useStore(eventDetailStore);
  const eventDate = Object.keys(eventDetail.scheduleMap);
  const [round, setRound] = useState<number | null>(null); // 선택된 날짜 상태
  const navi = useNavigate();
  const eventId = eventDetail.eventId;
  const eventScheduleId = eventDetail.scheduleId;
  const user = useStore(userStore);

  const handleRoundSelect = (round: number) => {
    setRound(round); // 날짜 선택 시 choiceDate 상태 업데이트
  };

  const nextClick = () => {
    notifyNavigation("NEXT_STEP");
    navi("/ticketing/select-seat");
  };

  let wsInstance: WebSocket | null = null;

  // 페이지 이동 시 웹소켓 메시지 전송
  const notifyNavigation = (message: string) => {
    if (wsInstance?.readyState === WebSocket.OPEN) {
      wsInstance.send(message);
      console.log("WebSocket 이동 메시지 전송:", message);
    }
  };

  const connectWebSocket = () => {
    const encodedToken = encodeURIComponent(user.accessToken);
    const WEBSOCKET_URL = `${WORK_WEBSOCKET_URL}/${eventId}/${eventScheduleId}?Authorization=${encodedToken}`;
    const ws = new WebSocket(WEBSOCKET_URL);

    ws.onopen = () => {
      console.log("WebSocket 연결 성공");
    };

    ws.onmessage = (event: MessageEvent) => {
      const handleMessage = async () => {
        try {
          if (event.data === WorkStatus.ORDER_RIGHT_LOST) {
            await releaseSlot(eventId);
            alert("세션이 만료되었습니다. 창을 닫습니다.");
            ws.close();
            window.close();
          }
        } catch (error) {
          console.error("WebSocket 메시지 처리 중 오류 발생:", error);
        }
      };

      handleMessage();
    };

    ws.onclose = (event: CloseEvent) => {
      console.log("WebSocket 연결 종료:", event.reason);
    };

    ws.onerror = (error: Event) => {
      console.error("WebSocket 오류:", error);
    };

    return ws;
  };

  useEffect(() => {
    wsInstance = connectWebSocket();

    return () => {
      wsInstance?.close();
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
