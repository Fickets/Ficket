export type BlobInfo = {
  blob: () => Blob;
  filename: () => string;
  base64: () => string;
  blobUri: () => string;
};

export interface EventDetailData {
  companyId: number;
  companyName: string;
  stageId: number;
  stageName: string;
  stageImg: string;
  genre: string[];
  age: string;
  content: string;
  title: string;
  subTitle: string;
  runningTime: number;
  ticketingTime: string;
  reservationLimit: number;
  eventSchedules: EventDate[];
  stageSeats: SeatGrade[];
  poster: string;
  banner: string;
}

export interface EventFormProps {
  onChange: (formData: Partial<EventDetailData>) => void;
  initialData: EventDetailData;
}

export type ImageUploaderProps = {
  title: string;
  aspectRatio: number; // 이미지 비율을 설정하기 위한 prop (예: 2960 / 3520)
  onChange: (file: File | null) => void;
  initialImage: string;
};

export interface TinyEditorProps {
  onChange: (content: string) => void;
  initialContent: string; // 초기 콘텐츠
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
  stageId: number; // 공연장 ID
  stageImg: string; // 공연장 좌석 맵 이미지 URL
  initialData: SeatGrade[];
  onChange: (data: { seats: SeatGrade[] }) => void;
}

export interface EventDate {
  date: string;
  sessions: {
    round: number;
    time: string;
  }[];
}

export interface CalendarWithScheduleProps {
  initialData: EventDate[];
  onChange: (data: { eventDate: EventDate[] }) => void; // null 허용
}
