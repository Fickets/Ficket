import { UserAdditionalInfoType } from "./users/UserAdditionalInfoType";
import { eventDetail} from './StoreType/EventDetailStore'
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

export interface EventDetailRes {
    status: number;
    data: eventDetail;
}