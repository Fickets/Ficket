export interface ReservationRateRankingResponse {
  eventId: number;
  eventTitle: string;
  eventMobilePosterUrl: string;
  eventPcPosterUrl: string;
  eventStageName: string;
  eventDates: string[];
  isClosed: boolean;
  reservationRate: number;
}

export interface RankingItem extends ReservationRateRankingResponse {
  rank: number; // 동적으로 추가되는 rank
}

export enum Genre {
  뮤지컬 = "뮤지컬",
  콘서트 = "콘서트",
  스포츠 = "스포츠",
  전시_행사 = "전시/행사",
  클래식_무용 = "클래식/무용",
  아동_가족 = "아동/가족",
}

export enum Period {
  DAILY = "DAILY", // 일간 랭킹
  WEEKLY = "WEEKLY", // 주간 랭킹
  MONTHLY = "MONTHLY", // 월간 랭킹
  PREVIOUS_DAILY = "PREVIOUS_DAILY", // 전일 랭킹
  PREVIOUS_WEEKLY = "PREVIOUS_WEEKLY", // 전주 랭킹
}
