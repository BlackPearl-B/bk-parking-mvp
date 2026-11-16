package vn.edu.hcmut.bkparking.dto;

public class CheckOutRequest {
    private String qrOrPlate;
    private String qrToken;
    private String exitGate;
    private String note;

    public CheckOutRequest() {}

    public CheckOutRequest(String qrOrPlate, String exitGate, String note) {
        this.qrOrPlate = qrOrPlate;
        this.exitGate = exitGate;
        this.note = note;
    }

    public String getQrOrPlate() {
        if (qrOrPlate != null && !qrOrPlate.isBlank()) return qrOrPlate;
        return qrToken;
    }

    public void setQrOrPlate(String qrOrPlate) { this.qrOrPlate = qrOrPlate; }

    public String getQrToken() { return qrToken; }

    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public String getExitGate() { return exitGate; }

    public void setExitGate(String exitGate) { this.exitGate = exitGate; }

    public String getNote() { return note; }

    public void setNote(String note) { this.note = note; }
}
