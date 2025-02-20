import { AxiosResponse } from "axios";
import { privateApi, publicApi } from "../../utils/http-common";
import { ResponseData } from "../../types/ApiResponseType";
import { eventDetailType } from '../../types/StoreType/EventDetailStore'
const url = "events";

export const eventDetail = async (
  eventId: number,
  Response: (Response: AxiosResponse<eventDetailType>) => void,
  Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
  await publicApi.get(`/${url}/detail/${eventId}`).then(Response).catch(Error);
};

export const genderStatistic = async (
  eventId: number,
  Response: (Response: AxiosResponse<string>) => void,
  Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
  await publicApi
    .get(`/${url}/statistic/gender/${eventId}`)
    .then(Response)
    .catch(Error);
};

export const checkEnterTicketing = async (
  eventScheduleId: number,
): Promise<number> => {
  try {
    // API 호출
    const response: AxiosResponse<number> = await privateApi.get(
      `/ticketing/order/enter-ticketing/${eventScheduleId}`,
    );

    // 서버 응답 데이터 반환
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const checkEventTime = async (
  eventId: string,
): Promise<boolean> => {
  try {
    // API 호출
    const response: AxiosResponse<boolean> = await privateApi.get(
      `/${url}/check-time/${eventId}`
    );

    // 서버 응답 데이터 반환
    return response.data;
  } catch (error) {
    // 오류 발생 시 에러 처리
    throw error;
  }
};

export const checkScheduleTime = async (
  scheduleId: number,
): Promise<boolean> => {
  try {
    // API 호출
    const response: AxiosResponse<boolean> = await privateApi.get(
      `/${url}/check-schedule/${scheduleId}`
    );

    // 서버 응답 데이터 반환
    return response.data;
  } catch (error) {
    // 오류 발생 시 에러 처리
    throw error;
  }
};
