import { adminPrivateApi } from "../../utils/http-common.ts";
import { AxiosResponse } from "axios";

export const generateTemporaryUrl = async (
  eventId: string,
): Promise<string> => {
  const params = new URLSearchParams({ eventId: eventId });
  const response: AxiosResponse<{ temporaryUrl: string }> =
    await adminPrivateApi.post(`/admins/generate-url?${params.toString()}`);

  return response.data.temporaryUrl;
};

/**
 * 이벤트 ID를 기반으로 임시 URL을 확인하는 API
 *
 * @param eventId 확인할 이벤트 ID
 * @returns 존재하면 임시 URL을 반환, 없으면 null 반환
 */
export const checkTemporaryUrl = async (
  eventId: string,
): Promise<string | null> => {
  const response: AxiosResponse<{ temporaryUrl: string | null }> =
    await adminPrivateApi.get(`/admins/${eventId}/temporary-url/exists`);
  return response.data.temporaryUrl;
};
