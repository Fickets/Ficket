import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Helmet } from "react-helmet-async";
import { motion } from "framer-motion";
import { Loader2 } from "lucide-react";

function OrderComplete() {
  const navigate = useNavigate();

  useEffect(() => {
    // 3초 후 마이티켓 페이지로 이동
    const timer = setTimeout(() => {
      navigate("/my-ticket");
    }, 3000);

    return () => clearTimeout(timer);
  }, [navigate]);

  return (
    <div className="relative w-full h-screen flex flex-col justify-center items-center bg-white px-6">
      <Helmet>
        <title>티켓팅 - 결제 완료</title>
      </Helmet>

      {/* 부드러운 페이드 인 효과 */}
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

      {/* 로딩 아이콘 */}
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
