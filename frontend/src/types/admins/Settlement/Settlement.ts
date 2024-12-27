export interface SettlementListProps {
    data: ApiResponse;
    onPageChange: (page: number) => void;
}

export interface SettlementSearchParams {
    eventName?: string | null;
    settlementStatus?: string | null;
    startDate?: string | null;
    endDate?: string | null;
    page?: number;
    size?: number;
    sort?: string;
}

export interface SettlementSearchBarProps {
    onSearch: (params: Partial<SettlementSearchParams>) => void; // 검색 조건 전달
}

export interface SettlementRecord {
    companyName: string | null;
    companyId: number | null;
    eventId: number | null;
    title: string | null;
    lastModifiedAt: string | null;
    createdAt: string | null;
    totalNetSupplyAmount: number | null;
    totalServiceFee: number | null;
    totalSupplyAmount: number | null;
    totalSettlementValue: number | null;
    settlementStatus: String | null;
}

export interface ApiResponse {
    content: SettlementRecord[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}


export interface Settlement {
    settlementId: number;
    netSupplyAmount: number;
    vat: number;
    supplyValue: number;
    serviceFee: number;
    refundValue: number;
    settlementValue: number;
    settlementStatus: string;
    orderId: number
}