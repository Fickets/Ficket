import { publicApi } from '../../utils/http-common.ts';
import { AxiosResponse } from 'axios';
import {
  EventScheduledOpenResponse,
  PageDTO,
  SearchParams,
} from '../../types/eventScheduledOpen.ts';

export const searchEventScheduledOpen = async (
  searchCond: SearchParams
): Promise<PageDTO<EventScheduledOpenResponse>> => {
  const params: Record<string, string | number> = {};

  if (searchCond.searchValue) {
    params.searchValue = searchCond.searchValue;
  }

  if (searchCond.genre) {
    params.genre = searchCond.genre;
  }

  if (searchCond.page !== undefined) {
    params.page = searchCond.page;
  }

  if (searchCond.size !== undefined) {
    params.size = searchCond.size;
  }

  if (searchCond.sort) {
    params.sort = searchCond.sort;
  }

  try {
    const response: AxiosResponse<PageDTO<EventScheduledOpenResponse>> =
      await publicApi.get('/events/detail/scheduled-open-event', { params });
    console.log(response.data);
    return response.data;
  } catch (error) {
    console.error('Error fetching scheduled open events:', error);
    throw error;
  }
};
