import { AxiosResponse } from 'axios';
import { FaceApiResponse } from '../../types/uploadFace.ts';
import { adminPrivateApi, publicApi } from '../../utils/http-common.ts';


export const checkFace = async (
    userFace: File,
    eventId: string,
    connectId: number,
): Promise<FaceApiResponse> => {
    try {
        const formData = new FormData();
        formData.append('userImg', userFace);

        const response: AxiosResponse<FaceApiResponse> = await publicApi.post(
            `ticketing/check/${eventId}/${connectId}/user-match`,
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            }
        );
        return response.data;
    } catch (error) {
        console.error(`Error match user face`, error);
        throw error;
    }
};


export const checkUrl = async (
    eventId: string,
    uuid: string
): Promise<string> => {
    const params = new URLSearchParams({ uuid: uuid });
    const response: AxiosResponse<{ guestToken: string }> =
        await publicApi.get(`/admins/check-url/${eventId}?${params.toString()}`);

    return response.data.guestToken;
}


export const ticketStatusChange = async (
    ticketId: number,
    eventId: string,
    connectId: number,
): Promise<Object> => {
    const params = new URLSearchParams({ eventId: eventId, connectId: connectId.toString() });
    const response: AxiosResponse<{ res: string }> =
        await publicApi.get(`/api/v1/admins/ticket-watch/${ticketId}?${params.toString()}`);

    return response;
}