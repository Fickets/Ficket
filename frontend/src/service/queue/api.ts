import { privateApi } from "../../utils/http-common.ts";
import { MyQueueStatusResponse } from "../../types/queue.ts";

// 대기열 진입 API
export const enterQueue = async (eventId: string): Promise<void> => {
  try {
    await privateApi.get<void>(`/queues/${eventId}/enter`);
  } catch (error) {
    console.error("대기열 진입 실패:", error);
    throw error;
  }
};

// 나의 대기열 상태 조회 API
export const getQueueStatus = async (
  eventId: string,
): Promise<MyQueueStatusResponse> => {
  try {
    const response = await privateApi.get<MyQueueStatusResponse>(
      `/queues/${eventId}/my-status`,
    );

    console.log(response);
    return response.data;
  } catch (error) {
    console.error("대기열 상태 조회 실패:", error);
    throw error;
  }
};
