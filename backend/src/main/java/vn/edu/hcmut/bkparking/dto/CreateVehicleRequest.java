package vn.edu.hcmut.bkparking.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateVehicleRequest {

    @NotBlank(message = "Biển số xe không được để trống")
    private String licensePlate;

    @NotBlank(message = "Loại phương tiện không được để trống (MOTORBIKE, BICYCLE, ELECTRIC_BIKE)")
    private String vehicleType;

    private String brand;
    private String model;
    private String color;
    private String registrationDocUrl;

    public CreateVehicleRequest() {}

    public CreateVehicleRequest(String licensePlate, String vehicleType, String brand, String model, String color, String registrationDocUrl) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.registrationDocUrl = registrationDocUrl;
    }

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

    public String getRegistrationDocUrl() { return registrationDocUrl; }
    public void setRegistrationDocUrl(String registrationDocUrl) { this.registrationDocUrl = registrationDocUrl; }
}
