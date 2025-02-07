import { AxiosResponse } from "axios";
import { publicApi } from "../../utils/http-common";
import { ResponseData } from "../../types/ApiResponseType";
import { Period } from "../../types/ReservationRateRanking";
import { SimplePageRes } from "../../types/home";
export const getArea = async (
    Response: (Response: AxiosResponse<string[]>) => void,
    Error: (Error: AxiosResponse<ResponseData>) => void,
) => {
    await publicApi.get(`/events/area`).then(Response).catch(Error);
};

export const getGenreAreaPeriod = async (
    genre: string,
    period: Period,
    area: string,
    page: number = 0,
    size: number = 10,
    sort: string = "eventDate",
    direction: "ASC" | "DESC" = "DESC"
): Promise<SimplePageRes> => {
    try {
        const response = await publicApi.get(`/events/genre-search`, {
            params: {
                genre,
                period,
                area,
                page,
                size,
                sort,
                direction,
            },
        });
        return response.data
    } catch (error) {
        throw error;
    }
};