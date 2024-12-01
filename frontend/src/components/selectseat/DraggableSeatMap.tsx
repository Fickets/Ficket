import { useRef, useState, useEffect } from 'react';
import { SeatStatusResponse, SeatMapProps } from '../../types/selectseat';

const DraggableSeatMap = ({
  eventStageImg,
  reservationLimit,
  seatStatusResponse,
  onSeatSelect,
  selectedSeats,
  gradeColors,
}: SeatMapProps) => {
  const seatMapRef = useRef<HTMLDivElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [dragging, setDragging] = useState(false);
  const [offset, setOffset] = useState({ x: 0, y: 0 });
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [scale, setScale] = useState(1);

  const containerWidth = 630; // 컨테이너 크기
  const containerHeight = 460; // 컨테이너 크기
  const contentWidth = containerWidth; // 컨텐츠 크기
  const contentHeight = containerHeight; // 컨텐츠 크기

  useEffect(() => {
    // 초기 위치를 설정 (컨테이너의 중앙)
    if (seatMapRef.current) {
      setPosition({
        x: 0,
        y: 0,
      });
    }
  }, []);

  const handleMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!seatMapRef.current) return;
    setDragging(true);
    const rect = seatMapRef.current.getBoundingClientRect();
    setOffset({
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    });
  };

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!dragging || !containerRef.current) return;

    const newX = e.clientX - offset.x;
    const newY = e.clientY - offset.y;

    const dragSpeed = 0.4; // 드래그 속도 계수 (1보다 작으면 느려짐, 크면 빨라짐)

    // 컨테이너 바운더리 체크
    const maxX = containerWidth / 2;
    const minX = -(contentWidth * scale - containerWidth / 2);
    const maxY = containerHeight / 2;
    const minY = -(contentHeight * scale - containerHeight / 2);

    setPosition({
      x: Math.max(minX, Math.min(maxX, newX * dragSpeed)),
      y: Math.max(minY, Math.min(maxY, newY * dragSpeed)),
    });
  };

  const handleMouseUp = () => {
    setDragging(false);
  };

  const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    e.preventDefault();
    const zoomFactor = 0.1;

    setScale((prevScale) => {
      const newScale =
        e.deltaY > 0
          ? Math.max(0.5, prevScale - zoomFactor) // 축소
          : Math.min(2, prevScale + zoomFactor); // 확대

      // 확대/축소 시 바운더리 체크
      const maxX = containerWidth / 2;
      const minX = -(contentWidth * newScale - containerWidth / 2);
      const maxY = containerHeight / 2;
      const minY = -(contentHeight * newScale - containerHeight / 2);

      setPosition((prevPosition) => ({
        x: Math.max(minX, Math.min(maxX, prevPosition.x)),
        y: Math.max(minY, Math.min(maxY, prevPosition.y)),
      }));

      return newScale;
    });
  };

  const calculateOriginalSize = (seats: SeatStatusResponse[]) => {
    const maxX = Math.max(...seats.map((seat) => seat.seatX));
    const maxY = Math.max(...seats.map((seat) => seat.seatY));
    return { originalWidth: maxX, originalHeight: maxY };
  };

  const { originalWidth, originalHeight } =
    calculateOriginalSize(seatStatusResponse);

  const calculatePosition = (x: number, y: number) => {
    const xScaleFactor = 0.85;
    const yScaleFactor = 0.9;
    const scaledX = (x / originalWidth) * containerWidth * xScaleFactor;
    const scaledY = (y / originalHeight) * containerHeight * yScaleFactor;
    return { scaledX, scaledY };
  };

  const handleSeatClick = (seat: SeatStatusResponse) => {
    const isSelected = selectedSeats.some(
      (s) => s.seatMappingId === seat.seatMappingId
    );

    if (isSelected) {
      onSeatSelect(
        selectedSeats.filter((s) => s.seatMappingId !== seat.seatMappingId)
      );
    } else {
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

  return (
    <div
      ref={containerRef}
      className="relative w-[630px] h-[460px] bg-white border border-gray-300 overflow-hidden"
    >
      <div
        ref={seatMapRef}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        onWheel={handleWheel}
        style={{
          transform: `translate(${position.x}px, ${position.y}px) scale(${scale})`,
          transformOrigin: 'center center',
          cursor: dragging ? 'grabbing' : 'grab',
          position: 'absolute',
          width: `${contentWidth}px`,
          height: `${contentHeight}px`,
        }}
      >
        <div className="relative w-[630px] h-[460px] bg-gray-200">
          <img
            src={eventStageImg}
            alt="Stage"
            className="w-full h-full"
            onContextMenu={(e) => e.preventDefault()} // 우클릭 메뉴 방지
            onDragStart={(e) => e.preventDefault()}
          />
          {seatStatusResponse.map((seat) => {
            const { scaledX, scaledY } = calculatePosition(
              seat.seatX,
              seat.seatY
            );

            const isSelected = selectedSeats.some(
              (s) => s.seatMappingId === seat.seatMappingId
            );

            return (
              <div
                key={seat.seatMappingId}
                className={`absolute w-[8px] h-[8px] flex items-center justify-center cursor-pointer border ${
                  seat.status === 'LOCKED' || seat.status === 'PURCHASED'
                    ? 'cursor-not-allowed'
                    : isSelected
                      ? 'border-black'
                      : 'border-gray-300'
                }`}
                style={{
                  backgroundColor:
                    seat.status === 'LOCKED' || seat.status === 'PURCHASED'
                      ? '#FFF'
                      : isSelected
                        ? '#000'
                        : gradeColors[seat.seatGrade],
                  top: `${scaledY}px`,
                  left: `${scaledX}px`,
                  pointerEvents:
                    seat.status === 'LOCKED' || seat.status === 'PURCHASED'
                      ? 'none'
                      : 'auto',
                }}
                onClick={() => handleSeatClick(seat)}
              ></div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default DraggableSeatMap;
