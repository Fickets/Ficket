import {
  Company,
  FetchCompaniesResponse,
  Stage,
  FetchStagesResponse,
  FetchStageSeatsResponse,
} from './../../types/register';
import { SeatInfo } from '../../types/register';
import { privateApi } from '../../utils/http-common';
import { AxiosResponse, AxiosProgressEvent } from 'axios';

// Fetch companies
export const fetchCompanies = async (): Promise<Company[]> => {
  try {
    const response: AxiosResponse<FetchCompaniesResponse> =
      await privateApi.get('/admins/companies');
    return response.data.companyList;
  } catch (error: any) {
    console.error(
      'Error fetching companies:',
      error?.response?.status,
      error?.message
    );
    throw new Error(error?.response?.statusText || 'Failed to fetch companies');
  }
};

export const fetchStages = async (): Promise<Stage[]> => {
  try {
    const response: AxiosResponse<FetchStagesResponse> = await privateApi.get(
      '/events/admin/stages'
    );
    return response.data.eventStageDtoList;
  } catch (error: any) {
    console.error(
      'Error fetching stages:',
      error?.response?.status,
      error?.message
    );
    throw new Error(error?.response?.statusText || 'Failed to fetch stages');
  }
};

// Fetch Stage Seats
export const fetchStageSeats = async (stageId: number): Promise<SeatInfo[]> => {
  const response: AxiosResponse<FetchStageSeatsResponse> = await privateApi.get(
    `events/admin/stage/${stageId}`
  );
  return response.data.stageSeats;
};

// Register event
export const registerEvent = async (formData: FormData): Promise<string> => {
  const response: AxiosResponse<string> = await privateApi.post(
    'events/admins/event',
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
