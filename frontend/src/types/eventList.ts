// 데이터 타입 정의
export interface Event {
  eventId: number;
  eventTitle: string;
  stageName: string;
  companyName: string;
  adminName: string;
  eventDates: string[];
}

export interface EventListProps {
  data: ApiResponse;
  onPageChange: (page: number) => void;
}

export interface SearchParams {
  eventId?: number | null; // 필터링용 이벤트 ID (선택적)
  eventTitle?: string | null; // 공연 제목 (선택적)
  companyId?: number | null; // 회사 ID (선택적)
  adminId?: number | null; // 관리자 ID (선택적)
  eventStageId?: number | null; // 공연장 ID (선택적)
  startDate?: string | null; // 검색 시작 날짜 (ISO 형식 문자열, 선택적)
  endDate?: string | null; // 검색 종료 날짜 (ISO 형식 문자열, 선택적)
  page?: number;
  size?: number;
  sort?: string;
}

export interface EventSearchBarProps {
  onSearch: (params: Partial<SearchParams>) => void; // 검색 조건 전달
}

export interface Admin {
  adminId: number;
  adminName: string;
}

export interface ApiResponse {
  content: Event[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
