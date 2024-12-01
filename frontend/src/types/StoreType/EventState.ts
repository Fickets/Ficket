import { create } from 'zustand';
import { StateCreator } from 'zustand';

interface EventState {
  eventId: number;
  eventScheduleId: number;
  eventTitle: string;
  eventDate: string;
  eventTime: string;
  eventStage: string;
  faceImg: File | null;
  selectedSeats: {
    seatMappingId: number;
    grade: string;
    row: string;
    col: string;
  }[];
  setEventInfo: (
    eventInfo: Partial<
      Omit<
        EventState,
        | 'setEventInfo'
        | 'resetEventInfo'
        | 'setFaceImg'
        | 'setSelectedSeats'
        | 'resetSelectedSeats'
      >
    >
  ) => void;
  setFaceImg: (file: File | null) => void;
  setSelectedSeats: (seats: EventState['selectedSeats']) => void;
  resetSelectedSeats: () => void;
  resetEventInfo: () => void;
}

// Zustand 상태 생성기 타입 선언
const createEventState: StateCreator<EventState> = (set) => ({
  eventId: 1,
  eventScheduleId: 5,
  eventTitle: '',
  eventDate: '',
  eventTime: '',
  eventStage: '',
  faceImg: null,
  selectedSeats: [], // 초기값은 빈 배열

  // 상태 업데이트 함수
  setEventInfo: (
    eventInfo: Partial<
      Omit<
        EventState,
        | 'setEventInfo'
        | 'resetEventInfo'
        | 'setFaceImg'
        | 'setSelectedSeats'
        | 'resetSelectedSeats'
      >
    >
  ) => {
    set((state) => ({
      ...state, // 기존 상태 유지
      ...eventInfo, // 전달된 정보로 업데이트
    }));
  },

  // faceImg만 업데이트하는 함수
  setFaceImg: (file: File | null) => {
    set(() => ({
      faceImg: file,
    }));
  },

  // selectedSeats 업데이트 함수
  setSelectedSeats: (seats) => {
    set(() => ({
      selectedSeats: seats,
    }));
  },

  // selectedSeats 초기화 함수
  resetSelectedSeats: () => {
    set(() => ({
      selectedSeats: [], // 빈 배열로 초기화
    }));
  },

  // 상태 초기화 함수
  resetEventInfo: () => {
    set(() => ({
      eventId: 1,
      eventScheduleId: 5,
      eventTitle: '',
      eventDate: '',
      eventTime: '',
      eventStage: '',
      faceImg: null,
      selectedSeats: [], // 빈 배열로 초기화
    }));
  },
});

// Zustand 스토어 생성
export const useEventStore = create<EventState>(createEventState);
