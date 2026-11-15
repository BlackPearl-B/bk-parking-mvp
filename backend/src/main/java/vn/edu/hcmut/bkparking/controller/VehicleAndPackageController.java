package vn.edu.hcmut.bkparking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmut.bkparking.common.dto.ApiResponse;
import vn.edu.hcmut.bkparking.common.exception.BusinessException;
import vn.edu.hcmut.bkparking.common.exception.ErrorCode;
import vn.edu.hcmut.bkparking.dto.AdminVehicleResponseDto;
import vn.edu.hcmut.bkparking.dto.CreateVehicleRequest;
import vn.edu.hcmut.bkparking.dto.RejectVehicleRequest;
import vn.edu.hcmut.bkparking.dto.UserVehicleResponseDto;
import vn.edu.hcmut.bkparking.entity.ParkingSession;
import vn.edu.hcmut.bkparking.entity.UserPackage;
import vn.edu.hcmut.bkparking.entity.Vehicle;
import vn.edu.hcmut.bkparking.repository.ParkingPackageRepository;
import vn.edu.hcmut.bkparking.repository.ParkingSessionRepository;
import vn.edu.hcmut.bkparking.repository.UserPackageRepository;
import vn.edu.hcmut.bkparking.repository.VehicleRepository;
import vn.edu.hcmut.bkparking.security.UserPrincipal;
import vn.edu.hcmut.bkparking.service.VehicleService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "3. Vehicles, Approval & Packages (TV2 + TV1 Integration)", description = "API đăng ký phương tiện, lấy mã QR, xem gói mẫu và Admin duyệt/từ chối phương tiện")
public class VehicleAndPackageController {

    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    private final ParkingPackageRepository parkingPackageRepository;
    private final UserPackageRepository userPackageRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    public VehicleAndPackageController(
            VehicleService vehicleService,
            VehicleRepository vehicleRepository,
            ParkingPackageRepository parkingPackageRepository,
            UserPackageRepository userPackageRepository,
            ParkingSessionRepository parkingSessionRepository) {
        this.vehicleService = vehicleService;
        this.vehicleRepository = vehicleRepository;
        this.parkingPackageRepository = parkingPackageRepository;
        this.userPackageRepository = userPackageRepository;
        this.parkingSessionRepository = parkingSessionRepository;
    }

    // =========================================================================
    // 1. GÓI ĐỖ XE
    // =========================================================================

    @GetMapping("/api/v1/packages")
    @Operation(summary = "[User] Xem danh sách gói đỗ xe mẫu và gói hiện tại của người dùng")
    public ApiResponse<Map<String, Object>> getPackages(@AuthenticationPrincipal UserPrincipal principal) {
        List<Map<String, Object>> availablePackages = parkingPackageRepository.findByIsActiveTrue().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("code", p.getCode());
            m.put("name", p.getName());
            m.put("description", p.getDescription());
            m.put("vehicleType", p.getVehicleType());
            m.put("price", p.getPrice());
            m.put("durationDays", p.getDurationDays());
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> myPackages = userPackageRepository.findByUserIdOrderByEndDateDesc(principal.getId())
                .stream().map(up -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", up.getId());
                    m.put("packageCode", up.getParkingPackage().getCode());
                    m.put("packageName", up.getParkingPackage().getName());
                    m.put("vehicleType", up.getParkingPackage().getVehicleType());
                    m.put("price", up.getParkingPackage().getPrice());
                    m.put("startDate", up.getStartDate().toString());
                    m.put("endDate", up.getEndDate().toString());
                    m.put("status", up.getStatus());
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("availablePackages", availablePackages);
        result.put("mySubscriptions", myPackages);
        return ApiResponse.ok(result);
    }

    // =========================================================================
    // 2. PHÂN HỆ USER: TRẢ VỀ UserVehicleResponseDto (KHÔNG LỘ THÔNG TIN ADMIN)
    // =========================================================================

    @GetMapping("/api/v1/vehicles/my")
    @Operation(summary = "[User] Xem danh sách phương tiện của tôi")
    public ApiResponse<List<UserVehicleResponseDto>> getMyVehicles(@AuthenticationPrincipal UserPrincipal principal) {
        List<UserVehicleResponseDto> list = vehicleService.getMyVehicles(principal.getId());
        return ApiResponse.ok(list);
    }

    @PostMapping("/api/v1/vehicles")
    @Operation(summary = "[User] Đăng ký phương tiện mới", description = "Trạng thái ban đầu là PENDING. Kiểm tra trùng biển số và giới hạn tối đa 2 xe hợp lệ/người dùng.")
    public ApiResponse<UserVehicleResponseDto> registerVehicle(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateVehicleRequest request) {
        UserVehicleResponseDto saved = vehicleService.registerVehicle(principal.getId(), request);
        return ApiResponse.ok("Gửi yêu cầu đăng ký phương tiện thành công (Trạng thái: Chờ duyệt)", saved);
    }

    @GetMapping("/api/v1/vehicles/{id}/qr")
    @Operation(summary = "[User] Mở mã QR gắn với phương tiện đã được duyệt")
    public ApiResponse<Map<String, Object>> getVehicleQr(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy phương tiện ID: " + id));

        boolean isOwner = vehicle.getUser().getId().equals(principal.getId());
        boolean isAdminOrStaff = !"ROLE_USER".equals(principal.getRoleCode());
        if (!isOwner && !isAdminOrStaff) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!"APPROVED".equalsIgnoreCase(vehicle.getStatus())) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_APPROVED,
                    "Phương tiện chưa được duyệt (trạng thái hiện tại: " + vehicle.getStatus()
                            + ") nên chưa thể sử dụng mã QR");
        }

        List<UserPackage> activePackages = userPackageRepository.findByUserIdAndStatus(vehicle.getUser().getId(),
                "ACTIVE");
        Optional<ParkingSession> activeSession = parkingSessionRepository.findByVehicleIdAndStatus(vehicle.getId(),
                "ACTIVE");

        Map<String, Object> qrData = new LinkedHashMap<>();
        qrData.put("vehicleId", vehicle.getId());
        qrData.put("licensePlate", vehicle.getLicensePlate());
        qrData.put("vehicleType", vehicle.getVehicleType());
        qrData.put("ownerName", vehicle.getUser().getFullName());
        qrData.put("ownerCode", vehicle.getUser().getCode());
        qrData.put("qrToken", vehicle.getQrToken());
        qrData.put("packageEligible", !activePackages.isEmpty());
        qrData.put("currentlyInParkingLot", activeSession.isPresent());
        qrData.put("currentParkingLot", activeSession.map(s -> s.getParkingLot().getName()).orElse(null));
        return ApiResponse.ok(qrData);
    }

    // =========================================================================
    // 3. PHÂN HỆ ADMIN: TRẢ VỀ AdminVehicleResponseDto (ĐẦY ĐỦ THÔNG TIN DUYỆT &
    // AUDIT)
    // =========================================================================

    @GetMapping("/api/v1/admin/vehicles")
    @Operation(summary = "[Admin] Danh sách phương tiện (lọc theo status=PENDING / APPROVED / REJECTED)")
    public ApiResponse<List<AdminVehicleResponseDto>> getAdminVehicles(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        List<AdminVehicleResponseDto> list = vehicleService.getAdminVehicles(status, keyword);
        return ApiResponse.ok("Lấy danh sách xe kiểm duyệt thành công", list);
    }

    @GetMapping("/api/v1/admin/vehicles/{id}")
    @Operation(summary = "[Admin] Xem chi tiết hồ sơ phương tiện")
    public ApiResponse<AdminVehicleResponseDto> getAdminVehicleDetail(@PathVariable Long id) {
        AdminVehicleResponseDto dto = vehicleService.getAdminVehicleDetail(id);
        return ApiResponse.ok(dto);
    }

    @PatchMapping("/api/v1/admin/vehicles/{id}/approve")
    @Operation(summary = "[Admin] Duyệt đăng ký phương tiện", description = "Chuyển trạng thái sang APPROVED và tự động phát hành mã định danh qrToken")
    public ApiResponse<AdminVehicleResponseDto> approveVehicle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        AdminVehicleResponseDto saved = vehicleService.approveVehicle(principal.getId(), id);
        return ApiResponse.ok("Đã duyệt phương tiện và cấp mã QR thành công", saved);
    }

    @PatchMapping("/api/v1/admin/vehicles/{id}/reject")
    @Operation(summary = "[Admin] Từ chối đăng ký phương tiện", description = "Chuyển trạng thái sang REJECTED và lưu lý do từ chối")
    public ApiResponse<AdminVehicleResponseDto> rejectVehicle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RejectVehicleRequest request) {
        AdminVehicleResponseDto saved = vehicleService.rejectVehicle(principal.getId(), id, request);
        return ApiResponse.ok("Đã từ chối hồ sơ đăng ký phương tiện", saved);
    }
}
