import { publicApi } from "../../utils/http-common.ts";
import { AxiosResponse } from "axios";
import {
  Genre,
  Period,
  ReservationRateRankingResponse,
} from "../../types/ReservationRateRanking.ts";

export const fetchReservationRateRanking = async ({
  genre,
  period,
}: {
  genre?: Genre | null;
  period?: Period | null;
}): Promise<ReservationRateRankingResponse[]> => {
  try {
    const params: { genre?: Genre; period?: Period } = {};
    if (genre) params.genre = genre;
    if (period) params.period = period;

    const response: AxiosResponse<ReservationRateRankingResponse[]> =
      await publicApi.get("/events/detail/reservation-rate-rank", { params });

    return response.data;
  } catch (error) {
    console.error("Error fetching reservation rate ranking:", error);
    throw error; // 에러를 상위로 전달
  }
};
