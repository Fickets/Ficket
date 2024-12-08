export interface SeatCntGrade {
  partitionName: string;
  count: number;
}

export interface SeatGradeInfo {
  grade: string;
  price: number;
}

export interface EventSeatSummary {
  posterMobileUrl: string;
  reservationLimit: number;
  eventStageImg: string;
  seatGradeInfoList: SeatGradeInfo[];
}

export interface SeatStatusResponse {
  seatMappingId: number;
  seatX: number;
  seatY: number;
  seatGrade: string;
  seatRow: string;
  seatCol: string;
  status: string;
  seatPrice: number;
}

export interface RightPanelProps {
  gradeColors: { [key: string]: string };
  eventTitle: string;
  eventStage: string;
  eventDate: string;
  eventTime: string;
  posterMobileUrl: string;
  seatGradeInfoList: {
    grade: string;
    price: number;
  }[];
  seatCntGrade: {
    partitionName: string;
    count: number;
  }[];
  selectedSeats: {
    seatMappingId: number;
    grade: string;
    row: string;
    col: string;
    price: number;
  }[];
}

export interface SeatMapProps {
  eventStageImg: string;
  reservationLimit: number;
  seatStatusResponse: SeatStatusResponse[];
  onSeatSelect: (
    selectedSeats: {
      seatMappingId: number;
      grade: string;
      row: string;
      col: string;
      price: number;
    }[],
  ) => void;
  selectedSeats: {
    seatMappingId: number;
    grade: string;
    row: string;
    col: string;
    price: number;
  }[];
  gradeColors: { [key: string]: string };
}
