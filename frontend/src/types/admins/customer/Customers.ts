export interface CustomerListProps {
    data: ApiResponse;
    onPageChange: (page: number) => void;
}

export interface CustomerSearchParams {
    userId?: number | null; // 필터링용 이벤트 ID (선택적)
    userName?: string | null; // 공연 제목 (선택적)
    startDate?: string | null; // 검색 시작 날짜 (ISO 형식 문자열, 선택적)
    endDate?: string | null; // 검색 종료 날짜 (ISO 형식 문자열, 선택적)
    page?: number;
    size?: number;
    sort?: string;
}

export interface CustomerSearchBarProps {
    onSearch: (params: Partial<CustomerSearchParams>) => void; // 검색 조건 전달
}

export interface Customer {
    userId: number;
    userName: string;
}

export interface customerTicket {
    orderId: number,
    seatLoc: string[],
    ticketTotalPrice: number,
    eventTitle: string,
    stageName: string,
    createdAt: string,
}


export interface ApiResponse {
    content: customerTicket[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}