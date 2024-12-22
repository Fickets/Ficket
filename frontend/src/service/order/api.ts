import { privateApi } from '../../utils/http-common.ts';
import { AxiosResponse } from 'axios';
import {
  CreateOrderRequest,
  OrderStatus,
  OrderStatusResponse,
} from '../../types/order.ts';

// order 생성
export const createOrder = async (
  createOrderRequest: CreateOrderRequest
): Promise<OrderStatus> => {
  try {
    // 주문 생성 요청
    const response: AxiosResponse<number> = await privateApi.post(
      '/ticketing/order',
      createOrderRequest,
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    // 성공적으로 생성된 주문 ID를 추출
    const orderId: number = response.data;

    console.log('Order created successfully:', orderId);

    // 생성된 주문 ID를 기반으로 조회 API 호출
    const orderStatusResponse: AxiosResponse<OrderStatusResponse> =
      await privateApi.get(`/ticketing/order/${orderId}`);

    // 주문 상태 확인
    const orderStatus = orderStatusResponse.data.orderStatus;
    if (orderStatus !== OrderStatus.INPROGRESS) {
      console.error(`Order status is not valid: ${orderStatus}`);
      throw new Error(`Order processing failed. Status: ${orderStatus}`);
    }

    // 상태가 "INPROGRESS"인 경우에만 데이터 반환
    return orderStatusResponse.data.orderStatus;
  } catch (error) {
    console.error('Error while creating order or fetching details:', error);
    throw error; // 에러 전파
  }
};
