package vn.edu.hcmut.bkparking.dto;

import vn.edu.hcmut.bkparking.entity.ParkingSession;

public class ParkingSessionResponseDto {
    private Long sessionId;
    private Long vehicleId;
    private String licensePlate;
    private String vehicleType;
    private String ownerName;
    private String ownerCode;
    private Long parkingLotId;
    private String parkingLotCode;
    private String parkingLotName;
    private String entryGate;
    private String exitGate;
    private String checkinTime;
    private String checkoutTime;
    private String status;
    private String note;

    public ParkingSessionResponseDto() {}

    public static ParkingSessionResponseDto fromEntity(ParkingSession s) {
        if (s == null) return null;
        ParkingSessionResponseDto dto = new ParkingSessionResponseDto();
        dto.sessionId = s.getId();
        if (s.getVehicle() != null) {
            dto.vehicleId = s.getVehicle().getId();
            dto.licensePlate = s.getVehicle().getLicensePlate();
            dto.vehicleType = s.getVehicle().getVehicleType();
            if (s.getVehicle().getUser() != null) {
                dto.ownerName = s.getVehicle().getUser().getFullName();
                dto.ownerCode = s.getVehicle().getUser().getCode();
            }
        }
        if (s.getParkingLot() != null) {
            dto.parkingLotId = s.getParkingLot().getId();
            dto.parkingLotCode = s.getParkingLot().getCode();
            dto.parkingLotName = s.getParkingLot().getName();
        }
        dto.entryGate = s.getEntryGate();
        dto.exitGate = s.getExitGate();
        dto.checkinTime = s.getCheckinTime() != null ? s.getCheckinTime().toString() : null;
        dto.checkoutTime = s.getCheckoutTime() != null ? s.getCheckoutTime().toString() : null;
        dto.status = s.getStatus();
        dto.note = s.getNote();
        return dto;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

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

    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }

    public String getParkingLotCode() { return parkingLotCode; }
    public void setParkingLotCode(String parkingLotCode) { this.parkingLotCode = parkingLotCode; }

    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }

    public String getEntryGate() { return entryGate; }
    public void setEntryGate(String entryGate) { this.entryGate = entryGate; }

    public String getExitGate() { return exitGate; }
    public void setExitGate(String exitGate) { this.exitGate = exitGate; }

    public String getCheckinTime() { return checkinTime; }
    public void setCheckinTime(String checkinTime) { this.checkinTime = checkinTime; }

    public String getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(String checkoutTime) { this.checkoutTime = checkoutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Compatibility getters for Frontend / API Contract
    public Long getId() { return sessionId; }
    public String getCheckInTime() { return checkinTime; }
    public String getCheckOutTime() { return checkoutTime; }
    public String getCheckInGate() { return entryGate; }
    public String getCheckOutGate() { return exitGate; }

    public Long getDurationMinutes() {
        if (checkinTime != null && checkoutTime != null) {
            try {
                java.time.LocalDateTime in = java.time.LocalDateTime.parse(checkinTime);
                java.time.LocalDateTime out = java.time.LocalDateTime.parse(checkoutTime);
                return java.time.Duration.between(in, out).toMinutes();
            } catch (Exception ignored) {}
        }
        return null;
    }
}
