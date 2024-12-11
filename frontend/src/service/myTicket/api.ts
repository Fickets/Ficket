import { privateApi } from "../../utils/http-common.ts";
import { AxiosResponse } from "axios";
import { PagedResponse } from "../../types/myTicket.ts";

export const deleteUser = async (): Promise<void> => {
  await privateApi.delete("/users");
};

export const updateUser = async (requestData: {
  userId: number;
  userName: string;
  birth: number;
  gender: string;
}): Promise<number> => {
  const response: AxiosResponse = await privateApi.put("/users", requestData);

  return response.status;
};

export const fetchMyTickets = async (
  page: number,
  sort: string,
  sidoFilter: string | null,
): Promise<PagedResponse> => {
  const response: AxiosResponse<PagedResponse> = await privateApi.get(
    `/users/my-ticket`,
    {
      params: {
        page,
        size: 6,
        sort,
        ...(sidoFilter && { sidoFilter }), // sidoFilter가 null이 아닌 경우에만 추가
      },
    },
  );
  return response.data;
};

export const refundMyTicket = async (orderId: number): Promise<number> => {
  const response: AxiosResponse = await privateApi.delete(
    `/ticketing/order/${orderId}`,
  );

  return response.status;
};
