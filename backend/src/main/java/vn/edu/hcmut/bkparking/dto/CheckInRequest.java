package vn.edu.hcmut.bkparking.dto;

import jakarta.validation.constraints.NotNull;

public class CheckInRequest {
    private String qrOrPlate;
    private String qrToken;

    @NotNull(message = "Vui lòng chọn bãi đỗ xe (parkingLotId)")
    private Long parkingLotId;

    private String entryGate;
    private String note;

    public CheckInRequest() {}

    public CheckInRequest(String qrOrPlate, Long parkingLotId, String entryGate, String note) {
        this.qrOrPlate = qrOrPlate;
        this.parkingLotId = parkingLotId;
        this.entryGate = entryGate;
        this.note = note;
    }

    public String getQrOrPlate() {
        if (qrOrPlate != null && !qrOrPlate.isBlank()) return qrOrPlate;
        return qrToken;
    }

    public void setQrOrPlate(String qrOrPlate) { this.qrOrPlate = qrOrPlate; }

    public String getQrToken() { return qrToken; }

    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public Long getParkingLotId() { return parkingLotId; }

    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }

    public String getEntryGate() { return entryGate; }

    public void setEntryGate(String entryGate) { this.entryGate = entryGate; }

    public String getNote() { return note; }

    public void setNote(String note) { this.note = note; }
}
