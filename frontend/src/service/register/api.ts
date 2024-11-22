import axios from 'axios';
import { SeatInfo } from '../../types/register';

// Fetch companies
export const fetchCompanies = async () => {
  const response = await axios.get(
    'http://localhost:9000/api/v1/admins/companies'
  );
  console.log(response.data.companyList[0]);
  return response.data.companyList;
};

// Fetch stages
export const fetchStages = async () => {
  const response = await axios.get(
    'http://localhost:9000/api/v1/events/stages'
  );
  return response.data.eventStageDtoList;
};

// Fetch Stage Seats
export const fetchStageSeats = async (stageId: number): Promise<SeatInfo[]> => {
  const response = await axios.get(
    `http://localhost:9000/api/v1/events/stage/${stageId}`
  );
  return response.data.stageSeats;
};

// Register event
export const registerEvent = async (formData: FormData) => {
  const response = await axios.post(
    'http://localhost:9000/api/v1/events',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  );
  return response.data;
};
