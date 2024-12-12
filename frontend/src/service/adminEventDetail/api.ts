import { adminPrivateApi } from '../../utils/http-common';
import { AxiosResponse } from 'axios';
import {
  DailyRevenueResponse,
  DayCountMap,
} from '../../types/adminEventDetail';

// 날자별 수익 조회
export const fetchDailyRevenue = async (
  eventId: string
): Promise<DailyRevenueResponse[]> => {
  try {
    const response: AxiosResponse<DailyRevenueResponse[]> =
      await adminPrivateApi.get(`/admins/${eventId}/daily-revenue`);
    return response.data;
  } catch (error: any) {
    throw error;
  }
};

// 요일별 예매 수 조회
export const fetchDayCount = async (eventId: string): Promise<DayCountMap> => {
  try {
    const response: AxiosResponse<DayCountMap> = await adminPrivateApi.get(
      `/admins/${eventId}/day-count`
    );
    return response.data;
  } catch (error: any) {
    throw error;
  }
};
