import { EventOpenListProps } from '../../types/eventScheduledOpen.ts';

const MobileEventOpenList = ({
  events,
  searchParams,
  onSearchParamsChange,
}: EventOpenListProps) => {
  return (
    <div className="grid grid-cols-1 gap-4">
      {events.content.map((event) => (
        <div key={event.eventId} className="border p-4 rounded shadow">
          <h4>{event.title}</h4>
          <p>{event.ticketStartTime}</p>
        </div>
      ))}
    </div>
  );
};

export default MobileEventOpenList;
