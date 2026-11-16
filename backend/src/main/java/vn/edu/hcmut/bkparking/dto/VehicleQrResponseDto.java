package vn.edu.hcmut.bkparking.dto;

public class VehicleQrResponseDto {
    private Long vehicleId;
    private String licensePlate;
    private String vehicleType;
    private String ownerName;
    private String ownerCode;
    private String qrToken;
    private boolean packageEligible;
    private boolean currentlyInParkingLot;
    private String currentParkingLot;

    public VehicleQrResponseDto() {}

    public VehicleQrResponseDto(Long vehicleId, String licensePlate, String vehicleType,
                                String ownerName, String ownerCode, String qrToken,
                                boolean packageEligible, boolean currentlyInParkingLot,
                                String currentParkingLot) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.ownerName = ownerName;
        this.ownerCode = ownerCode;
        this.qrToken = qrToken;
        this.packageEligible = packageEligible;
        this.currentlyInParkingLot = currentlyInParkingLot;
        this.currentParkingLot = currentParkingLot;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerCode() { return ownerCode; }
    public void setOwnerCode(String ownerCode) { this.ownerCode = ownerCode; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public boolean isPackageEligible() { return packageEligible; }
    public void setPackageEligible(boolean packageEligible) { this.packageEligible = packageEligible; }

    public boolean isCurrentlyInParkingLot() { return currentlyInParkingLot; }
    public void setCurrentlyInParkingLot(boolean currentlyInParkingLot) { this.currentlyInParkingLot = currentlyInParkingLot; }

    public String getCurrentParkingLot() { return currentParkingLot; }
    public void setCurrentParkingLot(String currentParkingLot) { this.currentParkingLot = currentParkingLot; }
}
