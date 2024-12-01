import { AxiosResponse, AxiosProgressEvent } from 'axios';
import { privateApi } from '../../utils/http-common';
import { EventDetailData } from '../../types/edit';

// Fetch Event Detail
export const fetchEventDetail = async (
  eventId: number
): Promise<EventDetailData> => {
  const response: AxiosResponse<EventDetailData> = await privateApi.get(
    `/events/admin/${eventId}/detail`
  );
  return response.data;
};

export const updateEvent = async (
  eventId: number,
  formData: FormData
): Promise<string> => {
  const response: AxiosResponse<string> = await privateApi.patch(
    `/events/admins/event/${eventId}`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  );
  return response.data;
};

export const uploadImage = async (
  formData: FormData,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void
): Promise<string> => {
  const response: AxiosResponse<string> = await privateApi.post(
    '/events/admin/content/image',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    }
  );
  return response.data;
};
