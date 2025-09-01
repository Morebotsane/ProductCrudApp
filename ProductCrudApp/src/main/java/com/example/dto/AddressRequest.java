package com.example.dto;

import com.example.entities.AddressType;
import jakarta.validation.constraints.NotBlank;

public class AddressRequest {

    @NotBlank(message = "Line 1 cannot be empty")
    private String line1;

    private String line2;

    @NotBlank(message = "City cannot be empty")
    private String city;

    private String region;

    @NotBlank(message = "Postal code cannot be empty")
    private String postalCode;

    @NotBlank(message = "Country cannot be empty")
    private String country;

    private AddressType type; // BILLING or SHIPPING
    private boolean isDefault;

    // --- getters & setters ---
    public String getLine1() {
        return line1;
    }
    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }
    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public AddressType getType() {
        return type;
    }
    public void setType(AddressType type) {
        this.type = type;
    }

    public boolean isDefault() {
        return isDefault;
    }
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
