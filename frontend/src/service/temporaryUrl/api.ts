import { adminPrivateApi } from '../../utils/http-common.ts';
import { AxiosResponse } from 'axios';

export const generateTemporaryUrl = async (
  eventId: string
): Promise<string> => {
  const params = new URLSearchParams({ eventId: eventId });
  const response: AxiosResponse<{ temporaryUrl: string }> =
    await adminPrivateApi.post(`/admins/generate-url?${params.toString()}`);

  return response.data.temporaryUrl;
};
