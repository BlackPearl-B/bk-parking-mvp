package vn.edu.hcmut.bkparking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmut.bkparking.common.exception.BusinessException;
import vn.edu.hcmut.bkparking.common.exception.ErrorCode;
import vn.edu.hcmut.bkparking.dto.AdminVehicleResponseDto;
import vn.edu.hcmut.bkparking.dto.CreateVehicleRequest;
import vn.edu.hcmut.bkparking.dto.RejectVehicleRequest;
import vn.edu.hcmut.bkparking.dto.UserVehicleResponseDto;
import vn.edu.hcmut.bkparking.entity.ParkingSession;
import vn.edu.hcmut.bkparking.entity.User;
import vn.edu.hcmut.bkparking.entity.Vehicle;
import vn.edu.hcmut.bkparking.repository.ParkingSessionRepository;
import vn.edu.hcmut.bkparking.repository.UserRepository;
import vn.edu.hcmut.bkparking.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    @Value("${app.vehicle.max-per-user:2}")
    private int maxVehiclesPerUser;

    public VehicleService(
            VehicleRepository vehicleRepository,
            UserRepository userRepository,
            ParkingSessionRepository parkingSessionRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.parkingSessionRepository = parkingSessionRepository;
    }

    // =========================================================================
    // 1. PHÂN HỆ USER: CHỈ TRẢ VỀ UserVehicleResponseDto (BẢO MẬT TUYỆT ĐỐI)
    // =========================================================================

    /**
     * [User] Lấy danh sách xe của người dùng hiện tại
     */
    @Transactional(readOnly = true)
    public List<UserVehicleResponseDto> getMyVehicles(Long userId) {
        return vehicleRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    /**
     * [User] Đăng ký phương tiện mới (trạng thái ban đầu PENDING)
     */
    public UserVehicleResponseDto registerVehicle(Long userId, CreateVehicleRequest request) {
        String normalizedPlate = request.getLicensePlate().trim().toUpperCase();

        // 1. Kiểm tra trùng biển số xe
        if (vehicleRepository.findByLicensePlateIgnoreCase(normalizedPlate).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_LICENSE_PLATE,
                    "Biển số xe " + normalizedPlate + " đã tồn tại trong hệ thống");
        }

        // 2. Kiểm tra giới hạn số lượng phương tiện
        long currentCount = vehicleRepository.countByUserIdAndStatusIn(userId, List.of("PENDING", "APPROVED"));
        if (currentCount >= maxVehiclesPerUser) {
            throw new BusinessException(ErrorCode.VEHICLE_LIMIT_EXCEEDED,
                    "Mỗi tài khoản chỉ được đăng ký tối đa " + maxVehiclesPerUser + " phương tiện hợp lệ");
        }

        // 3. Tìm thông tin chủ xe
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy người dùng"));

        // 4. Tạo entity Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setUser(owner);
        vehicle.setLicensePlate(normalizedPlate);
        vehicle.setVehicleType(request.getVehicleType().trim().toUpperCase());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());
        vehicle.setRegistrationDocUrl(request.getRegistrationDocUrl());
        vehicle.setStatus("PENDING");
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToUserDto(saved);
    }

    // =========================================================================
    // 2. PHÂN HỆ ADMIN: TRẢ VỀ AdminVehicleResponseDto (ĐẦY ĐỦ QUYỀN HẠN & AUDIT)
    // =========================================================================

    /**
     * [Admin] Lấy danh sách phương tiện kèm thông tin chủ xe & người duyệt
     */
    @Transactional(readOnly = true)
    public List<AdminVehicleResponseDto> getAdminVehicles(String status, String keyword) {
        List<Vehicle> vehicles;
        if (keyword != null && !keyword.isBlank()) {
            vehicles = vehicleRepository.searchVehicles(keyword.trim());
            if (status != null && !status.isBlank()) {
                String st = status.trim().toUpperCase();
                vehicles = vehicles.stream()
                        .filter(v -> st.equalsIgnoreCase(v.getStatus()))
                        .collect(Collectors.toList());
            }
        } else if (status != null && !status.isBlank()) {
            vehicles = vehicleRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        } else {
            vehicles = vehicleRepository.findAll();
        }
        return vehicles.stream().map(this::mapToAdminDto).collect(Collectors.toList());
    }

    /**
     * [Admin] Xem chi tiết hồ sơ phương tiện
     */
    @Transactional(readOnly = true)
    public AdminVehicleResponseDto getAdminVehicleDetail(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy phương tiện ID: " + vehicleId));
        return mapToAdminDto(vehicle);
    }

    /**
     * [Admin] Phê duyệt phương tiện
     */
    public AdminVehicleResponseDto approveVehicle(Long adminId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy phương tiện ID: " + vehicleId));
        User reviewer = userRepository.findById(adminId).orElse(null);

        String cleanPlate = vehicle.getLicensePlate().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        String qrToken = "BKQR-VEH" + vehicle.getId() + "-" + cleanPlate + "-2026";

        vehicle.setStatus("APPROVED");
        vehicle.setRejectionReason(null);
        vehicle.setQrToken(qrToken);
        vehicle.setReviewedBy(reviewer);
        vehicle.setReviewedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToAdminDto(saved);
    }

    /**
     * [Admin] Từ chối phương tiện
     */
    public AdminVehicleResponseDto rejectVehicle(Long adminId, Long vehicleId, RejectVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy phương tiện ID: " + vehicleId));
        User reviewer = userRepository.findById(adminId).orElse(null);

        vehicle.setStatus("REJECTED");
        vehicle.setRejectionReason(request.getReason().trim());
        vehicle.setQrToken(null);
        vehicle.setReviewedBy(reviewer);
        vehicle.setReviewedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToAdminDto(saved);
    }

    // =========================================================================
    // HELPER MAPPING CHUYÊN BIỆT THEO VAI TRÒ
    // =========================================================================

    public UserVehicleResponseDto mapToUserDto(Vehicle v) {
        Optional<ParkingSession> activeSession = parkingSessionRepository.findByVehicleIdAndStatus(v.getId(), "ACTIVE");
        boolean isParked = activeSession.isPresent();
        String lotName = activeSession.map(s -> s.getParkingLot().getName()).orElse(null);
        return UserVehicleResponseDto.fromEntity(v, isParked, lotName);
    }

    public AdminVehicleResponseDto mapToAdminDto(Vehicle v) {
        Optional<ParkingSession> activeSession = parkingSessionRepository.findByVehicleIdAndStatus(v.getId(), "ACTIVE");
        boolean isParked = activeSession.isPresent();
        String lotName = activeSession.map(s -> s.getParkingLot().getName()).orElse(null);
        return AdminVehicleResponseDto.fromEntity(v, isParked, lotName);
    }
}
