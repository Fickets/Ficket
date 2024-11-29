import { AxiosResponse } from "axios";
import { publicApi } from "../utils/http-common";
import { ResponseData } from "../types/ApiResponseType";

const url = "users"

// 유저토큰 갱신
export const userTokenRefresh = async (
    Response: (Response: AxiosResponse<ResponseData>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void) => {
    await publicApi.post(`/${url}/reissue`)
        .then(Response)
        .catch(Error)
}