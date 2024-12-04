export interface eventDetail {

    adminId: number; // 관리자 ID
    companyId: number; // 회사 ID
    companyName: string; // 회사 이름
    stageId: number; // 공연장 ID
    stageName: string;
    sido: string;
    sigungu: string;
    street: string;
    eventStageImg: string;

    genre: string[];
    age: string;
    content: string;
    title: string;
    subTitle: string;
    ticketingTime: string;
    runningTime: number;
    reservationLimit: number;
    posterMobileUrl: string;
    posterPcUrl: string;
    scheduleMap: { [key: string]: { [key: number]: eventScheduleDto } };


    // eventStatus
    ticketingStep: boolean,
    scheduleId: number,
    choiceDate: string,
    choiceTime: string,
    round: number,
    faceImg: File | null;
    selectedSeats: {
        seatMappingId: number;
        grade: string;
        row: string;
        col: string;
    }[];

    setTicketingStep: (newStep: boolean) => void;
    setScheduleId: (newId: number) => void;
    setChoiceDate: (newDate: string) => void;
    setChoicetime: (newTime: string) => void;
    setRound: (newRound: number) => void;
    setFaceImg: (file: File | null) => void;
    setSelectedSeats: (seats: {
        seatMappingId: number;
        grade: string;
        row: string;
        col: string;
    }[]) => void;


    setAdminId: (newId: number) => void;
    setCompanyId: (newId: number) => void;
    setCompanyName: (newName: string) => void;
    setStageId: (newId: number) => void;
    setStageName: (newName: string) => void;
    setSido: (newSido: string) => void;
    setSigungu: (newSigungu: string) => void;
    setStreet: (newStreet: string) => void;
    setEventStageImg: (newImg: string) => void;
    setGenre: (newGenre: string[]) => void;
    setAge: (newAge: string) => void;
    setContent: (newContent: string) => void;
    setTitle: (newTitle: string) => void;
    setSubTitle: (newSubTitle: string) => void;
    setTicketingTime: (newTime: string) => void;
    setRunningTime: (newTime: number) => void;
    setReservationLimit: (newLimit: number) => void;
    setPosterMobileUrl: (newUrl: string) => void;
    setPosterPcUrl: (newUrl: string) => void;
    setScheduleMap: (newScheduleMap: { [key: string]: { [key: number]: eventScheduleDto } }) => void;

}

export interface eventScheduleDto {
    eventScheduleId: number;
    round: number;
    eventDate: string;
    partition: { [key: string]: partitionDto };

}
export interface partitionDto {
    partitionName: string;
    remainingSeats: number;
}