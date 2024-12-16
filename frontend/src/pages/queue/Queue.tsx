import { useEffect, useState } from "react";
import { useStore } from "zustand";
import { useNavigate } from "react-router-dom";
import { eventDetailStore } from "../../stores/EventStore.tsx";

// 웹소켓 설정
const WEBSOCKET_URL = "ws://your-websocket-url";

// 임의의 랜덤 값 생성 함수
const getRandomNumber = (min: number, max: number): number => {
  return Math.floor(Math.random() * (max - min + 1)) + min;
};

const Queue = () => {
  const event = useStore(eventDetailStore);
  const navigate = useNavigate(); // 페이지 이동을 위해 사용

  const choiceDate: string = event.choiceDate;

  // 상태 관리
  const [queueNumber, setQueueNumber] = useState<number>(
    getRandomNumber(1, 100000),
  );
  const [totalQueue, setTotalQueue] = useState<number>(
    getRandomNumber(1000, 5000000),
  );
  const [status, setStatus] = useState<"waiting" | "imminent" | "end">(
    "waiting",
  );

  useEffect(() => {
    const socket = new WebSocket(WEBSOCKET_URL);

    socket.onopen = () => {
      console.log("WebSocket connected");
    };

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);

      // 상태 업데이트
      if (data.status) setStatus(data.status);
      if (data.queueNumber) setQueueNumber(data.queueNumber);
      if (data.totalQueue) setTotalQueue(data.totalQueue);

      // 상태가 'end'이면 좌석 선택 페이지로 이동
      if (data.status === "end") {
        if (choiceDate) {
          navigate("/ticket/select-seat");
        } else {
          navigate("/ticket/select-date");
        }
      }
    };

    socket.onclose = () => {
      console.log("WebSocket disconnected");
    };

    // 페이지 종료 또는 새로고침 제어
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      // 사용자 창 닫기/새로고침 시 서버에 알림 메시지 전송
      socket.send(
        JSON.stringify({ type: "disconnect", reason: "user_closed" }),
      );
      socket.close();

      // 사용자에게 경고 메시지 표시
      event.preventDefault();
      event.returnValue = ""; // 표준 경고 메시지 표시
    };

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      socket.close();
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [navigate]);

  return (
    <div className="w-full flex justify-center mt-10 px-4 sm:px-6 lg:px-8">
      {/* 메인 컨테이너 */}
      <div className="w-full max-w-sm text-left">
        {status === "imminent" ? (
          // 상태가 imminent일 때
          <div>
            <h1 className="text-xl font-bold text-black mb-1">
              곧 고객님의 순서가 다가옵니다!
            </h1>
            <h2 className="text-lg text-red-500 mb-1">예매를 준비해주세요.</h2>
          </div>
        ) : (
          // 상태가 waiting일 때
          <div>
            <h1 className="text-xl font-bold mb-1 text-black">
              접속 인원이 많아 대기 중입니다.
            </h1>
            <h2 className="text-lg text-purple-500 mb-1">
              조금만 기다려주세요.
            </h2>
          </div>
        )}

        {/* 이벤트 제목과 포스터 */}
        <h3 className="text-sm font-normal text-gray-500 mb-2">
          {event.title}
        </h3>
        <img
          src={event.posterPcUrl}
          alt="poster"
          className="w-[120px] h-[150px] mb-6 border border-gray-300 mx-auto"
        />

        {/* 대기 상태 박스 */}
        <div className="border border-gray-300 rounded-lg p-4">
          <h4 className="font-bold mb-2 text-center">나의 대기 순서</h4>
          <h1 className="text-5xl text-center font-bold text-black">
            {queueNumber?.toLocaleString()}
          </h1>

          {/* 진행률 바 */}
          <div className="relative w-full h-5 bg-gray-200 rounded-full mt-4">
            <div
              className={`absolute top-0 left-0 h-full rounded-full ${
                status === "imminent" ? "bg-red-500" : "bg-purple-500"
              }`}
              style={{
                width: `${
                  totalQueue
                    ? Math.min(100, 100 - (queueNumber / totalQueue) * 100)
                    : 0
                }%`,
              }}
            ></div>
          </div>

          <div className="border-t border-gray-300 mt-4"></div>

          {/* 총 대기 인원 */}
          <div className="flex justify-between mt-2 text-sm text-gray-500">
            <span>현재 대기인원</span>
            <span className="font-bold">{totalQueue?.toLocaleString()}명</span>
          </div>
        </div>

        {/* 안내 문구 */}
        <p className="mt-4 text-xs text-gray-500 leading-relaxed">
          * 잠시만 기다리시면 예매하기 페이지로 연결됩니다. <br />*
          새로고침하거나 재접속하시면 대기순서가 초기화되어 더 길어질 수
          있습니다.
        </p>
      </div>
    </div>
  );
};

export default Queue;
