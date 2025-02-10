import { useEffect, useState, useRef } from "react";
import { useStore } from "zustand";
import { useNavigate, useParams } from "react-router-dom";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { MyQueueStatusResponse, QueueStatus } from "../../types/queue.ts";
import { userStore } from "../../stores/UserStore.tsx";
import { enterQueue } from "../../service/queue/api.ts";

type Params = {
  eventId: string;
};

const Queue = () => {
  const { eventId } = useParams<Params>();

  if (!eventId) {
    return <div>No Event ID found</div>;
  }

  const event = useStore(eventDetailStore);
  const user = useStore(userStore);
  const navigate = useNavigate();
  const choiceDate: string = event.choiceDate;

  // 상태 관리
  const [message, setMessage] = useState<MyQueueStatusResponse>(
    {} as MyQueueStatusResponse,
  );
  const [initialWaitingNumber, setInitialWaitingNumber] = useState<
    number | null
  >(null); // 최초 대기 번호 저장
  const [socket, setSocket] = useState<WebSocket | null>(null); // WebSocket 인스턴스
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const isInitialSet = useRef<boolean>(false);

  // WebSocket 연결 함수
  const connectWebSocket = (token: string) => {
    const encodedToken = encodeURIComponent(token);
    const WEBSOCKET_URL = `ws://ec2-54-180-138-77.ap-northeast-2.compute.amazonaws.com:9000/queue-status/${eventId}?Authorization=${encodedToken}`;
    const ws = new WebSocket(WEBSOCKET_URL);

    ws.onopen = () => {
      console.log("WebSocket 연결 성공");
    };

    ws.onmessage = (event: any) => {
      try {
        const data: MyQueueStatusResponse = JSON.parse(event.data);
        // 최초 1회만 setInitialWaitingNumber 호출
        if (!isInitialSet.current && data.myWaitingNumber !== undefined) {
          setInitialWaitingNumber(data.myWaitingNumber);
          isInitialSet.current = true; // 최초 실행 이후 플래그 변경
        }

        setMessage((prevMessage) => ({
          ...prevMessage,
          ...data,
        }));

        // setMessage(data);

        if (data.queueStatus === QueueStatus.COMPLETED) {
          navigate(
            choiceDate ? "/ticketing/select-seat" : "/ticketing/select-date",
          );
        }
      } catch (error) {
        console.error("WebSocket 메시지 파싱 실패:", error);
      }
    };

    ws.onclose = (event: any) => {
      console.log("WebSocket 연결 종료:", event.reason);
    };

    ws.onerror = (error: any) => {
      console.error("WebSocket 오류:", error);
    };

    setSocket(ws);
    return ws;
  };

  const initEnterQueue = async () => {
    try {
      await enterQueue(eventId); // 대기열 재진입 API 호출

      // WebSocket 재연결
      connectWebSocket(user.accessToken);
    } catch (error) {
      console.error("대기열 진입 실패:", error);
    }
  };

  useEffect(() => {
    // 대기열 재진입 및 WebSocket 연결
    initEnterQueue();

    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 3000);

    // 컴포넌트 언마운트 시 WebSocket 연결 닫기
    return () => {
      clearTimeout(timer);
      if (socket) {
        socket.close();
        console.log("WebSocket 연결 해제");
      }
    };
  }, []); // 빈 의존성 배열로 새로고침 시 초기화

  const calculateProgress = (): number => {
    if (
      initialWaitingNumber === null ||
      message.myWaitingNumber === undefined
    ) {
      return 0; // 초기값이 없으면 진행률 0%
    }
    const progress =
      ((initialWaitingNumber - message.myWaitingNumber) /
        initialWaitingNumber) *
      100;
    return Math.max(0, Math.min(100, progress)); // 0% ~ 100% 사이로 제한
  };

  return (
    <div className="w-full flex justify-center mt-10 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-sm text-left">
        {isLoading ? (
          <div className="text-center">
            <div className="animate-spin h-8 w-8 border-4 border-gray-400 border-t-transparent rounded-full mx-auto"></div>
            <p className="mt-4 text-lg text-gray-500">
              대기열을 불러오는 중...
            </p>
          </div>
        ) : (
          <>
            {message.queueStatus === QueueStatus.ALMOST_DONE ? (
              <div>
                <h1 className="text-xl font-bold text-black mb-1">
                  곧 고객님의 순서가 다가옵니다!
                </h1>
                <h2 className="text-lg text-red-500 mb-1">
                  예매를 준비해주세요.
                </h2>
              </div>
            ) : (
              <div>
                <h1 className="text-xl font-bold mb-1 text-black">
                  접속 인원이 많아 대기 중입니다.
                </h1>
                <h2 className="text-lg text-purple-500 mb-1">
                  조금만 기다려주세요.
                </h2>
              </div>
            )}

            <h3 className="text-sm font-normal text-gray-500 mb-2">
              {event.title}
            </h3>
            <img
              src={event.posterPcUrl}
              alt="poster"
              className="w-[120px] h-[150px] mb-6 border border-gray-300 mx-auto"
            />

            <div className="border border-gray-300 rounded-lg p-4">
              <h4 className="font-bold mb-2 text-center">나의 대기 순서</h4>
              <h1 className="text-5xl text-center font-bold text-black">
                {message.myWaitingNumber?.toLocaleString() || "-"}
              </h1>

              <div className="relative w-full h-5 bg-gray-200 rounded-full mt-4">
                <div
                  className={`absolute top-0 left-0 h-full rounded-full ${
                    message.queueStatus === QueueStatus.ALMOST_DONE
                      ? "bg-red-500"
                      : "bg-purple-500"
                  }`}
                  style={{
                    width: `${calculateProgress()}%`,
                  }}
                ></div>
              </div>

              <div className="border-t border-gray-300 mt-4"></div>

              <div className="flex justify-between mt-2 text-sm text-gray-500">
                <span>현재 대기인원</span>
                <span className="font-bold">
                  {message.totalWaitingNumber?.toLocaleString() || "-"}명
                </span>
              </div>
            </div>

            <p className="mt-4 text-xs text-gray-500 leading-relaxed">
              * 잠시만 기다리시면 예매하기 페이지로 연결됩니다. <br />*
              새로고침하거나 재접속하시면 대기순서가 초기화되어 더 길어질 수
              있습니다.
            </p>
          </>
        )}
      </div>
    </div>
  );
};

export default Queue;
