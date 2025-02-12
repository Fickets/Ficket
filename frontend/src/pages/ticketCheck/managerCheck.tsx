import React, { useEffect, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import { SocketMessage } from "../../types/ticketCheck";
import testImage from "../../assets/nocontent.png";
import { ticketStatusChange } from "../../service/ticketCheck/ticketCheck";

const BROKER_URL: string = import.meta.env.BROKER_URL;

const ManagerCheckPage: React.FC = () => {
  const { eventId } = useParams<{ eventId: string }>();
  const location = useLocation();

  const queryParams = new URLSearchParams(location.search);
  const connectId = queryParams.get("connectId");

  const [, setClient] = useState<Client | null>(null);

  const [socketMessage, setSocketMessage] = useState<SocketMessage | null>(
    null,
  );

  useEffect(() => {
    const stored = localStorage.getItem("ADMIN_STORE");
    if (stored) {
      const obj = JSON.parse(stored);
      if (obj.state.accessToken !== "") {
        const token = obj.state.accessToken;
        // WebSocket 연결 설정
        const connectionOptions = {
          brokerURL: BROKER_URL,
          connectHeaders: {
            Authorization: token,
          }, // 연결 시 헤더 설정
          onConnect: () => {
            newClient.subscribe(
              `/sub/check/${eventId}/${connectId}`,
              (message) => {
                try {
                  // JSON 메시지를 파싱
                  const parsedMessage: SocketMessage = JSON.parse(message.body);
                  console.log(parsedMessage);
                  if (parsedMessage.data.message != null) {
                    setSocketMessage(parsedMessage);
                  } else {
                    setSocketMessage(null);
                  }
                  // 상태 업데이트
                } catch (error) {
                  console.error(
                    "Failed to parse message or invalid data",
                    error,
                  );
                }
              },
            );
          },
          onDisconnect: () => {},
        };
        const newClient = new Client();
        newClient.configure(connectionOptions);
        // 웹소켓 세션 활성화
        newClient.activate();
        setClient(newClient);
      }
    }
  }, []);

  const checkButton = async () => {
    if (socketMessage != null) {
      try {
        await ticketStatusChange(
          socketMessage?.data.ticket_id,
          eventId || "",
          Number(connectId),
        );
      } catch (error: any) {
        console.log(error);
      }
    }
  };

  return (
    <div className="flex flex-col items-center  bg-gray-400 min-h-screen">
      {/* 제목 섹션 */}
      <div className="mt-[30px] flex justify-between w-full px-[50px]">
        <p className="font-bold text-[30px]">Ficket Manager</p>
        <span className="ml-[15px] font-semibold text-[30px]">{connectId}</span>
      </div>

      {/* 이미지 섹션 */}
      <div className="mt-[50px] mx-[10px] w-[200px] h-[260px] bg-white flex items-center justify-center">
        <img
          src={socketMessage?.data.face_img || testImage}
          alt="Ticket Preview"
          className="object-cover w-full h-full"
        />
      </div>

      {/* 정보 섹션 */}
      <div className="flex mt-[50px] mx-[10px] w-[300px] bg-white border border-[#666666]">
        <div className="flex flex-col w-2/5 border-r border-[#666666]">
          <p className="ml-[5px] mt-[5px] text-[25px]">식별번호</p>
          <p className="ml-[5px] mt-[5px] text-[25px]">이름</p>
          <p className="ml-[5px] mt-[5px] text-[25px]">생년월일</p>
          <p className="ml-[5px] mt-[5px] text-[25px]">일치율</p>
          <p className="ml-[5px] mt-[5px] text-[25px]">좌석</p>
        </div>
        <div className="flex flex-col w-3/5">
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.data.ticket_id || "N/A"}
          </p>
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.name || "N/A"}
          </p>
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.birth || "N/A"}
          </p>
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.data.similarity || "N/A"}
          </p>
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.seatLoc || "N/A"}
          </p>
        </div>
      </div>

      {/* 버튼 섹션 */}
      <div className="my-[40px] bg-gray-400 py-5 w-full flex items-center justify-center">
        <button
          className="border border-white text-[30px] w-[100px] h-[50px] text-white bg-gray-800 hover:bg-gray-700"
          onClick={checkButton}
        >
          확인
        </button>
      </div>
    </div>
  );
};

export default ManagerCheckPage;
