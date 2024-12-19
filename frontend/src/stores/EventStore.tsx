import { create } from "zustand";
import { persist } from "zustand/middleware";
import {
  eventDetail,
  eventScheduleDto,
} from "../types/StoreType/EventDetailStore";
import { subscribeWithSelector } from "zustand/middleware";
export const eventDetailStore = create(
  subscribeWithSelector(
    persist<eventDetail>(
      (set) => ({
        eventId: "",
        adminId: 0,
        companyId: 0,
        companyName: "",
        stageId: 0,
        stageName: "",
        sido: "",
        sigungu: "",
        street: "",
        eventStageImg: "",
        genre: [],
        age: "",
        content: "",
        title: "",
        subTitle: "",
        ticketingTime: "",
        runningTime: 0,
        reservationLimit: 0,
        posterMobileUrl: "",
        posterPcUrl: "",
        posterPcMainUrl: "",
        partitionPrice: [],
        scheduleMap: {},

        // 예매 단계 선택
        ticketingStep: false,
        scheduleId: 0,
        choiceDate: "",
        choiceTime: "",
        round: 0,
        faceImg: null,
        selectedSeats: [],

        setTicketingStep: (newStep: boolean) =>
          set(() => ({ ticketingStep: newStep })),
        setScheduleId: (newId: number) => set(() => ({ scheduleId: newId })),
        setChoiceDate: (newDate: string) =>
          set(() => ({ choiceDate: newDate })),
        setChoicetime: (newTime: string) =>
          set(() => ({ choiceTime: newTime })),
        setRound: (newRound: number) => set(() => ({ round: newRound })),
        setFaceImg: (newImg: File | null) => set(() => ({ faceImg: newImg })),
        setSelectedSeats: (
          newSeats: {
            seatMappingId: number;
            grade: string;
            row: string;
            col: string;
            price: number;
          }[],
        ) => set(() => ({ selectedSeats: newSeats })),

        // set 함수들
        setEventId: (newId: string) => set(() => ({ eventId: newId })),
        setAdminId: (newId: number) => set(() => ({ adminId: newId })),
        setCompanyId: (newId: number) => set(() => ({ companyId: newId })),
        setCompanyName: (newName: string) =>
          set(() => ({ companyName: newName })),
        setStageId: (newId: number) => set(() => ({ stageId: newId })),
        setStageName: (newName: string) => set(() => ({ stageName: newName })),
        setSido: (newSido: string) => set(() => ({ sido: newSido })),
        setSigungu: (newSigungu: string) =>
          set(() => ({ sigungu: newSigungu })),
        setStreet: (newStreet: string) => set(() => ({ street: newStreet })),
        setEventStageImg: (newImg: string) =>
          set(() => ({ eventStageImg: newImg })),
        setGenre: (newGenre: string[]) => set(() => ({ genre: newGenre })),
        setAge: (newAge: string) => set(() => ({ age: newAge })),
        setContent: (newContent: string) =>
          set(() => ({ content: newContent })),
        setTitle: (newTitle: string) => set(() => ({ title: newTitle })),
        setSubTitle: (newSubTitle: string) =>
          set(() => ({ subTitle: newSubTitle })),
        setTicketingTime: (newTime: string) =>
          set(() => ({ ticketingTime: newTime })),
        setRunningTime: (newTime: number) =>
          set(() => ({ runningTime: newTime })),
        setReservationLimit: (newLimit: number) =>
          set(() => ({ reservationLimit: newLimit })),
        setPosterMobileUrl: (newUrl: string) =>
          set(() => ({ posterMobileUrl: newUrl })),
        setPosterPcUrl: (newUrl: string) =>
          set(() => ({ posterPcUrl: newUrl })),
        setPosterPcMainUrl: (newUrl: string) =>
          set(() => ({ posterPcMainUrl: newUrl })),
        setPartitionPrice: (newPartitionPrice: { [key: string]: string }[]) =>
          set(() => ({ partitionPrice: newPartitionPrice })),
        setScheduleMap: (newScheduleMap: {
          [key: string]: { [key: number]: eventScheduleDto };
        }) => set(() => ({ scheduleMap: newScheduleMap })),
      }),
      { name: "EVENTDETAIL_STORE" },
    ),
  ),
);
