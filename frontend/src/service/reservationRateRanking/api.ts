import { publicApi } from "../../utils/http-common.ts";
import { AxiosResponse } from "axios";
import { ReservationRateRankingResponse } from "../../types/ReservationRateRanking.ts";

export const fetchReservationRateRanking = async ({
  genre,
  period,
}: {
  genre?: string | null;
  period?: string | null;
}): Promise<ReservationRateRankingResponse[]> => {
  try {
    // params 객체에서 값이 있는 경우에만 추가
    const params: { genre?: string; period?: string } = {};
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
