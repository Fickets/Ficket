import { privateApi } from "../../utils/http-common.ts";
import { MyQueueStatusResponse } from "../../types/queue.ts";

// 대기열 진입 API
export const enterQueue = async (eventId: string): Promise<void> => {
  try {
    await privateApi.post<void>(`/queues/${eventId}/enter-queue`);
  } catch (error) {
    console.error("대기열 진입 실패:", error);
    throw error;
  }
};

export const leaveQueue = async (eventId: string): Promise<void> => {
  try {
    await privateApi.post<void>(`/queues/${eventId}/leave-queue`);
  } catch (error) {
    console.error("대기열 나가기 실패:", error);
    throw error;
  }
};

export const enterTicketing = async (eventId: string): Promise<boolean> => {
  try {
    const response = await privateApi.post(
      `/queues/${eventId}/enter-ticketing`,
    );
    return response.data;
  } catch (error) {
    console.error(`티켓팅 진입 실패 ${eventId}:`, error);
    return false;
  }
};

export const getQueueStatus = async (
  eventId: string,
): Promise<MyQueueStatusResponse> => {
  try {
    const response = await privateApi.get(`/queues/${eventId}/my-status`);
    return response.data;
  } catch (error) {
    console.error(`나의 대기열 상태 조회 실패 ${eventId}:`, error);
    throw error;
  }
};

export const checkTicketingStatus = async (
  eventId: string,
): Promise<boolean> => {
  try {
    const response = await privateApi.get<boolean>(`/queues/${eventId}/check`);
    return response.data;
  } catch (error) {
    console.error(`예매 화면 접속 상태 확인 실패 ${eventId}:`, error);
    return false;
  }
};
