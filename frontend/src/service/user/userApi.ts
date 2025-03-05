import { AxiosResponse } from "axios";
import { privateApi, publicApi } from "../../utils/http-common";
import { ResponseData } from "../../types/ApiResponseType";
import { UserAdditionalInfoReq } from "../../types/users/UserAdditionalInfoType";
// import { UserAdditionalInfoRes } from "../../types/ApiResponseType";
import { UserAdditionalInfoType } from "../../types/users/UserAdditionalInfoType";
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
export const userAdditionalInfo = async (
    params: UserAdditionalInfoReq,
    Response: (Response: AxiosResponse<UserAdditionalInfoType>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void) => {
    await privateApi.post(`/${url}/additional-info`, params)
        .then(Response)
        .catch(Error)
}

// 내 정보 조회
export const getMyInfo = async (
    Response: (Response: AxiosResponse<ResponseData>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void) => {
    await privateApi.get(`/${url}/my-info`)
        .then(Response)
        .catch(Error)
}

export const userLogout = async (
    Response: (Response: AxiosResponse<ResponseData>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void) => {
    await privateApi.get(`/${url}/logout`)
        .then(Response)
        .catch(Error)
}
