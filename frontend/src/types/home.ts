export interface ViewRankResponse {
  eventId: number;
  eventTitle: string;
  eventSubTitle: string;
  eventStageName: string;
  eventOriginBannerUrl: string;
  eventPcPosterUrl: string;
  eventDateList: string[];
}


export interface SimpleEvent {
  eventId: number;
  title: string;
  date: string;
  pcImg: string;
  mobileImg: string
  eventStage: string
}

export interface SimplePageRes {
  content: SimpleEvent[];
  page: number;
  size: number;
  last: boolean
}