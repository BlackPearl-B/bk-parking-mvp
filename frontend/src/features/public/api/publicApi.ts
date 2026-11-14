import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export interface ParkingLotPublicInfo {
  id: number;
  code: string;
  name: string;
  location: string;
  capacity: number;
  occupiedCount: number;
  availableCount: number;
  occupancyRate: number;
  status: 'ACTIVE' | 'MAINTENANCE' | 'FULL';
  updatedAt: string;
  isCarAllowed?: boolean;
}

const mockPublicLots: ParkingLotPublicInfo[] = [
  {
    id: 1,
    code: 'LOT_A',
    name: 'Bãi A – Cổng Lý Thường Kiệt',
    location: 'Cơ sở 1 (268 Lý Thường Kiệt)',
    capacity: 1800,
    occupiedCount: 1692,
    availableCount: 108,
    occupancyRate: 94.0,
    status: 'ACTIVE',
    updatedAt: '07:45:12',
    isCarAllowed: true,
  },
  {
    id: 2,
    code: 'LOT_B',
    name: 'Bãi B – Nhà A1–A5',
    location: 'Cơ sở 1 (Khu A)',
    capacity: 1200,
    occupiedCount: 1056,
    availableCount: 144,
    occupancyRate: 88.0,
    status: 'ACTIVE',
    updatedAt: '07:44:50',
    isCarAllowed: false,
  },
  {
    id: 3,
    code: 'LOT_C',
    name: 'Bãi C – Nhà B1–B4',
    location: 'Cơ sở 1 (Khu B)',
    capacity: 900,
    occupiedCount: 648,
    availableCount: 252,
    occupancyRate: 72.0,
    status: 'ACTIVE',
    updatedAt: '07:44:10',
    isCarAllowed: true,
  },
  {
    id: 4,
    code: 'LOT_D',
    name: 'Bãi D – Ký túc xá',
    location: 'Khu nội trú KTX',
    capacity: 600,
    occupiedCount: 330,
    availableCount: 270,
    occupancyRate: 55.0,
    status: 'ACTIVE',
    updatedAt: '07:43:20',
    isCarAllowed: false,
  },
  {
    id: 5,
    code: 'LOT_E',
    name: 'Bãi E – Hành chính & Giảng viên',
    location: 'Cơ sở 1 (Nhà A4)',
    capacity: 300,
    occupiedCount: 273,
    availableCount: 27,
    occupancyRate: 91.0,
    status: 'ACTIVE',
    updatedAt: '07:45:00',
    isCarAllowed: true,
  },
];

export const publicApi = {
  getParkingLots: async (): Promise<ParkingLotPublicInfo[]> => {
    try {
      const res = await axios.get(`${BASE_URL}/public/parking-lots`, { timeout: 1500 });
      return res.data.data;
    } catch {
      return mockPublicLots;
    }
  },
};
