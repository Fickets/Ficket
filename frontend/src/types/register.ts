export interface EventDate {
  date: string;
  sessions: {
    round: number;
    time: string;
  }[];
}

export interface EventData {
  companyId: number;
  stageId: number;
  genre: string[];
  age: string;
  content: string;
  title: string;
  subTitle: string;
  runningTime: number;
  ticketingTime: string;
  reservationLimit: number;
  eventDate: EventDate[];
  seats: SeatGrade[];
}

export interface GenreOption {
  label: string;
  value: string;
}

export interface EventFormProps {
  onChange: (data: Partial<EventData>) => void;
}

export interface CalendarWithScheduleProps {
  onChange: (data: { eventDate: EventDate[] }) => void;
}

export interface SeatGrade {
  grade: string;
  price: number;
  seats: number[];
}

export interface SeatInfo {
  seatId: number;
  x: number;
  y: number;
  seatCol: string;
  seatRow: string;
}

export interface SeatSettingProps {
  stageId: number | null; // 현재 선택된 행사장의 ID (없을 경우 null)
  onChange: (data: { seats: SeatGrade[] }) => void; // 변경된 좌석 데이터를 상위 컴포넌트로 전달
}

export type BlobInfo = {
  blob: () => Blob;
  filename: () => string;
  base64: () => string;
  blobUri: () => string;
};

export type TinyEditorProps = {
  onChange: (content: string) => void;
};

// 각 회사의 데이터 구조
export interface Company {
  companyId: number;
  companyName: string;
}

// API 응답의 구조
export interface FetchCompaniesResponse {
  companyList: Company[];
}

export interface Stage {
  stageId: number;
  stageName: string;
  eventStageImg: string;
}

// API 응답의 구조
export interface FetchStagesResponse {
  eventStageDtoList: Stage[];
}

export interface FetchStageSeatsResponse {
  stageSeats: SeatInfo[];
}
