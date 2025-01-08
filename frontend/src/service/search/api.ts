import { publicApi } from "../../utils/http-common.ts";
import {
  autoCompleteRes,
  SortBy,
  SaleType,
  SearchParams,
  SearchResult,
} from "../../types/search.ts";
import { AxiosResponse } from "axios";
import qs from "qs";

export const searchAutoComplete = async (
  keyword: string,
): Promise<autoCompleteRes[]> => {
  const response: AxiosResponse<autoCompleteRes[]> = await publicApi.get(
    `/search/auto-complete`,
    {
      params: {
        query: keyword,
      },
    },
  );

  return response.data;
};

export const searchByFilter = async (
  params: SearchParams,
): Promise<SearchResult> => {
  const response: AxiosResponse<SearchResult> = await publicApi.get(
    "/search/detail",
    {
      params: {
        genreList: params.genreList,
        locationList: params.locationList,
        title: params.title,
        startDate: params.startDate,
        endDate: params.endDate,
        saleTypeList: params.saleTypeList || [
          SaleType.ON_SALE,
          SaleType.TO_BE_SALE,
        ],
        sortBy: params.sortBy || SortBy.SORT_BY_ACCURACY,
        pageNumber: params.pageNumber || 1,
        pageSize: params.pageSize || 20,
      },
      paramsSerializer: {
        serialize: (params) => {
          return qs.stringify(params, { arrayFormat: "comma" }); // 배열 직렬화 방식을 쉼표로 설정
        },
      },
    },
  );

  return response.data;
};
