import { apiClient } from '../../../api/client';
import { ApiResponse, LoginResponse, DemoAccountItem, UserInfo } from '../../../types/api';

const mockDemoAccounts: DemoAccountItem[] = [
  {
    username: 'staff.gate',
    passwordHint: '123456',
    fullName: 'Lê Bảo Vệ (Trạm Bãi A)',
    code: 'NV001',
    role: 'ROLE_STAFF',
    roleName: 'Nhân viên trạm',
    portalTarget: 'STAFF_PORTAL',
  },
  {
    username: 'sv.nguyen',
    passwordHint: '123456',
    fullName: 'Nguyễn Văn An (SV K23)',
    code: '2311770',
    role: 'ROLE_USER',
    roleName: 'Sinh viên',
    portalTarget: 'USER_PORTAL',
  },
  {
    username: 'admin',
    passwordHint: '123456',
    fullName: 'Nguyễn Quản Trị',
    code: 'AD001',
    role: 'ROLE_ADMIN',
    roleName: 'Quản trị viên',
    portalTarget: 'ADMIN_PORTAL',
  },
];

export const authApi = {
  login: async (username: string, password?: string): Promise<LoginResponse> => {
    try {
      const res = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', {
        username,
        password: password || '123456',
      });
      return res.data.data;
    } catch {
      await new Promise((r) => setTimeout(r, 250));
      const found = mockDemoAccounts.find((a) => a.username === username) || mockDemoAccounts[0];
      return {
        accessToken: `mock-jwt-token-for-${found.username}`,
        tokenType: 'Bearer',
        portalTarget: found.portalTarget,
        user: {
          id: found.username === 'staff.gate' ? 3 : found.username === 'admin' ? 2 : 1,
          username: found.username,
          fullName: found.fullName,
          code: found.code,
          email: `${found.username}@hcmut.edu.vn`,
          phone: '0901234567',
          department: found.username === 'staff.gate' ? 'Tổ Bảo vệ Bãi A' : 'Khoa KH&KT Máy tính',
          role: found.role,
          roleName: found.roleName,
          status: 'ACTIVE',
          hasActivePackage: true,
          activePackage: {
            packageCode: 'PKG_SEM_261',
            packageName: 'Gói Học kỳ HK261',
            startDate: '2026-09-01',
            endDate: '2026-12-31',
            status: 'ACTIVE',
          },
          registeredVehicleCount: 1,
          maxVehicleLimit: 2,
        },
      };
    }
  },

  getDemoAccounts: async (): Promise<DemoAccountItem[]> => {
    try {
      const res = await apiClient.get<ApiResponse<DemoAccountItem[]>>('/auth/demo-accounts');
      return res.data.data;
    } catch {
      return mockDemoAccounts;
    }
  },

  getMe: async (): Promise<UserInfo | null> => {
    try {
      const res = await apiClient.get<ApiResponse<UserInfo>>('/auth/me');
      return res.data.data;
    } catch {
      return null;
    }
  },
};
