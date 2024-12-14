import { AxiosResponse } from "axios";
import { ViewRankResponse } from "../../types/home";
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
