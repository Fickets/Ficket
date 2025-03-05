// 좌석 정보 인터페이스
export interface MySeatInfo {
  seatGrade: string; // 좌석 등급 (예: "VIP", "R", "S", "A")
  seatRow: string; // 좌석 행 번호
  seatCol: string; // 좌석 열 번호
}

// 이벤트 정보 인터페이스
export interface EventContent {
  orderId: number;
  createdAt: string; // 생성일 (ISO 8601 형식)
  eventDateTime: string; // 이벤트 날짜 및 시간
  eventStageName: string; // 이벤트 스테이지 이름
  sido: string; // 시/도
  eventPcBannerUrl: string; // PC 배너 URL
  eventMobileBannerUrl: string; // 모바일 배너 URL
  eventName: string; // 이벤트 이름
  companyName: string; // 회사 이름
  mySeatInfoList: MySeatInfo[]; // 좌석 정보 리스트
}

// 페이징 정보 포함 인터페이스
export interface PagedResponse {
  content: EventContent[]; // 이벤트 콘텐츠 리스트
  page: number; // 현재 페이지 번호
  size: number; // 페이지 크기
  totalElements: number; // 전체 요소 개수
  totalPages: number; // 전체 페이지 수
}
