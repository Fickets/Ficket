import { AxiosResponse } from "axios";
import {
  EventSeatSummary,
  SeatCntGrade,
  SeatStatusResponse,
} from "../../types/selectseat";
import { privateApi } from "../../utils/http-common";

// 등급별 남은 좌석 수 조회
export const fetchSeatCntGrade = async (
  eventScheduleId: number,
): Promise<SeatCntGrade[]> => {
  const response: AxiosResponse<SeatCntGrade[]> = await privateApi.get(
    `/events/grades/seats/${eventScheduleId}`,
  );
  return response.data;
};

// 행사 요약 정보 조회
export const fetchEventSeatSummary = async (
  eventScheduleId: number,
): Promise<EventSeatSummary> => {
  const response: AxiosResponse<EventSeatSummary> = await privateApi.get(
    `/events/event-simple/${eventScheduleId}`,
  );
  return response.data;
};

// 전체 좌석 조회 (좌석 상태 포함)
export const fetchAllSeatStatus = async (
  eventScheduleId: number,
): Promise<SeatStatusResponse[]> => {
  const response: AxiosResponse<SeatStatusResponse[]> = await privateApi.get(
    `/events/${eventScheduleId}/seats`,
  );
  return response.data;
};

// 선택 좌석들 선점
export const lockSeats = async (payload: {
  eventScheduleId: number;
  selectSeatInfoList: {
    seatMappingId: number;
    seatPrice: number;
    seatGrade: string;
  }[];
}): Promise<void> => {
  try {
    await privateApi.post(`/events/seat/lock`, payload, {
      headers: {
        "Content-Type": "application/json",
      },
    });
  } catch (error) {
    throw error;
  }
};

// 좌석 선점 해제
export const unLockSeats = async (payload: {
  eventScheduleId: number;
  seatMappingIds: number[];
}): Promise<void> => {
  await privateApi.post<void>(`/events/seat/unlock`, payload, {
    headers: {
      "Content-Type": "application/json",
    },
  });
};
