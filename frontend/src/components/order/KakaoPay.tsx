import PortOne from "@portone/browser-sdk/v2";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { createOrder } from "../../service/order/api.ts";
import { useStore } from "zustand/index";
import { eventDetailStore } from "../../stores/EventStore.tsx";

const STORE_ID: string = import.meta.env.VITE_STORE_ID;
const CHANNEL_KEY: string = import.meta.env.VITE_CHANNEL_KEY;

const KakaoPay = () => {
  const navigate = useNavigate();

  const event = useStore(eventDetailStore);

  const faceImg = event.faceImg;
  const selectedSeats = event.selectedSeats;
  const eventScheduleId = event.scheduleId;
  const eventTitle = event.title;
  const eventDate = event.choiceDate;
  const eventTime = event.choiceTime;
  const eventStage = event.stageName;

  const [isWaitingPayment, setWaitingPayment] = useState<boolean>(false);
  const [paymentStatus, setPaymentStatus] = useState<string>("IDLE");
  const totalAmount = selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
  const customMessage = selectedSeats
    .map((seat) => `${seat.grade}석 ${seat.row}열 ${seat.col}번`)
    .join(", ");

  const randomId = () => {
    return Array.from(crypto.getRandomValues(new Uint32Array(2)))
      .map((word) => word.toString(16).padStart(8, "0"))
      .join("");
  };

  useEffect(() => {
    if (paymentStatus === "PAID") {
      alert("결제가 성공적으로 완료되었습니다!");
    } else if (paymentStatus === "FAILED") {
      alert("결제가 실패하였습니다. 다시 시도해주세요.");
    } else if (paymentStatus === "ERROR") {
      alert("결제 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
  }, [paymentStatus]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setWaitingPayment(true);

    const paymentId = randomId();
    console.log(`Generated paymentId: ${paymentId}`);

    try {
      console.log("Attempting to connect to SSE...");
      const eventSource = new EventSource(
        `http://localhost:9000/api/v1/ticketing/order/subscribe/${paymentId}`,
      );

      eventSource.onopen = () => {
        console.log("SSE connection opened successfully!");
      };

      eventSource.onmessage = (event) => {
        console.log(`Received message from SSE: ${event.data}`);
        const data = JSON.parse(event.data);
        setPaymentStatus(data.status);

        if (data.status === "PAID" || data.status === "FAILED") {
          eventSource.close();
          setWaitingPayment(false);
        }
      };

      eventSource.onerror = (error) => {
        console.error("SSE connection error:", error);
        setPaymentStatus("ERROR");
        setWaitingPayment(false);
        eventSource.close();
      };

      console.log("Creating order...");
      const createOrderRequest = {
        paymentId: paymentId,
        eventScheduleId: eventScheduleId,
        selectSeatInfoList: Array.from(
          new Set(
            selectedSeats.map((seat) =>
              JSON.stringify({
                seatMappingId: seat.seatMappingId,
                seatPrice: seat.price,
                seatGrade: seat.grade,
              }),
            ),
          ),
        ).map((item) => JSON.parse(item)),
      };

      const formData = new FormData();

      if (faceImg) {
        formData.append("userFaceImg", faceImg);
      } else {
        alert("얼굴 이미지를 가져오지 못했습니다. 다시 시도해주세요.");
        setWaitingPayment(false);
        navigate(-1);
        return;
      }

      formData.append(
        "createOrderRequest",
        new Blob([JSON.stringify(createOrderRequest)], {
          type: "application/json",
        }),
      );

      const orderDetails = await createOrder(formData);
      console.log("Order created successfully:", orderDetails);

      console.log("Sending payment request to PortOne...");
      await PortOne.requestPayment({
        storeId: STORE_ID,
        paymentId: paymentId,
        orderName: `${eventTitle} - ${eventDate} ${eventTime} (${eventStage})`,
        totalAmount: totalAmount,
        currency: "CURRENCY_KRW",
        channelKey: CHANNEL_KEY,
        payMethod: "EASY_PAY",
        easyPay: {
          easyPayProvider: "EASY_PAY_PROVIDER_KAKAOPAY",
        },
        bypass: {
          kakaopay: {
            custom_message: customMessage,
          },
        },
        windowType: {
          pc: "IFRAME",
          mobile: "REDIRECTION",
        },
        noticeUrls: [
          "https://4967-218-39-17-13.ngrok-free.app/api/v1/ticketing/order/valid",
        ],
      });

      console.log("Payment request sent successfully!");
    } catch (error) {
      console.error("Error creating order or requesting payment:", error);
      setPaymentStatus("ERROR");
      setWaitingPayment(false);
    }
  };

  return (
    <div>
      <button
        onClick={handleSubmit}
        className="w-full bg-red-500 text-white py-3 border border-black font-semibold text-lg"
        disabled={isWaitingPayment}
      >
        {isWaitingPayment
          ? "결제 진행 중..."
          : `${totalAmount.toLocaleString("ko-KR")} 원 결제하기`}
      </button>
    </div>
  );
};

export default KakaoPay;
