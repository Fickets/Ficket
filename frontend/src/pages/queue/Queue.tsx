import { useEffect, useState } from "react";
import { useStore } from "zustand";
import { useNavigate, useParams } from "react-router-dom";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { MyQueueStatusResponse } from "../../types/queue";
import {
  enterQueue,
  leaveQueue,
  enterTicketing,
  getQueueStatus,
} from "../../service/queue/api.ts";

type Params = {
  eventId: string;
};

const Queue = () => {
  const { eventId } = useParams<Params>();
  const navigate = useNavigate();

  if (!eventId) return <div>No Event ID found</div>;

  const event = useStore(eventDetailStore);
  const choiceDate: string = event.choiceDate;

  const [message, setMessage] = useState<MyQueueStatusResponse>(
    {} as MyQueueStatusResponse,
  );
  const [initialWaitingNumber, setInitialWaitingNumber] = useState<
    number | null
  >(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const initQueue = async () => {
    try {
      await enterQueue(eventId);
    } catch (error) {
      console.error("대기열 진입 실패:", error);
    }
  };

  const cleanupQueue = async () => {
    try {
      await leaveQueue(eventId);
      console.log("대기열 나가기 성공");
    } catch (error) {
      console.error("대기열 나가기 실패:", error);
    }
  };

  // 5초 주기 polling
  useEffect(() => {
    initQueue();

    const interval = setInterval(async () => {
      try {
        const status = await getQueueStatus(eventId);
        setMessage(status);

        if (
          initialWaitingNumber === null &&
          status.myWaitingNumber !== undefined
        ) {
          setInitialWaitingNumber(status.myWaitingNumber);
        }

        // myWaitingNumber 0이면 티켓팅 시도
        if (status.myWaitingNumber === 0) {
          const entered = await enterTicketing(eventId);
          if (entered) {
            navigate(
              choiceDate ? "/ticketing/select-seat" : "/ticketing/select-date",
            );
          }
        }

        // canEnter true면 바로 이동
        if (status.canEnter) {
          navigate(
            choiceDate ? "/ticketing/select-seat" : "/ticketing/select-date",
          );
        }
      } catch (error) {
        console.error("대기열 상태 조회 실패:", error);
      }
    }, 5000);

    setTimeout(() => setIsLoading(false), 1000);

    // cleanup: 컴포넌트 언마운트 + 새로고침 / 브라우저 종료
    const handleBeforeUnload = async () => {
      await cleanupQueue();
    };

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      clearInterval(interval);
      cleanupQueue();
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [eventId, navigate, choiceDate, initialWaitingNumber]);

  const calculateProgress = (): number => {
    if (initialWaitingNumber === null || message.myWaitingNumber === undefined)
      return 0;
    const progress =
      ((initialWaitingNumber - message.myWaitingNumber) /
        initialWaitingNumber) *
      100;
    return Math.max(0, Math.min(100, progress));
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
            <h1 className="text-xl font-bold mb-1 text-black">
              접속 인원이 많아 대기 중입니다.
            </h1>
            <h2 className="text-lg text-purple-500 mb-1">
              조금만 기다려주세요.
            </h2>

            <h3 className="text-sm font-normal text-gray-500">{event.title}</h3>
            <img
              src={event.posterPcUrl}
              alt="poster"
              className="w-[120px] h-[150px] mb-6 border border-gray-300 mx-auto"
            />

            <div className="border border-gray-300 rounded-lg p-4">
              <h4 className="font-bold mb-2 text-center">나의 대기 순서</h4>
              <h1 className="text-5xl text-center font-bold text-black">
                {message.myWaitingNumber?.toLocaleString() ?? "-"}
              </h1>

              <div className="relative w-full h-5 bg-gray-200 rounded-full mt-4">
                <div
                  className="absolute top-0 left-0 h-full rounded-full bg-purple-500"
                  style={{ width: `${calculateProgress()}%` }}
                ></div>
              </div>

              <div className="border-t border-gray-300 mt-4"></div>

              <div className="flex justify-between mt-2 text-sm text-gray-500">
                <span>현재 대기인원</span>
                <span className="font-bold">
                  {message.totalWaitingNumber?.toLocaleString() ?? "-"}명
                </span>
              </div>
            </div>

            <p className="mt-4 text-xs text-gray-500 leading-relaxed">
              * 잠시만 기다리시면 예매하기 페이지로 연결됩니다.
              <br />* 새로고침하거나 재접속하시면 대기순서가 초기화되어 더
              길어질 수 있습니다.
            </p>
          </>
        )}
      </div>
    </div>
  );
};

export default Queue;
