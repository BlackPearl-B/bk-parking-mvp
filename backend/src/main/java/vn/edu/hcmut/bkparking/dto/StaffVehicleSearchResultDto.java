package vn.edu.hcmut.bkparking.dto;

import vn.edu.hcmut.bkparking.entity.Vehicle;

public class StaffVehicleSearchResultDto {
    private Long vehicleId;
    private String licensePlate;
    private String vehicleType;
    private String brand;
    private String model;
    private String color;
    private String status;
    private String qrToken;
    private String ownerName;
    private String ownerCode;
    private String ownerPhone;
    private boolean packageEligible;
    private boolean currentlyParked;
    private String currentParkingLot;
    private ParkingSessionResponseDto activeSession;

    public StaffVehicleSearchResultDto() {}

    public static StaffVehicleSearchResultDto from(
            Vehicle v,
            boolean packageEligible,
            boolean currentlyParked,
            ParkingSessionResponseDto activeSession) {
        StaffVehicleSearchResultDto dto = new StaffVehicleSearchResultDto();
        dto.vehicleId = v.getId();
        dto.licensePlate = v.getLicensePlate();
        dto.vehicleType = v.getVehicleType();
        dto.brand = v.getBrand();
        dto.model = v.getModel();
        dto.color = v.getColor();
        dto.status = v.getStatus();
        dto.qrToken = v.getQrToken();
        if (v.getUser() != null) {
            dto.ownerName = v.getUser().getFullName();
            dto.ownerCode = v.getUser().getCode();
            dto.ownerPhone = v.getUser().getPhone();
        }
        dto.packageEligible = packageEligible;
        dto.currentlyParked = currentlyParked;
        dto.activeSession = activeSession;
        if (activeSession != null) {
            dto.currentParkingLot = activeSession.getParkingLotName();
        }
        return dto;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerCode() { return ownerCode; }
    public void setOwnerCode(String ownerCode) { this.ownerCode = ownerCode; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public boolean isPackageEligible() { return packageEligible; }
    public void setPackageEligible(boolean packageEligible) { this.packageEligible = packageEligible; }

    public boolean isCurrentlyParked() { return currentlyParked; }
    public void setCurrentlyParked(boolean currentlyParked) { this.currentlyParked = currentlyParked; }

    public String getCurrentParkingLot() { return currentParkingLot; }
    public void setCurrentParkingLot(String currentParkingLot) { this.currentParkingLot = currentParkingLot; }

    public ParkingSessionResponseDto getActiveSession() { return activeSession; }
    public void setActiveSession(ParkingSessionResponseDto activeSession) { this.activeSession = activeSession; }

    // Compatibility getters for Frontend / API Contract
    public String getStudentOrStaffId() { return ownerCode; }
    public String getPhoneNumber() { return ownerPhone; }
}
