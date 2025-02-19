import { useRef, useState, useEffect } from "react";
import { SeatStatusResponse, SeatMapProps } from "../../types/selectseat";

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
  const [isMobile, setIsMobile] = useState(false);

  const containerWidth = 630;
  const containerHeight = 460;
  const contentWidth = containerWidth;
  const contentHeight = containerHeight;

  useEffect(() => {
    if (seatMapRef.current) {
      setPosition({ x: 0, y: 0 });
    }

    const updateDeviceType = () => {
      setIsMobile(window.innerWidth <= 768);
    };

    updateDeviceType();
    window.addEventListener("resize", updateDeviceType);

    return () => {
      window.removeEventListener("resize", updateDeviceType);
    };
  }, []);

  const handleTouchStart = (e: React.TouchEvent<HTMLDivElement>) => {
    if (!isMobile || !seatMapRef.current) return;
    setDragging(true);
    const touch = e.touches[0];
    const rect = seatMapRef.current.getBoundingClientRect();
    setOffset({
      x: touch.clientX - rect.left,
      y: touch.clientY - rect.top,
    });
  };

  const handleTouchMove = (e: React.TouchEvent<HTMLDivElement>) => {
    if (!isMobile || !dragging || !containerRef.current) return;

    const touch = e.touches[0];
    const newX = touch.clientX - offset.x;
    const newY = touch.clientY - offset.y;
    const dragSpeed = 0.4;

    const maxX = containerWidth / 2;
    const minX = -(contentWidth - containerWidth / 2);
    const maxY = containerHeight / 2;
    const minY = -(contentHeight - containerHeight / 2);

    setPosition({
      x: Math.max(minX, Math.min(maxX, newX * dragSpeed)),
      y: Math.max(minY, Math.min(maxY, newY * dragSpeed)),
    });
  };

  const handleTouchEnd = () => {
    if (!isMobile) return;
    setDragging(false);
  };

  const calculateOriginalSize = (seats: SeatStatusResponse[]) => {
    const maxX = Math.max(...seats.map((seat) => seat.seatX));
    const maxY = Math.max(...seats.map((seat) => seat.seatY));
    return { originalWidth: maxX, originalHeight: maxY };
  };

  const { originalWidth, originalHeight } =
    calculateOriginalSize(seatStatusResponse);

  const calculatePosition = (x: number, y: number) => {
    const xScaleFactor = 0.9;
    const yScaleFactor = 0.9;
    const scaledX = (x / originalWidth) * containerWidth * xScaleFactor;
    const scaledY = (y / originalHeight) * containerHeight * yScaleFactor;
    return { scaledX, scaledY };
  };

  const handleSeatClick = (seat: SeatStatusResponse) => {
    const isSelected = selectedSeats.some(
      (s) => s.seatMappingId === seat.seatMappingId,
    );

    if (isSelected) {
      onSeatSelect(
        selectedSeats.filter((s) => s.seatMappingId !== seat.seatMappingId),
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
            price: seat.seatPrice,
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
      className="relative w-full w-max-[400px] sm:w-[630px] h-[460px] bg-white border border-gray-300 overflow-hidden"
    >
      <div
        ref={seatMapRef}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        style={{
          transform: `translate(${position.x}px, ${position.y}px)`,
          cursor: isMobile ? (dragging ? "grabbing" : "grab") : "default",
          position: "absolute",
          width: `${contentWidth}px`,
          height: `${contentHeight}px`,
        }}
      >
        <div className="relative w-[630px] h-[460px] bg-gray-200">
          <img
            src={eventStageImg}
            alt="Stage"
            className="w-full h-full"
            onContextMenu={(e) => e.preventDefault()}
            onDragStart={(e) => e.preventDefault()}
          />
          {seatStatusResponse.map((seat) => {
            const { scaledX, scaledY } = calculatePosition(
              seat.seatX,
              seat.seatY,
            );

            const isSelected = selectedSeats.some(
              (s) => s.seatMappingId === seat.seatMappingId,
            );

            return (
              <div
                key={seat.seatMappingId}
                className={`absolute w-[8px] h-[8px] flex items-center justify-center cursor-pointer border ${
                  seat.status === "LOCKED" || seat.status === "PURCHASED"
                    ? "cursor-not-allowed"
                    : isSelected
                      ? "border-black"
                      : "border-gray-300"
                }`}
                style={{
                  backgroundColor:
                    seat.status === "LOCKED" || seat.status === "PURCHASED"
                      ? "#FFF"
                      : isSelected
                        ? "#000"
                        : gradeColors[seat.seatGrade],
                  top: `${scaledY}px`,
                  left: `${scaledX}px`,
                  pointerEvents:
                    seat.status === "LOCKED" || seat.status === "PURCHASED"
                      ? "none"
                      : "auto",
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
