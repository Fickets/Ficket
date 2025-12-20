import { AxiosResponse } from "axios";
import { ViewRankResponse, SimpleEvent } from "../../types/home";
import { publicApi } from "../../utils/http-common.ts";
import { Genre } from "../../types/ReservationRateRanking.ts";

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

export const openRecent = async (genre: string): Promise<SimpleEvent[]> => {
  const response: AxiosResponse<SimpleEvent[]> = await publicApi.get(
    `/events/open-recent`,
    {
      params: { genre },
    },
  );
  return response.data;
};

export const getGenreRankTopTen = async (
  genre: Genre,
): Promise<SimpleEvent[]> => {
  try {
    const genreKey = (Object.keys(Genre) as (keyof typeof Genre)[]).find(
      (key) => Genre[key] === genre,
    );

    const params = {
      genre: genreKey || genre,
    };

    const response = await publicApi.get("/events/genre-rank", { params });
    return response.data;
  } catch (error) {
    console.error("Error fetching ranking:", error);
    throw error;
  }
};
