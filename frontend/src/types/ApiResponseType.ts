import { UserAdditionalInfoType } from "./users/UserAdditionalInfoType";
import { eventDetail } from './StoreType/EventDetailStore'
export interface ResponseData {
    data: string;
    status: number;
    statusText: string;
    headers: Record<string, string>;
    config: string;
}

export interface AdminLoginData {
    datas: {
        adminId: number;
        adminName: string;
        // 다른 필요한 필드 추가
    };
    status: number;
    statusText: string;
    headers: Record<string, string>;
    config: string;
}

export interface datas {
    adminId: number;
    adminName: string;
    // 다른 필요한 필드 추가
};


export interface UserAdditionalInfoRes {
    status: number;
    data: UserAdditionalInfoType;
}

export interface EventDetailRes {
    status: number;
    data: eventDetail;
}

export interface GenderStatisticRes {
    status: number;
    data: number[]
}