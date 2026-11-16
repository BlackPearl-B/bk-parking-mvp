package vn.edu.hcmut.bkparking.dto;

import vn.edu.hcmut.bkparking.entity.Vehicle;

/**
 * @deprecated Khuyến nghị sử dụng {@link UserVehicleResponseDto} cho User Portal
 *             và {@link AdminVehicleResponseDto} cho Admin Portal.
 */
@Deprecated
public class VehicleResponseDto extends AdminVehicleResponseDto {

    public VehicleResponseDto() {
        super();
    }

    public static VehicleResponseDto fromEntity(Vehicle v, boolean currentlyParked, String currentLotName) {
        AdminVehicleResponseDto adminDto = AdminVehicleResponseDto.fromEntity(v, currentlyParked, currentLotName);
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setId(adminDto.getId());
        dto.setLicensePlate(adminDto.getLicensePlate());
        dto.setVehicleType(adminDto.getVehicleType());
        dto.setBrand(adminDto.getBrand());
        dto.setModel(adminDto.getModel());
        dto.setColor(adminDto.getColor());
        dto.setStatus(adminDto.getStatus());
        dto.setRejectionReason(adminDto.getRejectionReason());
        dto.setQrToken(adminDto.getQrToken());
        dto.setCurrentlyParked(adminDto.isCurrentlyParked());
        dto.setCurrentParkingLotName(adminDto.getCurrentParkingLotName());
        dto.setCreatedAt(adminDto.getCreatedAt());
        dto.setOwnerId(adminDto.getOwnerId());
        dto.setOwnerName(adminDto.getOwnerName());
        dto.setOwnerCode(adminDto.getOwnerCode());
        dto.setOwnerEmail(adminDto.getOwnerEmail());
        dto.setRegistrationDocUrl(adminDto.getRegistrationDocUrl());
        dto.setReviewerId(adminDto.getReviewerId());
        dto.setReviewerName(adminDto.getReviewerName());
        dto.setReviewedAt(adminDto.getReviewedAt());
        return dto;
    }
}
