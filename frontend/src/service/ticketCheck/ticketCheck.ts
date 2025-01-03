import { AxiosResponse } from 'axios';
import { FaceApiResponse } from '../../types/uploadFace.ts';
import { adminPrivateApi, publicApi } from '../../utils/http-common.ts';


export const checkFace = async (
    userFace: File,
    eventScheduleId: number
): Promise<FaceApiResponse> => {
    try {
        const formData = new FormData();
        formData.append('userImg', userFace);

        const response: AxiosResponse<FaceApiResponse> = await publicApi.post(
            `/ticketing/check/${eventScheduleId}/user-match`,
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
        // throw error;
    }
};