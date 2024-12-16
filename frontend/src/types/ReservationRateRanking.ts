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
