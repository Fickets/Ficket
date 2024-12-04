import { adminPrivateApi } from "../../utils/http-common";
import { Admin, ApiResponse } from "../../types/eventList.ts";
import { AxiosResponse } from "axios";

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
