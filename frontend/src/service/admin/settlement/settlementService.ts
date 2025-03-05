import { AxiosResponse } from 'axios';
import { adminPrivateApi } from "../../../utils/http-common";
import { ResponseData } from '../../../types/ApiResponseType';
import { ApiResponse, Settlement } from '../../../types/admins/Settlement/Settlement';


// 이벤트 목록 조회 API
export const fetchEvents = async (): Promise<string[]> => {
    try {
        const response: AxiosResponse<string[]> =
            await adminPrivateApi.get(`/events/search-title`);
        return response.data;
    } catch (error: any) {
        console.error(
            'Error fetching events:',
            error?.response?.status,
            error?.message
        );
        throw new Error(error?.response?.statusText || 'Failed to fetch events');
    }
};

// 정산 리스트 조회 API
export const fetchSettlementListByCond = async (params = {}) => {
    try {
        const response: AxiosResponse<ApiResponse> = await adminPrivateApi.get(
            `/settlements`,
            { params }
        );
        console.info(response.data.content);
        return response.data; // API 응답 데이터 반환
    } catch (error) {
        console.error('Error fetching customers list:', error);
        throw error; // 에러를 상위로 전달
    }
};

export const settlementsList = async (
    eventId: string,
    Response: (Response: AxiosResponse<Settlement[]>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
    await adminPrivateApi.get(`/settlements/list/${eventId}`)
        .then(Response)
        .catch(Error);
};

export const clearSettlement = async (
    eventId: string,
    Response: (Response: AxiosResponse<ResponseData>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
    await adminPrivateApi.get(`/settlements/clear/${eventId}`)
        .then(Response)
        .catch(Error);
}