import { AxiosResponse } from "axios";
import { ViewRankResponse, SimpleEvent } from "../../types/home";
import { publicApi } from "../../utils/http-common.ts";


export const fetchViewRanking = async (
  limit: number,
): Promise<ViewRankResponse[]> => {
  const response: AxiosResponse<ViewRankResponse[]> = await publicApi.get(
    `/events/detail/view-rank`,
    {
      params: { limit },
    },
  );

  return response.data;
};


export const openRecent = async (
  genre: string
): Promise<SimpleEvent[]> => {
  const response: AxiosResponse<SimpleEvent[]> = await publicApi.get(
    `/events/open-recent`,
    {
      params: { genre },
    },);
  return response.data;
};


export const genreRank = async (
  genre: string
): Promise<SimpleEvent[]> => {
  const response: AxiosResponse<SimpleEvent[]> = await publicApi.get(
    `/events/genre-rank`,
    {
      params: { genre }
    },);
  return response.data
}
