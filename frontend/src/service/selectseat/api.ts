import axios from 'axios';
import {
  EventSeatSummary,
  SeatCntGrade,
  SeatStatusResponse,
} from '../../types/selectseat';

// 등급별 남은 좌석 수 조회
export const fetchSeatCntGrade = async (
  eventScheduleId: number
): Promise<SeatCntGrade[]> => {
  const response = await axios.get<SeatCntGrade[]>(
    `http://localhost:9000/api/v1/events/grades/seats/${eventScheduleId}`
  );
  return response.data;
};

// 행사 요약 정보 조회
export const fetchEventSeatSummary = async (
  eventScheduleId: number
): Promise<EventSeatSummary> => {
  const response = await axios.get<EventSeatSummary>(
    `http://localhost:9000/api/v1/events/event-simple/${eventScheduleId}`
  );
  return response.data;
};

// 전체 좌석 조회 (좌석 상태 포함)
export const fetchAllSeatStatus = async (
  eventScheduleId: number
): Promise<SeatStatusResponse[]> => {
  const response = await axios.get<SeatStatusResponse[]>(
    `http://localhost:9000/api/v1/events/${eventScheduleId}/seats`
  );
  return response.data;
};

// 선택 좌석들 선점
export const lockSeats = async (
  userId: number,
  payload: {
    eventScheduleId: number;
    reservationLimit: number;
    seatMappingIds: number[];
  }
): Promise<void> => {
  await axios.post<void>(
    `http://localhost:9000/api/v1/events/seat`, // 요청 URL
    payload, // JSON 바디
    {
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId, // 헤더에 사용자 ID 추가
      },
    }
  );
};

// 좌석 선점 해제
export const unLockSeats = async (
  userId: number,
  payload: {
    eventScheduleId: number;
    seatMappingIds: number[];
  }
): Promise<void> => {
  await axios.post<void>(
    `http://localhost:9000/api/v1/events/seat/unlock`, // 요청 URL
    payload, // JSON 바디
    {
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId, // 헤더에 사용자 ID 추가
      },
    }
  );
};
