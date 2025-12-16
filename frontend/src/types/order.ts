export interface CreateOrderRequest {
  paymentId: string;
  eventScheduleId: number;
  faceId: number;
  faceImgUrl: string;
  selectSeatInfoList: SelectSeatInfo[];
}

export interface CreateOrderResponse {
  orderId: number;
  orderStatus: OrderStatus;
}

export interface SelectSeatInfo {
  seatMappingId: number;
  seatPrice: number;
  seatGroupId: number;
}

export interface OrderStatusResponse {
  orderStatus: OrderStatus;
}

export enum OrderStatus {
  INPROGRESS = "INPROGRESS",
  COMPLETED = "COMPLETED",
  CANCELLED = "CANCELLED",
  REFUNDED = "REFUNDED",
}
