import { privateApi } from "../../utils/http-common.ts";

// 대기열 진입 API
export const enterQueue = async (eventId: string): Promise<void> => {
  try {
    await privateApi.get<void>(`/queues/${eventId}/enter`);
  } catch (error) {
    console.error("대기열 진입 실패:", error);
    throw error;
  }
};

export const canEnterTicketingPage = async (
  eventId: string,
): Promise<boolean> => {
  try {
    const response = await privateApi.get(`/queues/${eventId}/can-enter`);
    return response.data;
  } catch (error) {
    console.error(`이벤트에 대한 대기열 항목 확인 오류 ${eventId}:`, error);
    return false; // 기본적으로 실패 시 false 반환
  }
};

export const occupySlot = async (eventId: string): Promise<boolean> => {
  try {
    const response = await privateApi.post(`/queues/${eventId}/occupy-slot`);
    return response.data;
  } catch (error) {
    console.error(`Error occupying slot for event ${eventId}:`, error);
    return false; // 기본적으로 실패 시 false 반환
  }
};

export const releaseSlot = async (eventId: string): Promise<void> => {
  try {
    await privateApi.delete(`/queues/${eventId}/release-slot`);
  } catch (error) {
    console.error(`Error occupying slot for event ${eventId}:`, error);
    throw error;
  }
};
