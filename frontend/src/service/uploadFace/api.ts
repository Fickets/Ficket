import { privateApi } from '../../utils/http-common.ts';
import { AxiosResponse } from 'axios';
import { FaceApiResponse } from '../../types/uploadFace.ts';

export const uploadUserFace = async (
  userImg: File, // 업로드할 이미지 파일
  eventScheduleId: number // 이벤트 일정 ID
): Promise<FaceApiResponse> => {
  try {
    // FormData 생성
    const formData = new FormData();
    formData.append('userImg', userImg);

    // API 호출
    const response: AxiosResponse<FaceApiResponse> = await privateApi.post(
      `/ticketing/order/${eventScheduleId}/upload-face`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );

    // 응답 반환
    return response.data;
  } catch (error) {
    console.error('Error uploading user face:', error);
    throw error; // 에러 전파
  }
};
