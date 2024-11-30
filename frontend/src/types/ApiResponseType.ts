import { UserAdditionalInfoType } from "./users/UserAdditionalInfoType";

export interface ResponseData {
    data: string;
    status: number;
    statusText: string;
    headers: Record<string, string>;
    config: string;
}

export interface UserAdditionalInfoRes {
    status: number;
    data: UserAdditionalInfoType;
}