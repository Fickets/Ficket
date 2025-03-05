import { UserAdditionalInfoType } from "./UserAdditionalInfoType";


export interface UserAdditionalInfoRes {
    status: number;
    data: UserAdditionalInfoType;
}

export interface GetMyInfo {
    status: number;
    data: UserAdditionalInfoType;
}
