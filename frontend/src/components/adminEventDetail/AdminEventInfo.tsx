import { useEffect, useState } from 'react';
import { useStore } from 'zustand';

import { eventDetail } from '../../service/event/eventApi';
import { eventDetailStore } from '../../stores/EventStore';
import '../../pages/event/CustomDetailCalendar.css';
import 'react-calendar/dist/Calendar.css';

const AdminEventInfo = ({ eventId }: { eventId: string }) => {
  const event = useStore(eventDetailStore);

  const [showPrice, setShowPrice] = useState(false);
  const [activeTab, setActiveTab] = useState('performance');
  const eventDates = Object.keys(event.scheduleMap);

  const togglePrice = () => {
    setShowPrice((prev) => !prev);
  };

  const eventDetailGet = async () => {
    await eventDetail(
      Number(eventId),
      (response) => {
        const res = response.data;
        event.setAdminId(res.adminId);
        event.setCompanyId(res.companyId);
        event.setCompanyName(res.companyName);
        event.setStageId(res.stageId);
        event.setStageName(res.stageName);
        event.setSido(res.sido);
        event.setSigungu(res.sigungu);
        event.setStreet(res.street);
        event.setEventStageImg(res.eventStageImg);
        event.setGenre(res.genre);
        event.setAge(res.age);
        event.setContent(res.content);
        event.setTitle(res.title);
        event.setSubTitle(res.subTitle);
        event.setTicketingTime(res.ticketingTime);
        event.setRunningTime(res.runningTime);
        event.setReservationLimit(res.reservationLimit);
        event.setPosterMobileUrl(res.posterMobileUrl);
        event.setPosterPcUrl(res.posterPcUrl);
        event.setPosterPcMainUrl(res.posterPcMainUrl);
        event.setPartitionPrice(res.partitionPrice);
        event.setScheduleMap(res.scheduleMap);
        event.setEventId(eventId);
      },
      (_error) => { }
    );
  };

  useEffect(() => {
    eventDetailGet();
  }, []);

  return (
    <div>
      {/** PC SCREEN */}
      <div className="ticket-page p-6 font-sans hidden md:block">
        <hr className="mt-[15px] mb-[50px]" />

        {/* 공연 정보 영역 */}
        <div className="ml-[230px] ">
          <h1 className="text-[24px] font-medium mb-[25px]">{event.title}</h1>
          <div className="flex">
            <img src={event.posterPcMainUrl} alt="" />
            <div className="ml-[55px] mt-[20px] min-w-[500px] max-w-[500px]">
              <div className="flex mb-[20px]">
                <p className="text-[16px] w-[90px]">장소</p>
                <p>{event.stageName}</p>
              </div>
              <div className="flex mb-[20px]">
                <p className="text-[16px] w-[90px]">공연기간</p>
                <p className="text-[16px]">
                  {(Object.keys(event.scheduleMap).at(-1) || '').replace(
                    /-/g,
                    '.'
                  ) +
                    ' ~ ' +
                    (Object.keys(event.scheduleMap)[0] || '').replace(
                      /-/g,
                      '.'
                    )}
                </p>
              </div>
              <div className="flex mb-[20px]">
                <p className="text-[16px] w-[90px]">공연시간</p>
                <p>{event.runningTime}분</p>
              </div>
              <div className="flex mb-[20px]">
                <p className="text-[16px] w-[90px]">관람연령</p>
                <p>{event.age.replace(/_/g, ' ')}</p>
              </div>
              <div className="flex mb-[20px]">
                <h2 className="w-[90px]">장르</h2>
                <div className="flex">
                  {' '}
                  {/* 외부 <p>를 <div>로 변경 */}
                  {event.genre.map((element, index) => (
                    <p key={index}>
                      {element.replace(/_/g, '/')}&nbsp;&nbsp;
                    </p>
                  ))}
                </div>
              </div>
              <div className="flex mb-[20px]">
                <p className="text-[16px] w-[90px]">가격</p>
                <div className="relative">
                  <button
                    onClick={togglePrice}
                    className="font-bold text-[16px] absolute top-0 left-0 w-[100px]"
                  >
                    전체 가격 보기
                  </button>

                  {/* 가격 창 */}
                  {showPrice && (
                    <div className=" mt-[35px]">
                      {event.partitionPrice.map((element, index) => (
                        <div className="flex" key={index}>
                          <p className=" my-[5px] ">
                            {element['partitionName']}석&nbsp;
                          </p>
                          <p className=" my-[5px]">
                            {Intl.NumberFormat().format(
                              Number(element['partitionPrice'].replace(',', ''))
                            )}{' '}
                            원
                          </p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/** 공연/판매 정보 */}
          <div className="flex flex-col">
            <br></br>
            <br></br>
          </div>
          <div className="w-[840px] ">
            {/* Tab Header */}
            <div className="flex border-b border-gray-300 sticky top-0 bg-white">
              <button
                className={`flex-1 text-center py-2 ${activeTab === 'performance'
                  ? 'border-b-2 border-black font-semibold'
                  : 'text-gray-500'
                  }`}
                onClick={() => setActiveTab('performance')}
              >
                공연 정보
              </button>
              <button
                className={`flex-1 text-center py-2 ${activeTab === 'sales'
                  ? 'border-b-2 border-black font-semibold'
                  : 'text-gray-500'
                  }`}
                onClick={() => setActiveTab('sales')}
              >
                판매 정보
              </button>
            </div>

            {/* Tab Content */}
            <div className="mt-6">
              {activeTab === 'performance' && (
                <div>
                  <div dangerouslySetInnerHTML={{ __html: event.content }} />
                  <br></br>
                </div>
              )}

              {activeTab === 'sales' && (
                <div>
                  <h2 className="text-xl font-bold">판매 정보</h2>
                  <div className="max-w-4xl">
                    {/** 시작  */}
                    <div className="max-w-4xl  p-[20px]">
                      <div className="bg-white">
                        {/* 상품 관련 정보 테이블 */}
                        <section className="mb-6">
                          <h2 className="text-lg font-semibold text-gray-800 mb-4">
                            상품 관련 정보
                          </h2>
                          <div className="min-w-full">
                            <tbody className="border">
                              <div className="flex">
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  주최/기획
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  {event.companyName}
                                </p>
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  고객문의
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  02-6467-2200
                                </p>
                              </div>
                              <div className="flex">
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  상영시간
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  {event.runningTime}분
                                </p>
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  관람연령
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  {event.age.replace(/_/g, ' ')}
                                </p>
                              </div>
                              <div className="flex">
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  예매수수료
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  장당 2,000원
                                </p>
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  공연장
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  {event.sido} {event.sigungu} {event.street}{' '}
                                  {event.stageName}
                                </p>
                              </div>
                              <div className="flex">
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  예매시작시간
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  {event.ticketingTime.replace('T', ' ')}
                                </p>
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  예매가능기간
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  {eventDates.at(-1)} ~ {eventDates[0]} 오전
                                  11시
                                </p>
                              </div>
                              <div className="flex">
                                <p className="px-4 py-[10px] bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  예매취소조건
                                </p>
                                <div className="flex flex-col">
                                  <p className="p-[10px] w-[720px] border">
                                    취소일자에 따라서 아래와 같이 취소수수료가
                                    부과됩니다. 예매 일 기준보다 관람일 기준이
                                    우선 적용됩니다. 단, 예매 당일 밤 12시 이전
                                    취소 시에는 취소수수료가 없으며, 예매
                                    수수료도 환불됩니다.(취소기한 내에 한함)
                                  </p>
                                  <div className="p-[5px] flex w-[720px] ">
                                    <p className="bg-gray-100 w-[360px] h-[50px] border text-center flex items-center justify-center leading-none">
                                      취소일
                                    </p>
                                    <p className="bg-gray-100 w-[360px] h-[50px] border leading-none items-center flex justify-center">
                                      취소수수료
                                    </p>
                                  </div>
                                  <div className="flex">
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-r-[1px] border-b-[1px]  flex items-center leading-none">
                                      예매 후 7일 이내
                                    </p>
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-b-[1px]  flex items-center leading-none">
                                      없음
                                    </p>
                                  </div>
                                  <div className="flex">
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-r-[1px] border-b-[1px]  flex items-center leading-none">
                                      예매 후 8일~관람일 10일전까지
                                    </p>
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-b-[1px]  flex items-center leading-none">
                                      장당 4,000원(티켓금액의 10%한도)
                                    </p>
                                  </div>
                                  <div className="flex">
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-r-[1px] border-b-[1px]  flex items-center leading-none">
                                      관람일 9일전~7일전까지
                                    </p>
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-b-[1px]  flex items-center leading-none">
                                      티켓금액의 10%
                                    </p>
                                  </div>
                                  <div className="flex">
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-r-[1px] border-b-[1px]  flex items-center leading-none">
                                      관람일 6일전~3일전까지
                                    </p>
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-b-[1px]  flex items-center leading-none">
                                      티켓금액의 20%
                                    </p>
                                  </div>
                                  <div className="flex">
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-r-[1px] border-b-[1px]  flex items-center leading-none">
                                      관람일 2일전~1일전까지
                                    </p>
                                    <p className="text-[14px] pl-[15px] w-[360px] h-[30px] border-b-[1px]  flex items-center leading-none">
                                      티켓금액의 30%
                                    </p>
                                  </div>
                                </div>
                              </div>
                              <div className="flex">
                                <p className="px-4 py-[10px] bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  취소환불방법
                                </p>
                                <div className="flex flex-col border-b-[1px]">
                                  <p className="text-[14px] mt-[10px] pl-[15px] w-[720px]   flex items-center leading-none">
                                    - My티켓 &gt; 예매/취소내역에서 직접 취소
                                    또는 고객센터 (1544-1234)를 통해서 예매를
                                    취소할 수 있습니다.
                                  </p>
                                  <p className="text-[14px] my-[10px] pl-[15px] w-[720px]">
                                    - 티켓이 배송된 이후에는 인터넷 취소가
                                    안되며, 취소마감 시간 이전에 티켓이 피켓
                                    고객센터로 반송되어야 취소 가능합니다.
                                    취소수수료는 도착일자 기준으로 부과되며,
                                    배송료는 환불되지 않습니다.
                                  </p>
                                </div>
                              </div>
                              <br></br>
                              <div>
                                <h2 className="text-[20px] w-[500px]">
                                  예매 유의사항
                                </h2>
                                <p className="ml-[20px] w-[820px]">
                                  - 다른 이용자의 원활한 예매 및 취소에 지장을
                                  초래할 정도로 반복적인 행위를 지속하는 경우
                                  회원의 서비스 이용을 제한할 수 있습니다.
                                </p>
                                <p className="ml-[20px] w-[820px]">
                                  - 일부 상품의 판매 오픈 시 원활한 서비스
                                  제공을 위하여 특정 결제수단 이용이 제한될 수
                                  있습니다.
                                </p>
                              </div>
                              <br></br>
                              <div>
                                <h2 className="text-[20px] w-[500px]">
                                  환불안내
                                </h2>
                                <p className="ml-[20px] w-[820px] font-medium">
                                  신용카드 결제의 경우{' '}
                                </p>
                                <p className="ml-[20px] w-[820px]">
                                  - 일반적으로 당사의 취소 처리가 완료되고 4~5일
                                  후 카드사의 취소가 확인됩니다. (체크카드 동일)
                                </p>
                                <p className="ml-[20px] w-[820px]">
                                  - 예매 취소 시점과 해당 카드사의 환불
                                  처리기준에 따라 취소금액의 환급방법과 환급일은
                                  다소 차이가 있을 수 있으며,
                                </p>
                                <p className="ml-[20px] w-[820px]">
                                  &nbsp;&nbsp;예매 취소시 기존에 결제하였던
                                  내역을 취소하며 최초 결제하셨던 동일카드로
                                  취소 시점에 따라 취소수수료와{' '}
                                </p>
                                <p className="ml-[20px] w-[820px]">
                                  &nbsp;&nbsp;배송료를 재승인 합니다.
                                </p>
                                <p className="ml-[20px] w-[820px]">- </p>
                              </div>
                            </tbody>
                          </div>
                          <div></div>
                        </section>
                      </div>
                    </div>
                    {/** 끝  */}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminEventInfo;
