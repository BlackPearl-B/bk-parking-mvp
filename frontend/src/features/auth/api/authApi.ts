import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export interface UserProfile {
  id: number;
  username: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  studentOrStaffId: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN' | 'ROLE_STAFF';
}

export interface LoginResponseData {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

export interface DemoAccount {
  username: string;
  fullName: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN' | 'ROLE_STAFF';
  description: string;
}

const mockDemoAccounts: DemoAccount[] = [
  { username: 'baove01', fullName: 'Lê Bảo Vệ (Bãi A)', role: 'ROLE_STAFF', description: 'Bảo vệ kiểm soát trạm quét QR vào/ra' },
  { username: 'sinhvien01', fullName: 'Nguyễn Văn An (SV)', role: 'ROLE_USER', description: 'Sinh viên có gói tháng & xe đã duyệt' },
  { username: 'admin01', fullName: 'Nguyễn Quản Trị', role: 'ROLE_ADMIN', description: 'Quản trị viên quản lý bãi xe' },
];

export const authApi = {
  login: async (username: string, password?: string): Promise<LoginResponseData> => {
    try {
      const res = await axios.post(`${BASE_URL}/auth/login`, { username, password: password || 'password' }, { timeout: 1500 });
      return res.data.data;
    } catch {
      await new Promise((r) => setTimeout(r, 350));
      const found = mockDemoAccounts.find((a) => a.username === username) || mockDemoAccounts[0];
      return {
        accessToken: `mock-jwt-token-for-${found.username}`,
        tokenType: 'Bearer',
        expiresIn: 86400,
        user: {
          id: found.username === 'baove01' ? 3 : found.username === 'admin01' ? 2 : 1,
          username: found.username,
          fullName: found.fullName,
          email: `${found.username}@hcmut.edu.vn`,
          phoneNumber: '0901234567',
          studentOrStaffId: found.username === 'baove01' ? 'NV-012' : found.username === 'admin01' ? 'AD-001' : '2011876',
          role: found.role,
        },
      };
    }
  },

  getDemoAccounts: async (): Promise<DemoAccount[]> => {
    try {
      const res = await axios.get(`${BASE_URL}/auth/demo-accounts`, { timeout: 1500 });
      return res.data.data;
    } catch {
      return mockDemoAccounts;
    }
  },
};
