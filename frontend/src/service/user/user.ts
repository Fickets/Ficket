import { AxiosResponse } from "axios";
import { privateApi, publicApi } from "../../utils/http-common";
import { ResponseData } from "../../types/ApiResponseType";
import { UserAdditionalInfoType, UserAdditionalInfoReq } from "../../types/users/UserAdditionalInfoType";
import { UserAdditionalInfoRes } from "../../types/ApiResponseType";
const url = "users"

// 유저토큰 갱신
export const userTokenRefresh = async (
    Response: (Response: AxiosResponse<ResponseData>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void) => {
    await publicApi.get(`/${url}/reissue`)
        .then(Response)
        .catch(Error)
}

// 유저 정보 넣기
export const UserAdditionalInfo = async (
    params: UserAdditionalInfoReq,
    Response: (Response: AxiosResponse<UserAdditionalInfoRes>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void) => {
    await privateApi.post(`/${url}/additional-info`, params)
        .then(Response)
        .catch(Error)
}
