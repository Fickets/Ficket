export interface autoCompleteRes {
  EventId: string;
  Title: string;
}

// Enums
export enum SaleType {
  ON_SALE = "ON_SALE",
  TO_BE_SALE = "TO_BE_SALE",
  END_OF_SALE = "END_OF_SALE",
}

export enum SortBy {
  SORT_BY_ACCURACY = "SORT_BY_ACCURACY",
  SORT_BY_PERFORMANCE_IMMINENT = "SORT_BY_PERFORMANCE_IMMINENT",
}

export enum GenreType {
  뮤지컬 = "뮤지컬",
  콘서트 = "콘서트",
  스포츠 = "스포츠",
  전시_행사 = "전시_행사",
  클래식_무용 = "클래식_무용",
  아동_가족 = "아동_가족",
}

export enum Location {
  서울 = "서울특별시",
  충북 = "충청북도",
  충남 = "충청남도",
  대구 = "대구광역시",
  부산 = "부산광역시",
  대전 = "대전광역시",
  경남 = "경상남도",
  경북 = "경상북도",
  전라 = "전라남도",
  인천 = "인천광역시",
  광주 = "광주광역시",
  울산 = "울산광역시",
  강원 = "강원도",
  제주 = "제주특별자치도",
  세종 = "세종특별자치시",
}

// export interface SearchParams {
//   genreList?: GenreType[]; // 장르 필터
//   locationList?: Location[]; // 위치 필터
//   title: string; // 검색 제목
//   startDate?: string; // 시작 날짜 (YYYY-MM-DD)
//   endDate?: string; // 종료 날짜 (YYYY-MM-DD)
//   saleTypeList?: SaleType[]; // 판매 타입 필터
//   sortBy?: SortBy; // 정렬 기준
//   pageNumber?: number; // 페이지 번호
//   pageSize?: number; // 페이지 크기
// }

export interface SearchParams {
  title: string; // 검색 제목
  genreList?: GenreType[]; // 장르 필터
  locationList?: Location[]; // 위치 필터
  startDate?: string; // 시작 날짜 (YYYY-MM-DD)
  endDate?: string; // 종료 날짜 (YYYY-MM-DD)
  saleTypeList?: SaleType[]; // 판매 타입 필터
  sortBy?: SortBy; // 정렬 기준
  searchAfter?: SearchAfter;
  pageSize?: number; // 페이지 크기
}

export type Genre = {
  Genre: string;
};

export type Schedule = {
  Schedule: string;
};

export type Event = {
  EventId: string;
  Title: string;
  Poster_Url: string;
  Stage: string;
  Location: string;
  Genres: Genre[];
  Schedules: Schedule[];
  Ticketing: string;
  SaleType: SaleType;
};

export interface SearchResult {
  totalSize: number;
  totalPages: number;
  results: Event[];
}

export type SearchAfter = Array<string | number>;

export interface SearchAfterResult {
  totalSize: number;
  events: Event[];
  nextSearchAfter?: SearchAfter;
}
