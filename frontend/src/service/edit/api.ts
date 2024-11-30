import axios from 'axios';

// Fetch Event Detail
export const fetchEventDetail = async (eventId: number) => {
  const response = await axios.get(
    `http://localhost:9000/api/v1/events/${eventId}/detail`
  );
  return response.data;
};

export const updateEvent = async (eventId: number, formData: FormData) => {
  const response = await axios.patch(
    `http://localhost:9000/api/v1/events/${eventId}`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  );
  for (const [key, value] of formData.entries()) {
    if (value instanceof Blob) {
      console.log(key, value, `Blob size: ${value.size}`);
    }
  }
  return response.data;
};
