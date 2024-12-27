import { Genre } from "./ReservationRateRanking.ts";

export interface EventScheduledOpenResponse {
  eventId: number;
  title: string;
  genreList: Genre[];
  ticketStartTime: string;
  mobilePosterUrl: string;
  // isNewPostEvent: boolean;
  newPostEvent: boolean;
}

export interface PageDTO<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  sortProperty: string;
  sortDirection: "ASC" | "DESC" | "none";
  last: boolean;
}

export interface EventOpenListProps {
  events: PageDTO<EventScheduledOpenResponse>;
  searchParams: SearchParams;
  onSearchParamsChange: (newParams: Partial<SearchParams>) => void;
}

export interface SearchParams {
  searchValue: string | null;
  genre: Genre | null;
  size?: number;
  page?: number;
  sort?: string; // Example: "createdAt,DESC" or "field,ASC"
}
