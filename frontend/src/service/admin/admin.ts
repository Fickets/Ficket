import { AxiosResponse } from "axios";
import { adminPrivateApi, publicApi } from "../../utils/http-common";
import { ResponseData } from "../../types/ApiResponseType";
import { AdminLoginReq } from "../../types/admins/AdminLoginReq";
const url = "admins";

// 관리자 토큰 갱신
export const adminTokenRefresh = async (
  Response: (Response: AxiosResponse<ResponseData>) => void,
  Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
  await publicApi.get(`/${url}/reissue`).then(Response).catch(Error);
};

// 관리자 로그인
export const adminLogin = async (
  params: AdminLoginReq,
  Response: (Response: AxiosResponse<ResponseData>) => void,
  Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
  await publicApi.post(`/${url}/login`, params).then(Response).catch(Error);
};

// 관리자 로그아웃
export const adminLogout = async (
  Response: (Response: AxiosResponse<ResponseData>) => void,
  Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
  await adminPrivateApi.get(`/${url}/logout`).then(Response).catch(Error);
};
