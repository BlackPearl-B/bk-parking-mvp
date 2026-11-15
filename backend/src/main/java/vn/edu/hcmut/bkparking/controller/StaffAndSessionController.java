package vn.edu.hcmut.bkparking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmut.bkparking.common.dto.ApiResponse;
import vn.edu.hcmut.bkparking.common.exception.BusinessException;
import vn.edu.hcmut.bkparking.common.exception.ErrorCode;
import vn.edu.hcmut.bkparking.dto.CheckInRequest;
import vn.edu.hcmut.bkparking.dto.CheckOutRequest;
import vn.edu.hcmut.bkparking.dto.ParkingSessionResponseDto;
import vn.edu.hcmut.bkparking.entity.*;
import vn.edu.hcmut.bkparking.repository.*;
import vn.edu.hcmut.bkparking.security.UserPrincipal;
import vn.edu.hcmut.bkparking.service.ParkingSessionService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "4. Staff Gate Control, QR Scan & History (TV2 + TV1 Integration)", description = "API xác thực mã QR, ghi nhận xe VÀO/RA (check-in/check-out), tra cứu xe và lịch sử gửi xe")
public class StaffAndSessionController {

    private final ParkingSessionService parkingSessionService;
    private final VehicleRepository vehicleRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final UserPackageRepository userPackageRepository;

    public StaffAndSessionController(
            ParkingSessionService parkingSessionService,
            VehicleRepository vehicleRepository,
            ParkingSessionRepository parkingSessionRepository,
            UserPackageRepository userPackageRepository) {
        this.parkingSessionService = parkingSessionService;
        this.vehicleRepository = vehicleRepository;
        this.parkingSessionRepository = parkingSessionRepository;
        this.userPackageRepository = userPackageRepository;
    }

    public static class VerifyQrRequest {
        private String qrOrPlate;
        private String qrToken;

        public String getQrOrPlate() {
            if (qrOrPlate != null && !qrOrPlate.isBlank()) return qrOrPlate;
            return qrToken;
        }

        public void setQrOrPlate(String qrOrPlate) { this.qrOrPlate = qrOrPlate; }
        public String getQrToken() { return qrToken; }
        public void setQrToken(String qrToken) { this.qrToken = qrToken; }
    }

    @PostMapping("/api/v1/staff/qr/verify")
    @Operation(summary = "[Staff] Quét / nhập mã QR hoặc biển số để kiểm tra hợp lệ trước khi cho VÀO/RA")
    public ApiResponse<Map<String, Object>> verifyQr(@Valid @RequestBody VerifyQrRequest request) {
        String input = request.getQrOrPlate();
        if (input == null || input.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã QR hoặc biển số không được để trống");
        }
        Vehicle vehicle = resolveVehicle(input);
        List<UserPackage> activePackages = userPackageRepository.findByUserIdAndStatus(vehicle.getUser().getId(), "ACTIVE");
        Optional<ParkingSession> activeSession = parkingSessionRepository.findByVehicleIdAndStatus(vehicle.getId(), "ACTIVE");

        boolean isApproved = "APPROVED".equalsIgnoreCase(vehicle.getStatus());
        boolean hasPackage = !activePackages.isEmpty();
        boolean validForEntry = isApproved && hasPackage && activeSession.isEmpty();
        boolean validForExit = activeSession.isPresent();

        String recommendedAction;
        String statusMessage;
        if (!isApproved) {
            recommendedAction = "DENY";
            statusMessage = "Phương tiện chưa được duyệt (Trạng thái: " + vehicle.getStatus() + ")";
        } else if (activeSession.isPresent()) {
            recommendedAction = "CHECK_OUT";
            statusMessage = "Xe đang gửi tại " + activeSession.get().getParkingLot().getName() + " — Sẵn sàng xác nhận RA";
        } else if (!hasPackage) {
            recommendedAction = "DENY";
            statusMessage = "Tài khoản chưa có gói đỗ xe còn hiệu lực";
        } else {
            recommendedAction = "CHECK_IN";
            statusMessage = "Phương tiện và gói đỗ xe hợp lệ — Sẵn sàng xác nhận VÀO bãi";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", isApproved && (hasPackage || activeSession.isPresent()));
        result.put("recommendedAction", recommendedAction);
        result.put("statusMessage", statusMessage);
        result.put("vehicleId", vehicle.getId());
        result.put("licensePlate", vehicle.getLicensePlate());
        result.put("vehicleType", vehicle.getVehicleType());
        result.put("brand", vehicle.getBrand());
        result.put("model", vehicle.getModel());
        result.put("color", vehicle.getColor());
        result.put("vehicleStatus", vehicle.getStatus());
        result.put("qrToken", vehicle.getQrToken());
        result.put("ownerName", vehicle.getUser().getFullName());
        result.put("ownerCode", vehicle.getUser().getCode());
        result.put("packageEligible", hasPackage);
        result.put("packageName", hasPackage ? activePackages.get(0).getParkingPackage().getName() : null);
        result.put("validForEntry", validForEntry);
        result.put("validForExit", validForExit);
        result.put("activeSession", activeSession.map(this::toSessionDto).orElse(null));
        return ApiResponse.ok(result);
    }

    @PostMapping("/api/v1/staff/sessions/check-in")
    @Operation(summary = "[Staff] Ghi nhận xe VÀO bãi (Check-in)", description = "Kiểm tra quyền gửi xe, sức chứa bãi và chống tạo 2 lượt vào đang mở cho cùng 1 xe")
    public ApiResponse<ParkingSessionResponseDto> checkIn(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckInRequest request) {
        ParkingSessionResponseDto saved = parkingSessionService.checkIn(principal.getId(), request);
        return ApiResponse.ok("Xác nhận cho xe VÀO bãi thành công", saved);
    }

    @PostMapping("/api/v1/staff/sessions/check-out")
    @Operation(summary = "[Staff] Ghi nhận xe RA khỏi bãi (Check-out)", description = "Đóng ParkingSession đang mở của xe và giải phóng chỗ trống cho bãi")
    public ApiResponse<ParkingSessionResponseDto> checkOut(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckOutRequest request) {
        ParkingSessionResponseDto saved = parkingSessionService.checkOut(principal.getId(), request);
        return ApiResponse.ok("Xác nhận cho xe RA khỏi bãi thành công", saved);
    }

    @GetMapping("/api/v1/staff/vehicles/search")
    @Operation(summary = "[Staff] Tra cứu phương tiện theo biển số / MSSV / họ tên / mã QR")
    public ApiResponse<List<Map<String, Object>>> searchVehicles(@RequestParam(defaultValue = "") String keyword) {
        List<Vehicle> vehicles = keyword.isBlank()
                ? vehicleRepository.findAll()
                : vehicleRepository.searchVehicles(keyword.trim());

        List<Map<String, Object>> list = vehicles.stream().map(v -> {
            Optional<ParkingSession> activeSession = parkingSessionRepository.findByVehicleIdAndStatus(v.getId(), "ACTIVE");
            List<UserPackage> activePackages = userPackageRepository.findByUserIdAndStatus(v.getUser().getId(), "ACTIVE");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vehicleId", v.getId());
            m.put("licensePlate", v.getLicensePlate());
            m.put("vehicleType", v.getVehicleType());
            m.put("brand", v.getBrand());
            m.put("model", v.getModel());
            m.put("color", v.getColor());
            m.put("status", v.getStatus());
            m.put("qrToken", v.getQrToken());
            m.put("ownerName", v.getUser().getFullName());
            m.put("ownerCode", v.getUser().getCode());
            m.put("ownerPhone", v.getUser().getPhone());
            m.put("packageEligible", !activePackages.isEmpty());
            m.put("currentlyParked", activeSession.isPresent());
            m.put("activeSession", activeSession.map(ParkingSessionResponseDto::fromEntity).orElse(null));
            return m;
        }).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/api/v1/staff/sessions/active")
    @Operation(summary = "[Staff] Danh sách xe hiện đang gửi trong bãi")
    public ApiResponse<List<ParkingSessionResponseDto>> getActiveSessions(@RequestParam(required = false) Long parkingLotId) {
        List<ParkingSessionResponseDto> list = parkingSessionService.getActiveSessions(parkingLotId);
        return ApiResponse.ok(list);
    }

    @GetMapping("/api/v1/staff/sessions/history")
    @Operation(summary = "[Staff] Lịch sử toàn bộ lượt xe ra/vào bãi")
    public ApiResponse<List<ParkingSessionResponseDto>> getAllSessionHistory() {
        List<ParkingSessionResponseDto> list = parkingSessionService.getAllSessionHistory();
        return ApiResponse.ok(list);
    }

    @GetMapping("/api/v1/sessions/my-history")
    @Operation(summary = "[User] Lịch sử gửi xe ra/vào của tôi")
    public ApiResponse<List<ParkingSessionResponseDto>> getMySessionHistory(@AuthenticationPrincipal UserPrincipal principal) {
        List<ParkingSessionResponseDto> list = parkingSessionService.getMySessionHistory(principal.getId());
        return ApiResponse.ok(list);
    }

    private Vehicle resolveVehicle(String qrOrPlate) {
        return parkingSessionService.resolveVehicle(qrOrPlate);
    }

    private ParkingSessionResponseDto toSessionDto(ParkingSession s) {
        return ParkingSessionResponseDto.fromEntity(s);
    }
}

