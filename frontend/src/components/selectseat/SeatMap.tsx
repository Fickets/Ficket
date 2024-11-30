import {
  SeatStatusResponse,
  SeatStatus,
  SeatMapProps,
} from '../../types/selectseat';

const SeatMap = ({
  eventStageImg,
  reservationLimit,
  seatStatusResponse,
  onSeatSelect,
  selectedSeats,
  gradeColors,
}: SeatMapProps) => {
  const handleSeatClick = (seat: SeatStatusResponse) => {
    const isSelected = selectedSeats.some(
      (s) => s.seatMappingId === seat.seatMappingId
    );

    if (isSelected) {
      // 이미 선택된 좌석이면 선택 해제
      onSeatSelect(
        selectedSeats.filter((s) => s.seatMappingId !== seat.seatMappingId)
      );
    } else {
      // 새로운 좌석 선택 (예약 한도 초과 체크)
      if (selectedSeats.length < reservationLimit) {
        onSeatSelect([
          ...selectedSeats,
          {
            seatMappingId: seat.seatMappingId,
            grade: seat.seatGrade,
            row: seat.seatRow,
            col: seat.seatCol,
          },
        ]);
      } else {
        alert(`최대 ${reservationLimit}개의 좌석만 선택할 수 있습니다.`);
      }
    }
  };

  // 원본 좌표 크기를 계산
  const calculateOriginalSize = (seats: SeatStatusResponse[]) => {
    const maxX = Math.max(...seats.map((seat) => seat.seatX));
    const maxY = Math.max(...seats.map((seat) => seat.seatY));
    return { originalWidth: maxX, originalHeight: maxY };
  };

  const { originalWidth, originalHeight } =
    calculateOriginalSize(seatStatusResponse);

  const containerWidth = 630; // 실제 컨테이너의 너비
  const containerHeight = 460; // 실제 컨테이너의 높이
  const xOffset = 56; // X 좌표를 오른쪽으로 이동할 오프셋 (단위: px)

  // 좌표 비율 계산 함수 (X 줄이고, Y 늘리기)
  const calculatePosition = (x: number, y: number) => {
    const xScaleFactor = 0.7; // X 축을 70% 축소
    const yScaleFactor = 0.9; // Y 축을 90% 축소
    const scaledX =
      (x / originalWidth) * containerWidth * xScaleFactor + xOffset; // X 좌표에 오프셋 추가
    const scaledY = (y / originalHeight) * containerHeight * yScaleFactor;
    return { scaledX, scaledY };
  };

  return (
    <div className="relative w-[630px] h-[460px] bg-white border border-gray-300">
      {/* 무대 이미지 */}
      <div className="absolute top-0 left-0 w-full h-full bg-gray-200 flex items-center justify-center">
        <img
          src={eventStageImg}
          alt="Stage"
          className="h-full object-contain"
        />
        {seatStatusResponse.map((seat) => {
          const { scaledX, scaledY } = calculatePosition(
            seat.seatX,
            seat.seatY
          );

          return (
            <div
              key={seat.seatMappingId}
              className={`absolute w-[8px] h-[8px] flex items-center justify-center cursor-pointer border ${
                selectedSeats.some(
                  (s) => s.seatMappingId === seat.seatMappingId
                )
                  ? 'border-blue-500'
                  : 'border-gray-300'
              }`}
              style={{
                backgroundColor: gradeColors[seat.seatGrade] || '#ccc',
                top: `${scaledY}px`, // Y 좌표 (비율 적용)
                left: `${scaledX}px`, // X 좌표 (비율 적용 + 오프셋)
                opacity:
                  seat.status === SeatStatus.LOCKED ||
                  seat.status === SeatStatus.PURCHASED
                    ? 0.5
                    : 1,
                pointerEvents:
                  seat.status === SeatStatus.LOCKED ||
                  seat.status === SeatStatus.PURCHASED
                    ? 'none'
                    : 'auto',
              }}
              onClick={() => handleSeatClick(seat)}
            ></div>
          );
        })}
      </div>
    </div>
  );
};

export default SeatMap;
