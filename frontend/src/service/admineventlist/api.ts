import { adminPrivateApi } from "../../utils/http-common";
import { Admin, ApiResponse } from "../../types/eventList.ts";
import { AxiosResponse } from "axios";
import { Simulate } from "react-dom/test-utils";
import error = Simulate.error;

// 이벤트 목록 조회 API
export const fetchEventListByCond = async (params = {}) => {
  try {
    const response: AxiosResponse<ApiResponse> = await adminPrivateApi.get(
      "events/admin",
      { params },
    );
    console.info(response.data.content);
    return response.data; // API 응답 데이터 반환
  } catch (error) {
    console.error("Error fetching event list:", error);
    throw error; // 에러를 상위로 전달
  }
};

// 관리자 목록 조회 API
export const fetchAdmins = async (): Promise<Admin[]> => {
  try {
    const response: AxiosResponse<Admin[]> =
      await adminPrivateApi.get("/admins");
    return response.data;
  } catch (error: any) {
    console.error(
      "Error fetching companies:",
      error?.response?.status,
      error?.message,
    );
    throw new Error(error?.response?.statusText || "Failed to fetch companies");
  }
};

// 행사 Slot 설정
export const initializeSlotSize = async (
  eventId: string,
  maxSlot: number,
): Promise<string> => {
  try {
    const response: AxiosResponse<string> = await adminPrivateApi.post(
      `/admins/${eventId}/initialize-slot`,
      null, // 본문 데이터는 비워 둠
      { params: { maxSlot } }, // 쿼리 파라미터로 전달
    );

    return response.data;
  } catch (error: any) {
    throw error;
  }
};

// 행사 Slot 삭제
export const deleteSlot = async (eventId: string): Promise<void> => {
  try {
    const response: AxiosResponse<void> = await adminPrivateApi.delete(
      `/admins/${eventId}/delete-slot`,
    );

    if (response.status !== 204) {
      throw error;
    }
  } catch (error: any) {
    throw error;
  }
};

// 행사 삭제
export const deleteEvent = async (eventId: string): Promise<void> => {
  try {
    const response: AxiosResponse<void> = await adminPrivateApi.delete(
      `/events/admin/${eventId}`,
    );

    if (response.status !== 204) {
      throw error;
    }
  } catch (error: any) {
    throw error;
  }
};
