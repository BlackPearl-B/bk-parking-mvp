package vn.edu.hcmut.bkparking.dto;

import jakarta.validation.constraints.NotBlank;

public class RejectVehicleRequest {

    @NotBlank(message = "Vui lòng nhập lý do từ chối")
    private String reason;

    public RejectVehicleRequest() {
    }

    public RejectVehicleRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
