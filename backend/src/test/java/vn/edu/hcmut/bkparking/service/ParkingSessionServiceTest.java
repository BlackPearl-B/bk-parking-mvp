package vn.edu.hcmut.bkparking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.hcmut.bkparking.common.exception.BusinessException;
import vn.edu.hcmut.bkparking.common.exception.ErrorCode;
import vn.edu.hcmut.bkparking.dto.CheckInRequest;
import vn.edu.hcmut.bkparking.dto.CheckOutRequest;
import vn.edu.hcmut.bkparking.dto.ParkingSessionResponseDto;
import vn.edu.hcmut.bkparking.entity.*;
import vn.edu.hcmut.bkparking.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSessionServiceTest {

    @Mock
    private ParkingSessionRepository parkingSessionRepository;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserPackageRepository userPackageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ParkingSessionService parkingSessionService;

    private User mockUser;
    private User mockStaff;
    private Vehicle mockApprovedVehicle;
    private ParkingLot mockLot;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("Nguyễn Văn An");
        mockUser.setCode("2311770");

        mockStaff = new User();
        mockStaff.setId(5L);
        mockStaff.setFullName("Võ Văn Bảo Vệ");
        mockStaff.setCode("STF001");

        mockApprovedVehicle = new Vehicle();
        mockApprovedVehicle.setId(10L);
        mockApprovedVehicle.setUser(mockUser);
        mockApprovedVehicle.setLicensePlate("59T2-119.88");
        mockApprovedVehicle.setVehicleType("MOTORBIKE");
        mockApprovedVehicle.setStatus("APPROVED");
        mockApprovedVehicle.setQrToken("BKQR-VEH10-59T211988-2026");

        mockLot = new ParkingLot();
        mockLot.setId(1L);
        mockLot.setCode("LOT_A1");
        mockLot.setName("Nhà xe Khu A1");
        mockLot.setStatus("OPEN");
        mockLot.setCapacity(200);
    }

    // -------------------------------------------------------------------------
    // 1. CHECK-IN TESTS (TIÊU CHÍ: POST /staff/sessions/check-in, ANTI-DUPLICATE)
    // -------------------------------------------------------------------------

    @Test
    void testCheckIn_Success() {
        CheckInRequest request = new CheckInRequest("BKQR-VEH10-59T211988-2026", 1L, "Cổng A1 - Làn 1", "Gửi bình thường");

        when(vehicleRepository.findByQrToken("BKQR-VEH10-59T211988-2026")).thenReturn(Optional.of(mockApprovedVehicle));
        when(userPackageRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(new UserPackage()));
        when(parkingSessionRepository.findByVehicleIdAndStatus(10L, "ACTIVE")).thenReturn(Optional.empty());
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(mockLot));
        when(parkingSessionRepository.countByParkingLotIdAndStatus(1L, "ACTIVE")).thenReturn(50L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(mockStaff));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenAnswer(invocation -> {
            ParkingSession s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        ParkingSessionResponseDto result = parkingSessionService.checkIn(5L, request);

        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(100L);
        assertThat(result.getVehicleId()).isEqualTo(10L);
        assertThat(result.getLicensePlate()).isEqualTo("59T2-119.88");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getParkingLotName()).isEqualTo("Nhà xe Khu A1");
        assertThat(result.getEntryGate()).isEqualTo("Cổng A1 - Làn 1");

        verify(parkingSessionRepository, times(1)).save(any(ParkingSession.class));
    }

    @Test
    void testCheckIn_DuplicateActiveSession_ThrowsConflict() {
        // Tiêu chí 2: Không tạo hai session ACTIVE cho cùng xe
        CheckInRequest request = new CheckInRequest("BKQR-VEH10-59T211988-2026", 1L, "Cổng A1", null);

        ParkingSession existingSession = new ParkingSession();
        existingSession.setId(50L);
        existingSession.setParkingLot(mockLot);
        existingSession.setStatus("ACTIVE");

        when(vehicleRepository.findByQrToken("BKQR-VEH10-59T211988-2026")).thenReturn(Optional.of(mockApprovedVehicle));
        when(userPackageRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(new UserPackage()));
        when(parkingSessionRepository.findByVehicleIdAndStatus(10L, "ACTIVE")).thenReturn(Optional.of(existingSession));

        assertThatThrownBy(() -> parkingSessionService.checkIn(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_ALREADY_ACTIVE);

        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void testCheckIn_VehicleNotApproved_ThrowsBadRequest() {
        CheckInRequest request = new CheckInRequest("59T2-119.88", 1L, "Cổng A1", null);

        Vehicle pendingVehicle = new Vehicle();
        pendingVehicle.setId(11L);
        pendingVehicle.setLicensePlate("59T2-119.88");
        pendingVehicle.setStatus("PENDING");

        when(vehicleRepository.findByQrToken("59T2-119.88")).thenReturn(Optional.empty());
        when(vehicleRepository.findByLicensePlateIgnoreCase("59T2-119.88")).thenReturn(Optional.of(pendingVehicle));

        assertThatThrownBy(() -> parkingSessionService.checkIn(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VEHICLE_NOT_APPROVED);

        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void testCheckIn_PackageIneligible_ThrowsBadRequest() {
        CheckInRequest request = new CheckInRequest("BKQR-VEH10-59T211988-2026", 1L, "Cổng A1", null);

        when(vehicleRepository.findByQrToken("BKQR-VEH10-59T211988-2026")).thenReturn(Optional.of(mockApprovedVehicle));
        when(userPackageRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of()); // No active package

        assertThatThrownBy(() -> parkingSessionService.checkIn(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PACKAGE_INELIGIBLE);

        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void testCheckIn_ParkingLotFull_ThrowsConflict() {
        CheckInRequest request = new CheckInRequest("BKQR-VEH10-59T211988-2026", 1L, "Cổng A1", null);

        when(vehicleRepository.findByQrToken("BKQR-VEH10-59T211988-2026")).thenReturn(Optional.of(mockApprovedVehicle));
        when(userPackageRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(new UserPackage()));
        when(parkingSessionRepository.findByVehicleIdAndStatus(10L, "ACTIVE")).thenReturn(Optional.empty());
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(mockLot));
        when(parkingSessionRepository.countByParkingLotIdAndStatus(1L, "ACTIVE")).thenReturn(200L); // Capacity is 200

        assertThatThrownBy(() -> parkingSessionService.checkIn(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARKING_LOT_FULL);

        verify(parkingSessionRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // 2. CHECK-OUT TESTS (TIÊU CHÍ: POST /staff/sessions/check-out)
    // -------------------------------------------------------------------------

    @Test
    void testCheckOut_Success() {
        CheckOutRequest request = new CheckOutRequest("BKQR-VEH10-59T211988-2026", "Cổng A1 - Làn Ra", "Ra bình thường");

        ParkingSession activeSession = new ParkingSession();
        activeSession.setId(100L);
        activeSession.setVehicle(mockApprovedVehicle);
        activeSession.setParkingLot(mockLot);
        activeSession.setStatus("ACTIVE");
        activeSession.setCheckinTime(LocalDateTime.now().minusHours(2));

        when(vehicleRepository.findByQrToken("BKQR-VEH10-59T211988-2026")).thenReturn(Optional.of(mockApprovedVehicle));
        when(parkingSessionRepository.findByVehicleIdAndStatus(10L, "ACTIVE")).thenReturn(Optional.of(activeSession));
        when(userRepository.findById(5L)).thenReturn(Optional.of(mockStaff));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ParkingSessionResponseDto result = parkingSessionService.checkOut(5L, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getCheckoutTime()).isNotEmpty();
        assertThat(result.getExitGate()).isEqualTo("Cổng A1 - Làn Ra");

        verify(parkingSessionRepository, times(1)).save(activeSession);
    }

    @Test
    void testCheckOut_NoActiveSession_ThrowsBadRequest() {
        CheckOutRequest request = new CheckOutRequest("BKQR-VEH10-59T211988-2026", "Cổng Ra", null);

        when(vehicleRepository.findByQrToken("BKQR-VEH10-59T211988-2026")).thenReturn(Optional.of(mockApprovedVehicle));
        when(parkingSessionRepository.findByVehicleIdAndStatus(10L, "ACTIVE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingSessionService.checkOut(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_ACTIVE_SESSION);

        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void testResolveVehicle_NotFound_ThrowsInvalidQrToken() {
        CheckInRequest request = new CheckInRequest("UNKNOWN_QR", 1L, "Cổng A1", null);

        when(vehicleRepository.findByQrToken("UNKNOWN_QR")).thenReturn(Optional.empty());
        when(vehicleRepository.findByLicensePlateIgnoreCase("UNKNOWN_QR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingSessionService.checkIn(5L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_QR_TOKEN);
    }
}
