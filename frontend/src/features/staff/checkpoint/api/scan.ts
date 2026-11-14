import { ScanResult, Direction } from '../types';

export interface VerifyOptions {
  rawCode: string;
  isOffline?: boolean;
}

// Cơ sở dữ liệu xe đăng ký thực tế tại cổng
const registeredVehicles = [
  {
    plate: '59X3-456.78',
    ownerId: '2011876',
    ownerName: 'Trần Quốc Bảo',
    ownerRole: 'Sinh viên (SV)',
    vehicleInfo: 'Xe máy · Honda Vision 110 · Đen',
    allowedLots: 'Bãi A, B',
  },
  {
    plate: '59P1-123.45',
    ownerId: '2112345',
    ownerName: 'Trần Minh Anh',
    ownerRole: 'Sinh viên (SV)',
    vehicleInfo: 'Xe máy · Honda AirBlade · Đỏ',
    allowedLots: 'Bãi A, B',
  },
  {
    plate: '51A-123.45',
    ownerId: 'GV-0421',
    ownerName: 'Nguyễn Thị Hoa',
    ownerRole: 'Giảng viên (GV)',
    vehicleInfo: 'Ô tô · Toyota Vios · Trắng',
    allowedLots: 'Bãi A, E',
  },
  {
    plate: '59C1-246.80',
    ownerId: 'NV-0278',
    ownerName: 'Hoàng Gia Phúc',
    ownerRole: 'Nhân viên (NV)',
    vehicleInfo: 'Xe máy · Honda AirBlade · Đỏ',
    allowedLots: 'Bãi A, B',
    isLocked: true,
  },
];

// Lưu trạng thái xe đang trong bãi để tự động nhận diện hướng VÀO/RA
const vehiclesInLot = new Set<string>(['59X3-456.78', '51A-123.45']);

export const scanApi = {
  verifyPass: async ({ rawCode, isOffline = false }: VerifyOptions): Promise<ScanResult> => {
    await new Promise((r) => setTimeout(r, 450));
    const clean = rawCode.trim().toUpperCase().replace(/[-.\s]/g, '');

    // Tìm trong danh mục xe
    const found = registeredVehicles.find(
      (v) =>
        v.plate.toUpperCase().replace(/[-.\s]/g, '').includes(clean) ||
        v.ownerId.toUpperCase().includes(clean) ||
        clean.includes(v.ownerId.toUpperCase())
    );

    if (isOffline) {
      const plate = found ? found.plate : rawCode.toUpperCase();
      return {
        state: 'manual',
        direction: vehiclesInLot.has(plate) ? 'out' : 'in',
        plateNumber: plate,
        cameraPlate: `${plate} (Chưa đối chiếu)`,
        cameraConfidence: 0,
        isPlateMatched: false,
        vehicleInfo: found?.vehicleInfo || 'Xe máy · Cần đối chiếu ảnh',
        ownerName: found?.ownerName || 'Hồ sơ ngoại tuyến',
        ownerRole: found?.ownerRole || 'Sinh viên (SV)',
        ownerId: found?.ownerId || rawCode,
        allowedLots: 'Bãi A',
        isOffline: true,
      };
    }

    if (!found) {
      return {
        state: 'invalid',
        direction: 'in',
        plateNumber: rawCode.toUpperCase(),
        cameraPlate: rawCode.toUpperCase(),
        cameraConfidence: 95,
        isPlateMatched: true,
        vehicleInfo: 'Phương tiện chưa đăng ký trên hệ thống',
        ownerName: 'Không xác định',
        ownerRole: 'Khách',
        ownerId: 'N/A',
        allowedLots: 'Không',
        invalidReason: 'Xe chưa đăng ký trong hệ thống bãi xe ĐHBK.',
      };
    }

    if (found.isLocked) {
      return {
        state: 'invalid',
        direction: 'in',
        plateNumber: found.plate,
        cameraPlate: found.plate,
        cameraConfidence: 99,
        isPlateMatched: true,
        vehicleInfo: found.vehicleInfo,
        ownerName: found.ownerName,
        ownerRole: found.ownerRole,
        ownerId: found.ownerId,
        allowedLots: found.allowedLots,
        invalidReason: 'Gói đỗ xe đã hết hạn hoặc tài khoản bị khóa.',
      };
    }

    // Tự động nhận diện hướng VÀO hoặc RA
    const currentlyIn = vehiclesInLot.has(found.plate);
    const direction: Direction = currentlyIn ? 'out' : 'in';

    // Cập nhật trạng thái bãi
    if (direction === 'in') {
      vehiclesInLot.add(found.plate);
    } else {
      vehiclesInLot.delete(found.plate);
    }

    return {
      state: 'valid',
      direction,
      plateNumber: found.plate,
      cameraPlate: found.plate,
      cameraConfidence: 98,
      isPlateMatched: true,
      vehicleInfo: found.vehicleInfo,
      ownerName: found.ownerName,
      ownerRole: found.ownerRole,
      ownerId: found.ownerId,
      allowedLots: found.allowedLots,
      checkInTime: direction === 'out' ? 'Hôm nay 07:15:20' : undefined,
      parkingDuration: direction === 'out' ? '1 giờ 45 phút' : undefined,
    };
  },

  confirmAction: async (_action: 'allow' | 'reject' | 'exception', _plate: string): Promise<boolean> => {
    await new Promise((r) => setTimeout(r, 200));
    return true;
  },
};
