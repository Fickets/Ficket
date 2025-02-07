import { useStore } from 'zustand';
import { eventDetailStore } from '../../stores/EventStore.tsx';
import { useEffect, useState } from 'react';
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Title,
  Tooltip,
  LineElement,
  PointElement,
  ChartOptions,
} from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { genderStatistic } from '../../service/event/eventApi.ts';
import manImg from '../../assets/detail/man.png';
import womanImg from '../../assets/detail/woman.png';
import { Bar, Line } from 'react-chartjs-2';
import {
  DailyRevenueResponse,
  DayCountMap,
} from '../../types/adminEventDetail.ts';
import {
  fetchDailyRevenue,
  fetchDayCount,
} from '../../service/adminEventDetail/api.ts';

const EventStatistics = ({ eventId }: { eventId: string }) => {
  const event = useStore(eventDetailStore);
  const [showPrice, setShowPrice] = useState(false);

  const togglePrice = () => {
    setShowPrice((prev) => !prev);
  };

  const [genderStatisticData, setGenderStatisticData] = useState<number[]>([
    0, 0, 0, 0, 0, 0, 0,
  ]);

  // Chart.js 모듈 등록
  ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ChartDataLabels,
    LineElement,
    PointElement
  );

  const chartData = {
    labels: ['10대', '20대', '30대', '40대', '50대'], // x축에 표시될 레이블
    datasets: [
      {
        label: 'Sales', // 범례에 표시될 라벨
        data: genderStatisticData.slice(2), // 데이터의 슬라이스
        backgroundColor: '#5B4DFF', // 바 색상
        borderColor: '#5B4DFF', // 테두리 색상
        borderWidth: 1, // 테두리 두께
      },
    ],
  };

  const chartOptions: ChartOptions<'bar'> = {
    responsive: true,
    plugins: {
      legend: {
        display: false, // 범례 활성화
        position: 'top', // 범례 위치
        labels: {
          font: {
            size: 14, // 범례 텍스트 크기
            family: 'Arial', // 텍스트 폰트
            weight: 'bold', // 텍스트 두께
          },
          color: '#5B4DFF', // 범례 텍스트 색상
          padding: 10, // 범례 항목 간격
        },
      },
      tooltip: {
        enabled: false, // 툴팁 비활성화
      },
      datalabels: {
        color: '#5B4DFF', // 데이터 레이블 색상
        anchor: 'end', // 데이터 레이블 위치
        align: 'end', // 데이터 레이블 정렬
        formatter: (value: any) => `${value}%`, // 데이터 레이블 형식
        font: {
          size: 12, // 데이터 레이블 글씨 크기
        },
      },
    },
    scales: {
      x: {
        display: true, // x축을 표시
        labels: ['10대', '20대', '30대', '40대', '50대'], // x축의 레이블 설정
      },
      y: {
        display: false, // y축 숨김
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

  const genderStatisticGet = async () => {
    await genderStatistic(
      Number(eventId),
      (response) => {
        console.log(response); // 데이터 확인
        const res = response.data;

        if (!Array.isArray(res)) {
          console.error('API 응답 데이터가 배열이 아닙니다.', res);
          return;
        }

        const statisticData = res.slice(0, 2);
        const sum = statisticData.reduce(
          (acc, currentValue) => acc + currentValue,
          0
        );

        res.slice(2).forEach((value) => {
          const ageStatistic = (value / sum) * 100;
          statisticData.push(ageStatistic);
        });

        setGenderStatisticData(statisticData);
        console.log(statisticData);
      },
      (error: any) => {
        console.log(error.message);
      }
    );
  };

  const [revenueData, setRevenueData] = useState<DailyRevenueResponse[]>([]);

  const revenueStatisticGet = async () => {
    const response = await fetchDailyRevenue(eventId);
    setRevenueData(response);
  };

  const [dayCountData, setDayCountData] = useState<DayCountMap>({
    dayCountMap: {
      Monday: 0,
      Tuesday: 0,
      Wednesday: 0,
      Thursday: 0,
      Friday: 0,
      Saturday: 0,
      Sunday: 0,
    },
  });

  const dayCountStatisticGet = async () => {
    const response = await fetchDayCount(eventId);
    setDayCountData(response);
  };

  const DayCountChart = () => {
    const chartData = {
      labels: ['월', '화', '수', '목', '금', '토', '일'], // 요일 이름
      datasets: [
        {
          label: '요일별 예매 수',
          data: Object.values(dayCountData.dayCountMap), // 각 요일의 데이터 값
          borderColor: '#8B4513', // 라인 색상
          backgroundColor: '#8B4513', // 포인트 색상
          borderWidth: 2, // 라인 두께
          pointRadius: 5, // 포인트 크기
          pointBackgroundColor: '#FFFFFF', // 포인트 내부 색상
          pointBorderColor: '#8B4513', // 포인트 테두리 색상
        },
      ],
    };

    const chartOptions = {
      responsive: true,
      plugins: {
        legend: {
          display: false,
        },
        tooltip: {
          enabled: true,
        },
      },
      scales: {
        x: {
          title: {
            display: true,
            text: '요일',
          },
          ticks: {
            font: {
              size: 14,
            },
          },
        },
        y: {
          title: {
            display: true,
            text: '예매 수',
          },
          beginAtZero: true,
          ticks: {
            stepSize: 250, // y축 간격
          },
        },
      },
    };

    return <Line data={chartData} options={chartOptions} />;
  };

  useEffect(() => {
    genderStatisticGet();
    revenueStatisticGet();
    dayCountStatisticGet();
  }, [eventId]);

  return (
    <>
      <div className="mt-5 mx-auto max-w-[920px]">
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
                  (Object.keys(event.scheduleMap)[0] || '').replace(/-/g, '.')}
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
                {event.genre.map((element, index) => (
                  <p key={index}>{element.replace(/_/g, '/')}&nbsp;&nbsp;</p>
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
      </div>
      <hr className="w-[920px] mx-auto my-8 border-t border-gray-300" />
      {/* 예매자 통계 영역 */}
      <div className="stats-section mt-10 mx-auto max-w-[920px]">
        <h3 className="text-[25px] font-medium mb-6 text-left">
          📈 예매자 통계
        </h3>
        <div className="grid grid-cols-2 gap-6 p-6 rounded-lg shadow-lg border">
          {/* 성별 예매율 */}
          <div className="p-6 shadow-md rounded-md border flex flex-col items-center">
            <h4 className="text-lg font-medium mb-4">성별 예매율</h4>
            <div className="flex items-center justify-center gap-6">
              <div className="flex flex-col items-center">
                <p className="mt-[0px]">남자 {genderStatisticData[0]}명</p>
                <img src={manImg} className="w-[100px]" alt="남성" />
                <p className="text-md mt-2">
                  {(genderStatisticData[0] /
                    (genderStatisticData[0] + genderStatisticData[1])) *
                    100}
                  %
                </p>
              </div>
              <div className="flex flex-col items-center">
                <p className="mt-[0px]">여자 {genderStatisticData[1]}명</p>
                <img src={womanImg} className="w-[100px]" alt="여성" />
                <p className="text-md mt-2">
                  {(genderStatisticData[1] /
                    (genderStatisticData[0] + genderStatisticData[1])) *
                    100}
                  %
                </p>
              </div>
            </div>
          </div>

          {/* 연령별 예매율 */}
          <div className="p-6 shadow-md rounded-md border flex flex-col items-center">
            <h4 className="text-lg font-medium mb-4">연령별 예매율</h4>
            <div className="flex justify-center items-center h-40 w-full mt-5">
              <Bar data={chartData} options={chartOptions} />
            </div>
          </div>

          {/* 수익 */}
          <div className="p-6 shadow-md rounded-md border flex flex-col items-center">
            <h4 className="text-lg font-medium mb-4">수익</h4>
            <div className="w-full overflow-y-auto max-h-[150px]">
              <table className="w-full text-sm text-center">
                <thead>
                  <tr className="border-b">
                    <th className="py-2">날짜</th>
                    <th className="py-2">수익</th>
                  </tr>
                </thead>
                <tbody>
                  {revenueData.map((entry, index) => (
                    <tr
                      key={index}
                      className={`border-b ${index === revenueData.length - 1 ? '' : 'border-gray-200'}`}
                    >
                      <td className="py-2">
                        {new Date(entry.date).toLocaleDateString('ko-KR')}
                      </td>
                      <td className="py-2">
                        {Intl.NumberFormat().format(entry.revenue)} 원
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* 요일별 예매 수 */}
          <div className="p-6 shadow-md rounded-md border flex flex-col items-center">
            <h4 className="text-lg font-medium mb-4">요일별 예매 수</h4>
            <div className="h-40 w-full">
              <DayCountChart />
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default EventStatistics;
