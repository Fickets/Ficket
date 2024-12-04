import { AxiosResponse } from "axios";
import { privateApi, publicApi } from "../../utils/http-common";
import { ResponseData, EventDetailRes } from "../../types/ApiResponseType";

const url = "events"

export const eventDetail = async (
    eventId: number,
    Response: (Response: AxiosResponse<EventDetailRes>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void) => {
    await publicApi.get(`/${url}/detail/${eventId}`)
        .then(Response)
        .catch(Error)
}