import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useStore } from "zustand";
import moment from "moment";
import { Bar } from "react-chartjs-2";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { ChartOptions } from "chart.js";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import ChartDataLabels from "chartjs-plugin-datalabels";
import MobileHeader from "../../components/@common/MobileHeader";
import { Helmet } from "react-helmet-async";
import { canEnterTicketingPage, occupySlot } from "../../service/queue/api.ts";
import {
  checkEnterTicketing,
  eventDetail,
  genderStatistic,
} from "../../service/event/eventApi";
import { eventDetailStore } from "../../stores/EventStore";
import { useEventStore } from "../../types/StoreType/EventState";
import Calendar from "react-calendar";
import "./CustomDetailCalendar.css";
import "react-calendar/dist/Calendar.css";
import { eventScheduleDto } from "../../types/StoreType/EventDetailStore";
import { userStore } from "../../stores/UserStore";
import UserHeader from "../../components/@common/UserHeader";
import manImg from "../../assets/detail/man.png";
import womanImg from "../../assets/detail/woman.png";
import mappin from "../../assets/detail/MapPin.png";
import calendar from "../../assets/detail/Calendar.png";
import calculator from "../../assets/detail/Calculator.png";
import { Value } from "react-calendar/dist/esm/shared/types.js";
const EventDetail: React.FC = () => {
  const { eventId } = useParams();
  const navi = useNavigate();
  const event = useStore(eventDetailStore);
  const user = useStore(userStore);
  const [showPrice, setShowPrice] = useState(false);
  const [selectedButton, setSelectedButton] = useState<number>();
  const [eventRounds, setEventRounds] = useState<
    Record<string, eventScheduleDto>
  >({}); // Map 타입으로 초기화
  const [genderStatisticData, setGenderStatisticData] = useState<number[]>([
    0, 0, 0, 0, 0, 0, 0,
  ]);

  const [activeTab, setActiveTab] = useState("performance");
  // const [selectedButton, setSelectedButton] = React.useState(null);

  // Chart.js 모듈 등록
  ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ChartDataLabels,
  );

  const togglePrice = () => {
    setShowPrice((prev) => !prev);
  };
  const { setEventId } = useEventStore();

  const chartData = {
    labels: ["10대", "20대", "30대", "40대", "50대"], // x축에 표시될 레이블
    datasets: [
      {
        label: "Sales", // 범례에 표시될 라벨
        data: genderStatisticData.slice(2), // 데이터의 슬라이스
        backgroundColor: "#5B4DFF", // 바 색상
        borderColor: "#5B4DFF", // 테두리 색상
        borderWidth: 1, // 테두리 두께
      },
    ],
  };

  const chartOptions: ChartOptions<"bar"> = {
    responsive: true,
    plugins: {
      legend: {
        display: false, // 범례 비활성화
        position: "top",
        labels: {
          font: {
            size: 14,
            family: "Arial",
            weight: "bold",
          },
          color: "#5B4DFF",
          padding: 10,
        },
      },
      tooltip: {
        enabled: false, // 툴팁 비활성화
      },
      datalabels: {
        color: "#5B4DFF",
        anchor: "end",
        align: "end",
        formatter: (value) => `${value}%`, // 데이터 레이블 포맷
        font: {
          size: 12,
        },
      },
    },

    scales: {
      x: {
        display: true,
        type: "category",
      },
      y: {
        display: false,
        type: "linear",
      },
    },

    layout: {
      padding: {
        top: 10,
        bottom: 10,
        left: 10,
        right: 10,
      },
    },
  };

  // 여기부터 달력 함수 입니다 ------------------------------------------------

  const eventDates = Object.keys(event.scheduleMap);
  const [choiceDate, setChoiceDate] = useState<string | null>(null); // 선택된 날짜 상태

  // 예매 가능 날짜 설정
  const availableDates = eventDates.map((dateString) => {
    const [year, month, day] = dateString.split("-").map(Number);
    return new Date(year, month - 1, day); // month는 0부터 시작하므로 -1 필요
  });

  // 날짜 클릭 핸들러
  const handleDateClick = (value: Value) => {
    if (
      value &&
      value instanceof Date &&
      availableDates.some((d) => d.toDateString() === value.toDateString())
    ) {
      // Date 객체를 string으로 저장
      setChoiceDate(value.toDateString());

      const formattedDate = value.toLocaleDateString("en-CA");
      const selectedEventRounds = event.scheduleMap[formattedDate]; // store에서 event 데이터를 가져옴
      event.setChoiceDate(formattedDate);
      event.setTicketingStep(true);

      setEventRounds(selectedEventRounds);
      setSelectedButton(0);
      event.setRound(1);

      const scheduleData = event.scheduleMap[formattedDate][1];
      event.setScheduleId(scheduleData.eventScheduleId);
    }
  };

  // 클릭 불가능한 날짜 설정
  const tileDisabled = ({ date, view }: { date: Date; view: string }) => {
    if (view === "month") {
      // `availableDates`에 포함되지 않은 날짜는 클릭 비활성화
      return !availableDates.some(
        (d) => d.toDateString() === date.toDateString(),
      );
    }
    return false;
  };
  // 날짜별 스타일 적용
  const tileClassName = ({ date, view }: { date: Date; view: string }) => {
    if (view === "month") {
      // choiceDate가 string일 경우, Date 객체로 변환
      if (
        choiceDate &&
        new Date(choiceDate).toDateString() === date.toDateString()
      ) {
        return "selected-date"; // 선택된 날짜 스타일
      }
      if (
        availableDates.some((d) => d.toDateString() === date.toDateString())
      ) {
        return "available-date"; // 예매 가능 날짜 스타일
      }
    }
    return null;
  };

  // 달력 함수 END LINE ------------------------------------------------
  const roundButtonClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    const key = parseInt(e.currentTarget.getAttribute("data-key") || ""); // data-key 값을 숫자로 변환
    setSelectedButton(key); // 상태 업데이트
    console.log(`Clicked key: ${key}`);
    event.setRound(key + 1);
    const date = event.choiceDate;
    const scheduleData = event.scheduleMap[date][key + 1];
    event.setScheduleId(scheduleData.eventScheduleId);
  };

  useEffect(() => {
    eventDetailGet();
    genderStatisticGet();
  }, []);

  const genderStatisticGet = async () => {
    await genderStatistic(
      Number(eventId),
      (response) => {
        const res: string = response.data;

        // 문자열을 구분자로 분리해서 배열로 변환하고, 각 항목을 숫자로 변환
        const numericRes = res
          .split(",") // 예시로 콤마(,)로 구분된 문자열로 가정
          .map((item) => parseInt(item, 10))
          .filter((item) => !isNaN(item)); // NaN인 값은 필터링

        const statisticData = numericRes.slice(0, 2);
        const sum = statisticData.reduce(
          (acc, currentValue) => acc + currentValue,
          0,
        );

        numericRes.slice(2).forEach((value) => {
          const ageStatistic = (value / sum) * 100;
          statisticData.push(ageStatistic);
        });

        setGenderStatisticData(statisticData);
      },
      (_error) => { },
    );
  };

  const eventDetailGet = async () => {
    await eventDetail(
      Number(eventId),
      (response) => {
        console.log(response);
        const res = response.data;
        console.log(res);
        event.setEventId(eventId || "");
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
        event.setScheduleId(0);
        setEventId(Number(eventId));
      },
      (_error) => { },
    );
  };

  const goTicketing = async () => {
    if (user.isLogin) {
      let url = "";
      if (event.ticketingStep) {
        try {
          const availableCount = await checkEnterTicketing(event.scheduleId);
          event.setReservationLimit(availableCount);
          const canEnter = await canEnterTicketingPage(eventId as string);
          if (canEnter) {
            const occupied = await occupySlot(eventId as string);
            if (occupied) {
              url = "/ticketing/select-seat";
            } else {
              alert("슬롯 점유 실패. 다시 시도해 주세요.");
              return;
            }
          } else {
            url = `/ticketing/queue/${eventId}`;
          }
        } catch (error: any) {
          alert(`${error.message}`); // API에서 에러 발생 시 처리
          return; // 에러 발생 시 새 창 열기를 중단
        }
      } else {
        const canEnter = await canEnterTicketingPage(eventId as string);
        if (canEnter) {
          const occupied = await occupySlot(eventId as string);
          if (occupied) {
            url = "/ticketing/select-date";
          } else {
            alert("슬롯 점유 실패. 다시 시도해 주세요.");
            return;
          }
        } else {
          url = `/ticketing/queue/${eventId}`;
        }
      }
      window.open(
        url,
        "_blank", // 새 창 이름
        `width=900,height=600,top=300,left=450,resizable=no,scrollbars=no,toolbar=no,menubar=no,status=no`,
      );
    } else {
      if (event.choiceDate && event.round) {
        navi("/users/login");
      }
    }
  };

  const mobileGo = async () => {
    if (user.isLogin) {
      let url = "";
      const canEnter = await canEnterTicketingPage(eventId as string);
      if (canEnter) {
        const occupied = await occupySlot(eventId as string);
        if (occupied) {
          url = "/ticketing/select-date";
        } else {
          alert("슬롯 점유 실패. 다시 시도해 주세요.");
          return;
        }
      } else {
        url = `/ticketing/queue/${eventId}`;
      }
      navi(url);
    } else {
      toast.error("로그인이 필요합니다.", {
        position: "top-center",
        autoClose: 1000, // 3초 후 자동으로 닫힘
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
      });
    }
  };

  return (
    <div>
      <Helmet>
        <title>{event.title}</title>
      </Helmet>
      {/** PC SCREEN */}
      <div className="ticket-page p-6 font-sans hidden md:block">
        {/* 상단 헤더 영역 */}
        <UserHeader />

        {/* 공연 정보 영역 */}
        <div className="ml-[330px] ">
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
                  {(Object.keys(event.scheduleMap).at(-1) || "").replace(
                    /-/g,
                    ".",
                  ) +
                    " ~ " +
                    (Object.keys(event.scheduleMap)[0] || "").replace(
                      /-/g,
                      ".",
                    )}
                </p>
              </div>
              <div className="flex mb-[20px]">
                <p className="text-[16px] w-[90px]">공연시간</p>
                <p>{event.runningTime}분</p>
              </div>
              <div className="flex mb-[20px]">
                <p className="text-[16px] w-[90px]">관람연령</p>
                <p>{event.age.replace(/_/g, " ")}</p>
              </div>
              <div className="flex mb-[20px]">
                <h2 className="w-[90px]">장르</h2>
                <p className="flex">
                  {event.genre.map((element, index) => (
                    <p key={index}>{element.replace(/_/g, " ")}&nbsp;&nbsp;</p> // 각 항목을 <p> 태그로 렌더링
                  ))}
                </p>
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
                            {element["partitionName"]}석&nbsp;
                          </p>
                          <p className=" my-[5px]">
                            {Intl.NumberFormat().format(
                              Number(
                                element["partitionPrice"].replace(",", ""),
                              ),
                            )}{" "}
                            원
                          </p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
            {/** 달력/회차/예매버튼 */}
            <div className="fixed bg-white  top-[250px] ml-[900px]">
              <div className="ml-[0px] w-[340px]  border border-2 rounded-xl ">
                <h1 className="m-[20px] font-medium">관람일</h1>
                <Calendar
                  className="detailCalendar"
                  onChange={handleDateClick}
                  value={Object.keys(event.scheduleMap).at(-1)} // 초기 날짜 설정 (예매 가능 첫 날짜)
                  locale="ko-KR" // 한국어 설정
                  formatDay={(_locale, date) => moment(date).format("D")}
                  tileClassName={tileClassName}
                  tileDisabled={tileDisabled} // 클릭 비활성화 로직 추가
                  next2Label={null} // 다음 달 화살표 숨기기
                  prev2Label={null} // 이전 달 화살표 숨기기
                  showNeighboringMonth={false} // 이전/다음 달 날짜 숨기기
                />
                <hr className=" border  border-[#5B4DFF]" />
                <h1 className="my-[15px] ml-[20px] font-medium">회차</h1>
                <div className="flex w-[300px] h-[70px]  mx-[20px] overflow-x-auto">
                  {eventRounds &&
                    Object.entries(eventRounds).map(([, value], index) => (
                      <button
                        key={index}
                        data-key={index}
                        className={`flex-shrink-0 flex w-[150px] h-[50px] border border-[#8E43E7] justify-center items-center ${selectedButton === index
                            ? "bg-[#8E43E7] text-white"
                            : "bg-white"
                          }`}
                        onClick={(e) => roundButtonClick(e)}
                      // onClick={setSelectedButton(key)}
                      >
                        <p>{value["round"]}회</p> &nbsp;
                        <p>
                          {value["eventDate"].split("T")[1].split(":")[0] +
                            ":" +
                            value["eventDate"].split("T")[1].split(":")[1]}
                        </p>
                      </button>
                    ))}
                </div>
              </div>
              <div className="ml-[0px]">
                <button
                  className="mt-[30px] w-[340px] h-[50px] bg-[#8E43E7] font-bold text-white text-[20px]"
                  onClick={goTicketing}
                >
                  예매하기
                </button>
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
                className={`flex-1 text-center py-2 ${activeTab === "performance"
                    ? "border-b-2 border-black font-semibold"
                    : "text-gray-500"
                  }`}
                onClick={() => setActiveTab("performance")}
              >
                공연 정보
              </button>
              <button
                className={`flex-1 text-center py-2 ${activeTab === "sales"
                    ? "border-b-2 border-black font-semibold"
                    : "text-gray-500"
                  }`}
                onClick={() => setActiveTab("sales")}
              >
                판매 정보
              </button>
            </div>

            {/* Tab Content */}
            <div className="mt-6">
              {activeTab === "performance" && (
                <div>
                  <div dangerouslySetInnerHTML={{ __html: event.content }} />
                  <br></br>
                  {/* 예매자 통계 영역 */}
                  <div className="stats-section">
                    <h3 className="text-lg font-medium mb-4">예매자 통계</h3>
                    <div className="stats-container flex gap-6">
                      <div className="age-stat  p-4 rounded-md w-1/2 shadow-md border">
                        <h4 className="text-md font-medium mb-2">연령 통계</h4>
                        {/* 연령 통계 차트 이미지 또는 컴포넌트 */}
                        <div className="flex chart-placeholder h-[200px] rounded-md flex items-center justify-center">
                          <img src={manImg} className="w-[100px]" alt="" />
                          <div className="flex flex-col">
                            <p className="mt-[0px]">
                              남자 {genderStatisticData[0]}명
                            </p>
                            <p className="text-[30px] text-[#5B4DFF]">
                              {(genderStatisticData[0] /
                                (genderStatisticData[0] +
                                  genderStatisticData[1])) *
                                100}
                              %
                            </p>
                          </div>
                          <img src={womanImg} className="w-[100px]" alt="" />
                          <div className="flex flex-col">
                            <p className="mt-[10px]">
                              여자 {genderStatisticData[1]}명
                            </p>
                            <p className="text-[30px] text-[#5B4DFF]">
                              {(genderStatisticData[1] /
                                (genderStatisticData[0] +
                                  genderStatisticData[1])) *
                                100}
                              %
                            </p>
                          </div>
                        </div>
                      </div>
                      <div className="gender-stat p-4 rounded-md w-1/2 shadow-md border">
                        <h4 className="text-md font-medium mb-2">성별 통계</h4>
                        {/* 성별 통계 차트 이미지 또는 컴포넌트 */}
                        <div className="chart-placeholder h-[200px] rounded-md flex items-center justify-center">
                          <Bar data={chartData} options={chartOptions} />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {activeTab === "sales" && (
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
                                  {event.age.replace(/_/g, " ")}
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
                                  {event.sido} {event.sigungu} {event.street}{" "}
                                  {event.stageName}
                                </p>
                              </div>
                              <div className="flex">
                                <p className="px-4 py-2 bg-gray-100 text-gray-500 font-semibold text-[14px] w-[120px] border">
                                  예매시작시간
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] w-[300px] border">
                                  {event.ticketingTime.replace("T", " ")}
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
                                  신용카드 결제의 경우{" "}
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
                                  취소 시점에 따라 취소수수료와{" "}
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

      {/** APP SCREEN */}
      <div className="block md:hidden">
        <ToastContainer />
        <MobileHeader title={event.subTitle} />
        {/** 상단정보 */}
        <div className="flex flex-col mt-[60px]  mx-[30px]">
          <p className="text-[24px] font-bold">{event.title}</p>
          <div className="flex">
            <p className="flex">
              {event.genre.map((element, index) => (
                <p key={index} className="text-[#666666] text-[10px]">
                  {element.replace(/_/g, " ")}&nbsp;&nbsp;
                </p> // 각 항목을 <p> 태그로 렌더링
              ))}
            </p>
            <p className="text-[#666666] text-[10px]">
              {event.runningTime}분&nbsp;
            </p>
            <p className="text-[#666666] text-[10px]">
              {event.age.replace(/_/g, " ")}
            </p>
          </div>
          <div className="flex">
            <img src={event.posterMobileUrl} alt="" />
            <div className="ml-[30px] mt-[20px] min-w-[500px] max-w-[500px]">
              <div className="flex mb-[10px]">
                <img src={mappin} alt="" />
                <p className="ml-[5px] text-[12px]">{event.stageName}</p>
              </div>
              <div className="flex mb-[10px]">
                <img src={calendar} alt="" />
                <p className="ml-[5px] text-[12px]">
                  {(Object.keys(event.scheduleMap).at(-1) || "").replace(
                    /-/g,
                    ".",
                  ) +
                    " ~ " +
                    (Object.keys(event.scheduleMap)[0] || "").replace(
                      /-/g,
                      ".",
                    )}
                </p>
              </div>
              <div className="flex mb-[20px]">
                <img src={calculator} alt="" />
                <div className="relative">
                  <button
                    onClick={togglePrice}
                    className="font-bold text-[10px] absolute top-0 left-0 w-[100px]"
                  >
                    전체 가격 보기
                  </button>
                  {/* 가격 창 */}
                  {showPrice && (
                    <div className="fixed inset-0 bg-gray-500 bg-opacity-50 flex justify-center items-center">
                      <div className="bg-white p-5 rounded shadow-lg w-[300px]">
                        <h3 className="text-lg font-bold">가격 목록</h3>
                        <div>
                          {event.partitionPrice.map((element, index) => (
                            <div className="flex" key={index}>
                              <p className="my-[5px]">
                                {element["partitionName"]}석&nbsp;
                              </p>
                              <p className="my-[5px]">
                                {Intl.NumberFormat().format(
                                  Number(
                                    element["partitionPrice"].replace(",", ""),
                                  ),
                                )}{" "}
                                원
                              </p>
                            </div>
                          ))}
                        </div>
                        <button
                          onClick={togglePrice}
                          className="mt-3 w-full bg-blue-500 text-white p-2 rounded"
                        >
                          닫기
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
        {/** 공연/판매 정보 */}
        <div>
          <br></br>
          <div className="mx-[5px]">
            {/* Tab Header */}
            <div className="flex border-b border-gray-300 sticky top-0 bg-white">
              <button
                className={`flex-1 text-center py-2 ${activeTab === "performance"
                    ? "border-b-2 border-black font-semibold"
                    : "text-gray-500"
                  }`}
                onClick={() => setActiveTab("performance")}
              >
                공연 정보
              </button>
              <button
                className={`flex-1 text-center py-2 ${activeTab === "sales"
                    ? "border-b-2 border-black font-semibold"
                    : "text-gray-500"
                  }`}
                onClick={() => setActiveTab("sales")}
              >
                판매 정보
              </button>
            </div>

            {/* Tab Content */}
            <div className="mt-6">
              {/** 공연정보 */}
              {activeTab === "performance" && (
                <div>
                  <div dangerouslySetInnerHTML={{ __html: event.content }} />
                  <br></br>
                  {/* 예매자 통계 영역 */}
                  <div className="stats-section">
                    <h3 className="text-lg font-medium mb-4">예매자 통계</h3>
                    <div className="stats-container flex gap-6">
                      <div className="age-stat  p-4 rounded-md w-1/2 shadow-md border">
                        <h4 className="text-md font-medium mb-2">연령 통계</h4>
                        {/* 연령 통계 차트 이미지 또는 컴포넌트 */}
                        <div className="flex chart-placeholder mb-[50px] h-[150px] rounded-md flex items-center justify-center">
                          <img src={manImg} className="w-[50px]" alt="" />
                          <div className="flex flex-col">
                            <p className="mt-[0px]">
                              남자 {genderStatisticData[0]}명
                            </p>
                            <p className="text-[15px] text-[#5B4DFF]">
                              {(genderStatisticData[0] /
                                (genderStatisticData[0] +
                                  genderStatisticData[1])) *
                                100}
                              %
                            </p>
                          </div>
                          <img src={womanImg} className="w-[50px]" alt="" />
                          <div className="flex flex-col">
                            <p className="mt-[0px]">
                              여자 {genderStatisticData[1]}명
                            </p>
                            <p className="text-[15px] text-[#5B4DFF]">
                              {(genderStatisticData[1] /
                                (genderStatisticData[0] +
                                  genderStatisticData[1])) *
                                100}
                              %
                            </p>
                          </div>
                        </div>
                      </div>
                      <div className="gender-stat p-4 rounded-md w-1/2 shadow-md border">
                        <h4 className="text-md font-medium mb-2">성별 통계</h4>
                        {/* 성별 통계 차트 이미지 또는 컴포넌트 */}
                        <div className="chart-placeholder h-[150px] rounded-md flex items-center justify-center">
                          <Bar data={chartData} options={chartOptions} />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              {/** 판매정보 */}
              {activeTab === "sales" && (
                <div>
                  <div className="">
                    {/** 시작  */}
                    <div className="  p-[0px]">
                      <div className="bg-white">
                        {/* 상품 관련 정보 테이블 */}
                        <section className="mb-6">
                          <h2 className="text-lg font-semibold text-gray-800 mb-4">
                            상품 관련 정보
                          </h2>
                          <div className="min-w-full">
                            <tbody className="border">
                              <div className="flex">
                                <p className="px-4 py-2 w-[115px] text-gray-500 font-semibold text-[14px]">
                                  주최/기획
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] ">
                                  {event.companyName}
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex">
                                <p className="px-4 w-[115px] py-2 text-gray-500 font-semibold text-[14px] ">
                                  고객문의
                                </p>
                                <p className="px-4 py-2 text-black text-[14px]  ">
                                  02-6467-2200
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex">
                                <p className="px-4 w-[115px] py-2 text-gray-500 font-semibold text-[14px] ">
                                  상영시간
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] ">
                                  {event.runningTime}분
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex">
                                <p className="px-4 py-2 w-[115px] text-gray-500 font-semibold text-[14px]  ">
                                  관람연령
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] ">
                                  {event.age.replace(/_/g, " ")}
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex">
                                <p className="px-4 py-2 w-[115px] text-gray-500 font-semibold text-[14px] ">
                                  예매수수료
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] ">
                                  장당 2,000원
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex">
                                <p className="px-4 py-2 w-[115px] text-gray-500 font-semibold text-[14px]">
                                  공연장
                                </p>
                                <p className="px-4 py-2 text-black text-[14px] ">
                                  {event.sido} {event.sigungu} {event.street}{" "}
                                  {event.stageName}
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex">
                                <p className="px-4 py-2 w-[115px] text-gray-500 font-semibold text-[14px] ">
                                  예매시작시간
                                </p>
                                <p className="px-4 py-2 text-black text-[14px]  ">
                                  {event.ticketingTime.replace("T", " ")}
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex">
                                <p className="px-4 py-2  text-gray-500 font-semibold text-[14px]  ">
                                  예매가능기간
                                </p>
                                <p className="px-4 py-2 text-black text-[14px]  ">
                                  &nbsp;&nbsp;{eventDates.at(-1)} ~{" "}
                                  {eventDates[0]} 오전 11시
                                </p>
                              </div>
                              <hr className="mx-[10px]" />
                              <div className="flex flex-col mt-[10px]">
                                <p className="font-semibold text-[14px] px-4 text-gray-500">
                                  예매취소조건
                                </p>
                                <div className="flex flex-col">
                                  <p className="p-[10px] text-[14px]">
                                    취소일자에 따라서 아래와 같이 취소수수료가
                                    부과됩니다. 예매 일 기준보다 관람일 기준이
                                    우선 적용됩니다. 단, 예매 당일 밤 12시 이전
                                    취소 시에는 취소수수료가 없으며, 예매
                                    수수료도 환불됩니다.(취소기한 내에 한함)
                                  </p>
                                  <div className="mt-[10px] text-[14px] flex ">
                                    <p className="    flex-1 text-left  pl-2">
                                      취소일
                                    </p>
                                    <p className="    flex-1 text-left  pl-2 ">
                                      취소수수료
                                    </p>
                                  </div>
                                  <hr />
                                  <div className="flex text-[14px] mt-[5px]">
                                    <p className=" flex-1 text-left  pl-2">
                                      예매 후 7일 이내
                                    </p>
                                    <p className=" flex-1 text-left  pl-2">
                                      없음
                                    </p>
                                  </div>
                                  <div className="flex text-[14px] mt-[5px]">
                                    <p className="flex-1 text-left  pl-2">
                                      예매 후 8일~관람일 10일전까지
                                    </p>
                                    <p className="flex-1 text-left  pl-2">
                                      장당 4,000원(티켓금액의 10%한도)
                                    </p>
                                  </div>
                                  <div className="flex text-[14px] mt-[5px]">
                                    <p className="flex-1 text-left  pl-2">
                                      관람일 9일전~7일전까지
                                    </p>
                                    <p className="flex-1 text-left  pl-2">
                                      티켓금액의 10%
                                    </p>
                                  </div>
                                  <div className="flex text-[14px] mt-[5px]">
                                    <p className="flex-1 text-left  pl-2">
                                      관람일 6일전~3일전까지
                                    </p>
                                    <p className="flex-1 text-left  pl-2">
                                      티켓금액의 20%
                                    </p>
                                  </div>
                                  <div className="flex text-[14px] mt-[5px]">
                                    <p className="flex-1 text-left  pl-2">
                                      관람일 2일전~1일전까지
                                    </p>
                                    <p className="flex-1 text-left  pl-2">
                                      티켓금액의 30%
                                    </p>
                                  </div>
                                </div>
                              </div>
                              <hr className="mx-[10px] mt-[10px]" />
                              <div className="flex flex-col mt-[5px]">
                                <p className="px-4 py-[10px]  text-gray-500 font-semibold text-[14px]  ">
                                  취소환불방법
                                </p>
                                <div className="flex flex-col border-b-[1px]">
                                  <p className="text-[14px] mt-[10px] pl-[15px]  flex items-center leading-none">
                                    - My티켓 &gt; 예매/취소내역에서 직접 취소
                                    또는 고객센터 (1544-1234)를 통해서 예매를
                                    취소할 수 있습니다.
                                  </p>
                                  <p className="text-[14px] my-[10px] pl-[15px] ">
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
                                <h2 className="text-[14px]">예매 유의사항</h2>
                                <p className="ml-[20px] text-[14px]">
                                  - 다른 이용자의 원활한 예매 및 취소에 지장을
                                  초래할 정도로 반복적인 행위를 지속하는 경우
                                  회원의 서비스 이용을 제한할 수 있습니다.
                                </p>
                                <p className="ml-[20px] text-[14px]">
                                  - 일부 상품의 판매 오픈 시 원활한 서비스
                                  제공을 위하여 특정 결제수단 이용이 제한될 수
                                  있습니다.
                                </p>
                              </div>
                              <br></br>
                              <div>
                                <h2 className="text-[14px] ">환불안내</h2>
                                <p className="ml-[20px]  font-medium text-[14px]">
                                  신용카드 결제의 경우{" "}
                                </p>
                                <p className="ml-[20px] text-[14px]">
                                  - 일반적으로 당사의 취소 처리가 완료되고 4~5일
                                  후 카드사의 취소가 확인됩니다. (체크카드 동일)
                                </p>
                                <p className="ml-[20px] text-[14px]">
                                  - 예매 취소 시점과 해당 카드사의 환불
                                  처리기준에 따라 취소금액의 환급방법과 환급일은
                                  다소 차이가 있을 수 있으며,
                                </p>
                                <p className="ml-[20px] text-[14px]">
                                  &nbsp;&nbsp;예매 취소시 기존에 결제하였던
                                  내역을 취소하며 최초 결제하셨던 동일카드로
                                  취소 시점에 따라 취소수수료와{" "}
                                </p>
                                <p className="ml-[20px] text-[14px]">
                                  &nbsp;&nbsp;배송료를 재승인 합니다.
                                </p>
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
        {/** 예매하기 버튼 */}
        <div>
          <button
            className="fixed bottom-0 left-0 w-full bg-[#8E43E7] text-white py-4 text-center"
            onClick={mobileGo}
          >
            예매하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default EventDetail;
