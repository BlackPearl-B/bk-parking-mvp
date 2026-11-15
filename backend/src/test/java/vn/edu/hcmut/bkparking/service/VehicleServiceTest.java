package vn.edu.hcmut.bkparking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.hcmut.bkparking.common.exception.BusinessException;
import vn.edu.hcmut.bkparking.common.exception.ErrorCode;
import vn.edu.hcmut.bkparking.dto.AdminVehicleResponseDto;
import vn.edu.hcmut.bkparking.dto.CreateVehicleRequest;
import vn.edu.hcmut.bkparking.dto.RejectVehicleRequest;
import vn.edu.hcmut.bkparking.dto.UserVehicleResponseDto;
import vn.edu.hcmut.bkparking.entity.User;
import vn.edu.hcmut.bkparking.entity.Vehicle;
import vn.edu.hcmut.bkparking.repository.ParkingSessionRepository;
import vn.edu.hcmut.bkparking.repository.UserRepository;
import vn.edu.hcmut.bkparking.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParkingSessionRepository parkingSessionRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private User mockUser;
    private User mockAdmin;
    private Vehicle mockPendingVehicle;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vehicleService, "maxVehiclesPerUser", 2);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("Nguyễn Văn An");
        mockUser.setCode("2311770");
        mockUser.setEmail("an.nguyen2311770@hcmut.edu.vn");

        mockAdmin = new User();
        mockAdmin.setId(4L);
        mockAdmin.setFullName("Phạm Quản Trị");
        mockAdmin.setCode("ADM001");

        mockPendingVehicle = new Vehicle();
        mockPendingVehicle.setId(10L);
        mockPendingVehicle.setUser(mockUser);
        mockPendingVehicle.setLicensePlate("59T2-119.88");
        mockPendingVehicle.setVehicleType("MOTORBIKE");
        mockPendingVehicle.setStatus("PENDING");
    }

    // -------------------------------------------------------------------------
    // 1. PHÂN HỆ USER: TRẢ VỀ UserVehicleResponseDto (KHÔNG CHỨA THÔNG TIN ADMIN)
    // -------------------------------------------------------------------------

    @Test
    void registerVehicle_Success_ReturnsUserDto() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "59C1-777.88", "MOTORBIKE", "Honda", "AirBlade", "Đen", "https://doc.com/cavet.jpg");

        when(vehicleRepository.findByLicensePlateIgnoreCase("59C1-777.88")).thenReturn(Optional.empty());
        when(vehicleRepository.countByUserIdAndStatusIn(1L, List.of("PENDING", "APPROVED"))).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId(99L);
            return v;
        });

        UserVehicleResponseDto result = vehicleService.registerVehicle(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getLicensePlate()).isEqualTo("59C1-777.88");
        assertThat(result.getStatus()).isEqualTo("PENDING");

        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void registerVehicle_DuplicateLicensePlate_ThrowsConflict() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "59C1-777.88", "MOTORBIKE", "Honda", "AirBlade", "Đen", null);

        when(vehicleRepository.findByLicensePlateIgnoreCase("59C1-777.88"))
                .thenReturn(Optional.of(new Vehicle()));

        assertThatThrownBy(() -> vehicleService.registerVehicle(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_LICENSE_PLATE);

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void registerVehicle_ExceedLimit_ThrowsBadRequest() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "59C1-888.99", "MOTORBIKE", "Honda", "AirBlade", "Đen", null);

        when(vehicleRepository.findByLicensePlateIgnoreCase("59C1-888.99")).thenReturn(Optional.empty());
        when(vehicleRepository.countByUserIdAndStatusIn(1L, List.of("PENDING", "APPROVED"))).thenReturn(2L);

        assertThatThrownBy(() -> vehicleService.registerVehicle(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VEHICLE_LIMIT_EXCEEDED);

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void getMyVehicles_Success_ReturnsUserDtoList() {
        when(vehicleRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(mockPendingVehicle));

        List<UserVehicleResponseDto> list = vehicleService.getMyVehicles(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getLicensePlate()).isEqualTo("59T2-119.88");
    }

    // -------------------------------------------------------------------------
    // 2. PHÂN HỆ ADMIN: TRẢ VỀ AdminVehicleResponseDto (ĐẦY ĐỦ THÔNG TIN DUYỆT)
    // -------------------------------------------------------------------------

    @Test
    void getAdminVehicles_FilterPending_Success() {
        when(vehicleRepository.findByStatusOrderByCreatedAtDesc("PENDING"))
                .thenReturn(List.of(mockPendingVehicle));

        List<AdminVehicleResponseDto> list = vehicleService.getAdminVehicles("PENDING", null);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(list.get(0).getOwnerName()).isEqualTo("Nguyễn Văn An");
        verify(vehicleRepository, times(1)).findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Test
    void approveVehicle_Success_SavesReviewerAndGeneratesQr() {
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(mockPendingVehicle));
        when(userRepository.findById(4L)).thenReturn(Optional.of(mockAdmin));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminVehicleResponseDto result = vehicleService.approveVehicle(4L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getQrToken()).startsWith("BKQR-VEH10-");
        assertThat(result.getReviewerName()).isEqualTo("Phạm Quản Trị");
        assertThat(result.getReviewedAt()).isNotNull();
        assertThat(result.getRejectionReason()).isNull();

        verify(vehicleRepository, times(1)).save(mockPendingVehicle);
    }

    @Test
    void approveVehicle_NotFound_ThrowsNotFoundException() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.approveVehicle(4L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void rejectVehicle_Success_SavesReviewerAndReason() {
        RejectVehicleRequest request = new RejectVehicleRequest("Ảnh cà vẹt bị mờ, không rõ số máy");

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(mockPendingVehicle));
        when(userRepository.findById(4L)).thenReturn(Optional.of(mockAdmin));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminVehicleResponseDto result = vehicleService.rejectVehicle(4L, 10L, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("REJECTED");
        assertThat(result.getRejectionReason()).isEqualTo("Ảnh cà vẹt bị mờ, không rõ số máy");
        assertThat(result.getQrToken()).isNull();
        assertThat(result.getReviewerName()).isEqualTo("Phạm Quản Trị");
        assertThat(result.getReviewedAt()).isNotNull();

        verify(vehicleRepository, times(1)).save(mockPendingVehicle);
    }

    @Test
    void rejectVehicle_NotFound_ThrowsNotFoundException() {
        RejectVehicleRequest request = new RejectVehicleRequest("Lý do từ chối");
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.rejectVehicle(4L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);

        verify(vehicleRepository, never()).save(any());
    }
}
