import React, { useEffect, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import { SocketMessage } from "../../types/ticketCheck";
import testImage from "../../assets/nocontent.png";
import { ticketStatusChange } from "../../service/ticketCheck/ticketCheck";
import { Helmet } from "react-helmet-async";

const BROKER_URL: string = import.meta.env.VITE_BROKER_URL;

const ManagerCheckPage: React.FC = () => {
  const { eventId } = useParams<{ eventId: string }>();
  const location = useLocation();

  const queryParams = new URLSearchParams(location.search);
  const connectId = queryParams.get("connectId");

  const [, setClient] = useState<Client | null>(null);
  const [socketMessage, setSocketMessage] = useState<SocketMessage | null>(
    null,
  );
  const [showFullSeats, setShowFullSeats] = useState(false); // "더보기" 상태 관리

  useEffect(() => {
    const stored = localStorage.getItem("ADMIN_STORE");
    if (stored) {
      const obj = JSON.parse(stored);
      if (obj.state.accessToken !== "") {
        const token = obj.state.accessToken;
        const connectionOptions = {
          brokerURL: BROKER_URL,
          connectHeaders: {
            Authorization: token,
          },
          onConnect: () => {
            newClient.subscribe(
              `/sub/check/${eventId}/${connectId}`,
              (message) => {
                try {
                  const parsedMessage: SocketMessage = JSON.parse(message.body);
                  if (parsedMessage.data.message == null) {
                    setSocketMessage(parsedMessage);
                  } else {
                    setSocketMessage(null);
                  }
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

  // 일치율 상태 변환 함수
  const getSimilarityStatus = (similarity: number | undefined) => {
    if (similarity === undefined) return "";
    if (similarity <= 0.4) return "❌ 불일치";
    if (similarity < 0.5) return "⚠️ 일치하나 필요 시 재확인";
    return "✅ 일치";
  };

  // 좌석 정보 포맷팅 (배열을 그대로 반환)
  const seatList = socketMessage?.seatLoc ?? []; // 좌석 정보가 없으면 빈 배열 반환
  const displayedSeats = showFullSeats ? seatList : seatList.slice(0, 4); // 처음 4개만 표시

  return (
    <div className="flex flex-col items-center bg-gray-400 min-h-screen">
      <Helmet>
        <title>티켓 정보 확인</title>
      </Helmet>
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
          <p className="ml-[5px] mt-[5px] text-[25px]">출생연도</p>
          <p className="ml-[5px] mt-[5px] text-[25px]">일치 여부</p>
          <p className="ml-[5px] mt-[5px] text-[25px]">좌석</p>
        </div>
        <div className="flex flex-col w-3/5">
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.data.ticket_id || ""}
          </p>
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.name || ""}
          </p>
          <p className="ml-[5px] mt-[5px] text-[25px]">
            {socketMessage?.birth || ""}
          </p>
          <p className="ml-[5px] mt-[5px] text-[25px] font-bold">
            {getSimilarityStatus(socketMessage?.data.similarity)}
          </p>
          <div className="ml-[5px] mt-[5px] text-[25px] whitespace-pre-line">
            {seatList.length > 0 ? (
              <>
                {displayedSeats.join("\n")}
                {seatList.length > 4 && (
                  <button
                    className="mt-2 text-blue-500 underline text-[20px]"
                    onClick={() => setShowFullSeats(!showFullSeats)}
                  >
                    {showFullSeats
                      ? "접기"
                      : `더보기 (${seatList.length - 4}석)`}
                  </button>
                )}
              </>
            ) : (
              ""
            )}
          </div>
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
