import { useEffect } from "react";
import { useStore } from "zustand/index";
import { eventDetailStore } from "../../stores/EventStore.tsx";
import { userStore } from "../../stores/UserStore.tsx";
import { useNavigate } from "react-router-dom";
import { Helmet } from "react-helmet-async";
import { useMediaQuery } from "react-responsive";
import NotFound from "../errorpage/NotFound.tsx";
import { motion } from "framer-motion";
import { Loader2 } from "lucide-react";

const WORK_WEBSOCKET_URL: string = import.meta.env.VITE_WORK_WEBSOCKET_URL;

function OrderComplete() {
  const navigate = useNavigate();
  const event = useStore(eventDetailStore);
  const user = useStore(userStore);

  const eventId = event.eventId;
  const eventScheduleId = event.scheduleId;

  // ✅ 모바일 환경 여부 체크 (768px 이하만 true)
  const isMobile = useMediaQuery({ maxWidth: 767 });

  useEffect(() => {
    if (!isMobile) return; // ✅ 모바일이 아니면 WebSocket 연결 X

    const connectWebSocket = () => {
      const encodedToken = encodeURIComponent(user.accessToken);
      const WEBSOCKET_URL = `${WORK_WEBSOCKET_URL}/${eventId}/${eventScheduleId}?Authorization=${encodedToken}`;
      const ws = new WebSocket(WEBSOCKET_URL);

      ws.onopen = () => {
        console.log("WebSocket 연결 성공");
        setTimeout(() => navigate("/my-ticket"), 3000); // ✅ 3초 후 /my-ticket 이동
      };

      ws.onmessage = (event: MessageEvent) => {
        console.log("WebSocket 메시지:", event);
      };

      ws.onclose = () => {
        console.log("WebSocket 연결 종료");
      };

      ws.onerror = (error) => {
        console.error("WebSocket 오류:", error);
      };

      return ws;
    };

    const wsInstance = connectWebSocket();

    return () => {
      wsInstance.close();
    };
  }, [isMobile]);

  // ✅ PC 환경에서는 NotFound 페이지로 이동
  if (!isMobile) {
    return <NotFound />;
  }

  return (
    <div className="relative w-full h-screen flex flex-col justify-center items-center bg-white px-6">
      <Helmet>
        <title>티켓팅 - 결제 완료</title>
      </Helmet>

      {/* ✅ 부드러운 페이드 인 효과 */}
      <motion.div
        className="text-center"
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <h2 className="text-2xl sm:text-3xl font-semibold text-gray-900">
          결제가 완료되었습니다! 🎉
        </h2>
        <p className="text-gray-600 mt-3 text-sm sm:text-base">
          잠시 후 <span className="font-semibold text-red-500">마이티켓</span>{" "}
          페이지로 이동합니다.
        </p>
      </motion.div>

      {/* ✅ 로딩 아이콘 추가 */}
      <motion.div
        className="mt-6"
        animate={{ rotate: 360 }}
        transition={{ repeat: Infinity, duration: 1 }}
      >
        <Loader2 className="w-8 h-8 text-red-500 animate-spin" />
      </motion.div>
    </div>
  );
}

export default OrderComplete;
