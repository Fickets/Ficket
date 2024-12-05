import PortOne from "@portone/browser-sdk/v2";
import { useState } from "react";
import { useEventStore } from "../../types/StoreType/EventState.ts";

const STORE_ID: string = import.meta.env.VITE_STORE_ID;
const CHANNEL_KEY: string = import.meta.env.VITE_CHANNEL_KEY;

const KakaoPay = () => {
  const {
    faceImg,
    eventScheduleId,
    selectedSeats,
    eventTitle,
    eventStage,
    eventDate,
    eventTime,
  } = useEventStore();
  const [isWaitingPayment, setWaitingPayment] = useState<boolean>(false);
  const [totalAmount, setTotalAmount] = useState<number>(100000000);

  const randomId = () => {
    return Array.from(crypto.getRandomValues(new Uint32Array(2)))
      .map((word) => word.toString(16).padStart(8, "0"))
      .join("");
  };

  const getTotalAmount = (selectedSeats: any) => {
    const totalAmount = fetchTotalAmountByUser(selectedSeats); // 레디스 조회
    setTotalAmount(totalAmount);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    e.preventDefault();
    setWaitingPayment(true);
    const paymentId = randomId();

    try {
      const payment: any = await PortOne.requestPayment({
        storeId: STORE_ID,
        paymentId: paymentId,
        orderName: "VIP 2매, R석 2매",
        totalAmount: 100,
        currency: "CURRENCY_KRW",
        channelKey: CHANNEL_KEY,
        payMethod: "EASY_PAY",
        easyPay: {
          easyPayProvider: "EASY_PAY_PROVIDER_KAKAOPAY",
        },
        bypass: {
          kakaopay: {
            custom_message: `${eventTitle} - ${eventDate} ${eventTime} (${eventStage})`,
          },
        },
        windowType: {
          pc: "IFRAME",
          mobile: "REDIRECTION",
        },
        redirectUrl: "http://localhost:5137/order/payment-result",
      });
    } catch (error) {
      console.error("Error requesting payment:", error);
    } finally {
      setWaitingPayment(false);
    }
  };

  return (
    <div>
      <button
        onClick={handleSubmit}
        className="w-full bg-red-500 text-white py-3 border border-black font-semibold text-lg"
      >
        {" "}
        {isWaitingPayment ? "결제 진행 중..." : `${totalAmount} 결제하기`}
      </button>
    </div>
  );
};

export default KakaoPay;
