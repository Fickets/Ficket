import axios, { AxiosInstance } from "axios";
import { httpStatusCode } from "./http-status";
import { userTokenRefresh } from "../service/user/user";

// 쿠키를 이용하려면 활성화 필수
axios.defaults.withCredentials = true;

// gateway서버 URL
const baseURL: string = "http://localhost:9000/api/v1";

// 새 토큰 저장
const newAccess = (header: string) => {
    // USER_STORE 라는 이름의 문자 가져와서 accessToken 갱신하고 다시 저장
    const stored = localStorage.getItem('USER_STORE');
    if (stored) {
        const obj = JSON.parse(stored)
        obj.state.accessToken = header;
        localStorage.setItem("USER_STORE", JSON.stringify(obj));
    }
}

// 토큰 X
export const publicApi: AxiosInstance = axios.create({
    baseURL: baseURL,
    headers: {
        "Content-Type": "application/json",
    }
})
// 토큰 O
export const privateApi: AxiosInstance = axios.create({
    baseURL: baseURL,
    headers: {
        'Content-Type': 'application/json',
    },
});
privateApi.interceptors.request.use(
    (config) => {
        const stored = localStorage.getItem("USER_STORE");
        if (stored) {
            const obj = JSON.parse(stored)
            if (obj.state.accessToken !== '') {
                config.headers["Authorization"] = obj.state.accessToken;
            }
        }
        return config;
    },
    (error) => {
        return error;
    }
)
privateApi.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        const { config, response: { status, data }, } = error;
        if (status === 401 && data === "user access token expired") {
            const originRequest = config;
            userTokenRefresh(
                (res) => {
                    if (res.status === httpStatusCode.OK && res.headers.Authorization) {
                        newAccess(res.headers.Authorization);
                        axios.defaults.headers.Authorization = `${res.headers.Authorization}`;
                        originRequest.headers.Authorization = `${res.headers.Authorization}`;
                        // 토큰 교환 재 시도
                        return axios(originRequest);
                    }
                },
                () => {
                }
            )
        }
    }
);