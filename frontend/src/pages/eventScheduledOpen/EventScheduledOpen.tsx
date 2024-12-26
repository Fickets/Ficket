import { useEffect, useState } from 'react';
import { useMediaQuery } from 'react-responsive';
import { searchEventScheduledOpen } from '../../service/eventScheduledOpen/api.ts';
import {
  PageDTO,
  EventScheduledOpenResponse,
  SearchParams,
} from '../../types/eventScheduledOpen.ts';
import MobileEventOpenList from '../../components/eventScheduledOpen/MobileOpenEventList.tsx';
import PcEventOpenList from '../../components/eventScheduledOpen/PcOpenEventList.tsx';
import MobileHeader from '../../components/@common/MobileHeader.tsx';
import UserHeader from '../../components/@common/UserHeader.tsx';

const EventScheduledOpen = () => {
  const isMobile = useMediaQuery({ query: '(max-width: 768px)' });

  const [searchParams, setSearchParams] = useState<SearchParams>({
    searchValue: null,
    genre: null,
    page: 0,
    size: 15,
    sort: 'createdAt,DESC',
  });

  const [response, setResponse] =
    useState<PageDTO<EventScheduledOpenResponse> | null>(null);

  const fetchEventScheduledOpen = async (
    searchCond: SearchParams
  ): Promise<void> => {
    const response = await searchEventScheduledOpen(searchCond);
    setResponse(response);
  };

  useEffect(() => {
    const fetchData = async () => {
      await fetchEventScheduledOpen(searchParams);
    };

    fetchData();
  }, [searchParams]);

  // 검색 조건 변경 핸들러
  const handleSearchParamsChange = (newParams: Partial<SearchParams>) => {
    setSearchParams((prev) => ({
      ...prev,
      ...newParams,
      page: newParams.page !== undefined ? newParams.page : 0, // 페이지 초기화 (검색 조건 변경 시)
    }));
  };

  return (
    <div>
      {response?.content?.length ? (
        isMobile ? (
          <div>
            <MobileHeader title={'오픈 티켓'} />
            <MobileEventOpenList
              events={response}
              searchParams={searchParams}
              onSearchParamsChange={handleSearchParamsChange}
            />
          </div>
        ) : (
          <div>
            <UserHeader />
            <PcEventOpenList
              events={response}
              searchParams={searchParams}
              onSearchParamsChange={handleSearchParamsChange}
            />
          </div>
        )
      ) : (
        <div>로딩중...</div>
      )}
    </div>
  );
};

export default EventScheduledOpen;
