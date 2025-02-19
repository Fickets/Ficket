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
  const offsetRef = useRef({ x: 0, y: 0 });

  const [dragging, setDragging] = useState(false);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [scale, setScale] = useState(1);
  const [isMobile, setIsMobile] = useState(false);

  const containerWidth = 630;
  const containerHeight = 460;
  const contentWidth = containerWidth;
  const contentHeight = containerHeight;

  useEffect(() => {
    const updateDraggableStatus = () => {
      setIsMobile(window.innerWidth <= 768);
    };

    updateDraggableStatus();
    window.addEventListener("resize", updateDraggableStatus);

    return () => {
      window.removeEventListener("resize", updateDraggableStatus);
    };
  }, []);

  const handleMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!isMobile || !seatMapRef.current) return;

    setDragging(true);
    const rect = seatMapRef.current.getBoundingClientRect();
    offsetRef.current = { x: e.clientX - rect.left, y: e.clientY - rect.top };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);
  };

  const handleMouseMove = (e: MouseEvent) => {
    if (!isMobile || !dragging || !containerRef.current) return;

    const newX = e.clientX - offsetRef.current.x;
    const newY = e.clientY - offsetRef.current.y;
    const dragSpeed = 1;

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
    window.removeEventListener("mousemove", handleMouseMove);
    window.removeEventListener("mouseup", handleMouseUp);
  };

  const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    if (!isMobile) return;
    e.preventDefault();
    const zoomFactor = 0.1;

    setScale((prevScale) => {
      const newScale =
        e.deltaY > 0
          ? Math.max(0.5, prevScale - zoomFactor)
          : Math.min(2, prevScale + zoomFactor);

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
    const xScaleFactor = 0.9;
    const yScaleFactor = 0.9;
    return {
      scaledX: (x / originalWidth) * containerWidth * xScaleFactor,
      scaledY: (y / originalHeight) * containerHeight * yScaleFactor,
    };
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
      className="relative w-[400px] h-[460px] sm:w-[630px] sm:h-[460px] bg-white border border-gray-300 overflow-hidden"
    >
      <div
        ref={seatMapRef}
        onMouseDown={isMobile ? handleMouseDown : undefined}
        onWheel={isMobile ? handleWheel : undefined}
        style={{
          transform: `translate(${position.x}px, ${position.y}px) scale(${scale})`,
          transformOrigin: "center center",
          cursor: isMobile ? (dragging ? "grabbing" : "grab") : "default",
          position: "absolute",
          width: `${contentWidth}px`,
          height: `${contentHeight}px`,
        }}
      >
        <div className="relative w-[630px] h-[460px] bg-gray-200">
          <img src={eventStageImg} alt="Stage" className="w-full h-full" />

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
                  isSelected ? "border-black" : "border-gray-300"
                }`}
                style={{
                  backgroundColor: gradeColors[seat.seatGrade],
                  top: `${scaledY}px`,
                  left: `${scaledX}px`,
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
