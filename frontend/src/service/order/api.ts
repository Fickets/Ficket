import { privateApi } from "../../utils/http-common.ts";
import { AxiosResponse } from "axios";
import {
  CreateOrderRequest,
  CreateOrderResponse,
  OrderStatusResponse,
} from "../../types/order.ts";

// order 생성 - orderId와 orderStatus 반환
export const createOrder = async (
  createOrderRequest: CreateOrderRequest,
): Promise<CreateOrderResponse> => {
  try {
    // 주문 생성 요청
    const response: AxiosResponse<CreateOrderResponse> = await privateApi.post(
      "/ticketing/order",
      createOrderRequest,
      {
        headers: {
          "Content-Type": "application/json",
        },
      },
    );

    return response.data;
  } catch (error) {
    console.error("Error while creating order or fetching details:", error);
    throw error; // 에러 전파
  }
};

export const getOrderStatus = async (orderId: number) => {
  // 주문 ID를 기반으로 조회 API 호출
  const orderStatusResponse: AxiosResponse<OrderStatusResponse> =
    await privateApi.get(`/ticketing/order/${orderId}`);

  // 주문 상태 확인
  return orderStatusResponse.data.orderStatus;
};
