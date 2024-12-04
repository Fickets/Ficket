import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useStore } from 'zustand';
import { eventDetail } from '../../service/event/eventApi';
import { eventDetailStore } from '../../stores/EventStore';
import { useEventStore } from '../../types/StoreType/EventState';
function EventDetailPage() {
    const navi = useNavigate();
    const event = useStore(eventDetailStore)

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
                console.log(response.data)
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
                event.setScheduleMap(res.scheduleMap);
                setEventId(choiceId);


            }, (error) => {

            }
        )
    }

    const choiceDate = async () => {
        let asd = "2024-12-26 17:00:00";
        event.setScheduleId(3);
        event.setChoiceDate(asd.split("T")[0]);
        event.setChoicetime(asd.split("T")[1]);
        event.setRound(1);
    }


    const goTicketing = async () => {
        navi("/ticketing/select-date")
    }

    return (
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
    )
}

export default EventDetailPage;