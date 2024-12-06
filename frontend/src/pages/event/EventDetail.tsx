import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useStore } from 'zustand';
import { eventDetail } from '../../service/event/eventApi';
import { eventDetailStore } from '../../stores/EventStore';
import { useEventStore } from '../../types/StoreType/EventState';
import Calendar from 'react-calendar';
import CustomCalendar from '../../components/ticketing/CustomCalendar';
import UserHeader from '../../components/@common/UserHeader';
const EventDetail: React.FC = () => {
    const navi = useNavigate();
    const event = useStore(eventDetailStore)
    const [showPrice, setShowPrice] = useState(false);
    const togglePrice = () => {
        setShowPrice((prev) => !prev);
    };
    const {
        setEventId,
        setEventScheduleId,
        setEventDate,
        setEventTime
    } = useEventStore();






    let choiceId = 1;// 임시 EVENTID



    useEffect(() => {
        eventDetailGet();
    }, [])

    const eventDetailGet = async () => {
        await eventDetail(
            choiceId,
            (response) => {
                const res = response.data
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
                event.setPartitionPrice(res.partitionPrice)
                event.setScheduleMap(res.scheduleMap);
                setEventId(choiceId);


            }, (error) => {

            }
        )
    }

    const choiceDate = async () => {
        let asd = "2024-12-26T17:00:00";
        event.setTicketingStep(true);
        event.setScheduleId(3);
        event.setChoiceDate(asd.split("T")[0]);
        event.setChoicetime(asd.split("T")[1]);
        event.setRound(1);
    }


    const goTicketing = async () => {
        // navi("/ticketing/select-date")
        let url = "";
        if (event.ticketingStep) {
            url = "/ticketing/select-seat";
        } else {
            url = "/ticketing/select-date";
        }
        window.open(
            url,
            '_blank', // 새 창 이름
            `width=900,height=600,top=300,left=450,resizable=no,scrollbars=no,toolbar=no,menubar=no,status=no`
        );
    }

    return (
        <div>

            {/** TESt  */}
            {/**아래가 개발 코드 여기는 테스트 코드  */}
            <div>
                <h1>이벤트 디테일 페이지 입니다</h1>
                <button onClick={choiceDate} className='bg-black text-white'>
                    날짜선택버튼
                </button>
                <br></br>
                <button onClick={goTicketing} className='bg-black text-white'>
                    예약하기
                </button>
            </div>
            <br></br>
            <hr className='border-4' /><br></br>
            {/**아래가 개발 코드 위는 테스트 코드  */}


            <div className="ticket-page p-6 font-sans">

                {/* 상단 헤더 영역 */}
                <UserHeader />
                <hr className='mt-[15px] mb-[50px]' />

                {/* 공연 정보 영역 */}
                <div className='mx-[330px]'>

                    <h1 className='text-[24px] font-medium mb-[25px]'
                    >{event.title}</h1>
                    <div className='flex'>
                        <img src={event.posterPcMainUrl} alt="" />
                        <div className='ml-[55px] mt-[20px]'>
                            <div className='flex mb-[20px]'>
                                <p className='text-[16px] w-[90px]'>
                                    장소</p>
                                <p>{event.stageName}</p>
                            </div>
                            <div className='flex mb-[20px]'>
                                <p className='text-[16px] w-[90px]'>
                                    공연기간</p>
                                <p className="text-[16px]">
                                    {
                                        (Object.keys(event.scheduleMap).at(-1) || "").replace(/-/g, '.')
                                        + " ~ "
                                        + (Object.keys(event.scheduleMap)[0] || "").replace(/-/g, '.')
                                    }
                                </p>
                            </div>
                            <div className='flex mb-[20px]'>
                                <p className='text-[16px] w-[90px]'>
                                    공연시간</p>
                                <p>{event.runningTime}분</p>
                            </div>
                            <div className='flex mb-[20px]'>
                                <p className='text-[16px] w-[90px]'>
                                    관람연령</p>
                                <p>{event.age.replaceAll('_', ' ')}</p>
                            </div>
                            <div className='flex mb-[20px]'>
                                <p className='text-[16px] w-[90px]'>
                                    가격</p>
                                <div className='relative'>
                                    <button
                                        onClick={togglePrice}
                                        className='font-bold text-[16px] absolute top-0 left-0 w-[100px]'
                                    >
                                        전체 가격 보기
                                    </button>

                                    {/* 가격 창 */}
                                    {showPrice && (
                                        <div className=' mt-[35px]'>
                                            {event.partitionPrice.map((element, index) => (
                                                <div className='flex' key={index}>
                                                    <p className=' my-[5px] '>
                                                        {element["partitionName"]}석&nbsp;
                                                    </p>
                                                    <p className=' my-[5px]'>
                                                        {Intl.NumberFormat().format(Number(element["partitionPrice"].replace(",", "")))} 원
                                                    </p>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                        {/** 달력 */}
                        <div className='ml-[200px]'>
                            <Calendar />
                        </div>
                    </div>
                    <div className='flex flex-col'>
                        asd
                    </div>

                </div>
                {/* 아래는아직 미구현 껍대기 나중에 삭제*/}
                <div className="performance-info flex mt-[20px] mb-8">

                    <div className="details">
                        <h2 className="text-xl font-semibold mb-4">
                            2023 클래식콘서트: 불멸의 '비발디'
                        </h2>
                        <p className="text-gray-700">
                            공연기간: 2023.01.01 ~ 2023.01.31
                            <br />
                            공연시간: 매주 토요일 오후 5시
                            <br />
                            장소: 예술의전당 콘서트홀
                        </p>
                    </div>
                </div>

                {/* 달력 영역 */}
                <div className="calendar-section mb-8">
                    <h3 className="text-lg font-medium mb-4">예매하기</h3>
                    <Calendar />
                </div>

                {/* 이미지 미리보기 영역 */}
                <div className="preview-section mb-8">
                    <h3 className="text-lg font-medium mb-4">좌석배치 & 공연정보</h3>
                    <div className="preview-box bg-gray-200 flex items-center justify-center h-52 rounded-md">
                        {/* Placeholder 이미지 */}
                        <span className="text-gray-500 text-lg">이미지</span>
                    </div>
                </div>

                {/* 예매자 통계 영역 */}
                <div className="stats-section">
                    <h3 className="text-lg font-medium mb-4">예매자 통계</h3>
                    <div className="stats-container flex gap-6">
                        <div className="age-stat bg-gray-100 p-4 rounded-md w-1/2 shadow-md">
                            <h4 className="text-md font-medium mb-2">연령 통계</h4>
                            {/* 연령 통계 차트 이미지 또는 컴포넌트 */}
                            <div className="chart-placeholder bg-gray-300 h-36 rounded-md flex items-center justify-center">
                                <span className="text-gray-500 text-sm">차트 이미지</span>
                            </div>
                        </div>
                        <div className="gender-stat bg-gray-100 p-4 rounded-md w-1/2 shadow-md">
                            <h4 className="text-md font-medium mb-2">성별 통계</h4>
                            {/* 성별 통계 차트 이미지 또는 컴포넌트 */}
                            <div className="chart-placeholder bg-gray-300 h-36 rounded-md flex items-center justify-center">
                                <span className="text-gray-500 text-sm">차트 이미지</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div >
    )
}

export default EventDetail;