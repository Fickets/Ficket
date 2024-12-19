import { AxiosResponse } from 'axios';
import { Customer } from "../../../types/admins/Customers";
import { adminPrivateApi } from "../../../utils/http-common";
import { ResponseData } from '../../../types/ApiResponseType';
import { ApiResponse } from '../../../types/eventList';
import { customerTicket } from "../../../types/admins/customer/CustomerTicket"

const url = "users"

export const fetchCustomerListByCond = async (params = {}) => {
    try {
        const response: AxiosResponse<ApiResponse> = await adminPrivateApi.get(
            `${url}/customers`,
            { params }
        );
        console.info(response.data.content);
        return response.data; // API 응답 데이터 반환
    } catch (error) {
        console.error('Error fetching customers list:', error);
        throw error; // 에러를 상위로 전달
    }
};


// 고객 목록 조회 API
export const fetchCustomers = async (): Promise<String[]> => {
    try {
        const response: AxiosResponse<String[]> =
            await adminPrivateApi.get(`/${url}`);
        return response.data;
    } catch (error: any) {
        console.error(
            'Error fetching customers:',
            error?.response?.status,
            error?.message
        );
        throw new Error(error?.response?.statusText || 'Failed to fetch customers');
    }
};

export const customerTicketList = async (
    userId: string,
    Response: (Response: AxiosResponse<customerTicket>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
    await adminPrivateApi.get(`/${url}/customers/ticket/${userId}`)
        .then(Response)
        .catch(Error);
};

export const customerDelete = async (
    userId: string,
    Response: (Response: AxiosResponse<ResponseData>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
    await adminPrivateApi.delete(`/${url}/customers/delete/${userId}`)
        .then(Response)
        .catch(Error);
}