package com.example.dto;

import com.example.entities.AddressType;

public class AddressResponse {

    private Long id;
    private String line1;
    private String line2;
    private String city;
    private String region;
    private String postalCode;
    private String country;
    private AddressType type;
    private boolean isDefault;

    public AddressResponse(Long id, String line1, String line2, String city,
                           String region, String postalCode, String country,
                           AddressType type, boolean isDefault) {
        this.id = id;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
        this.isDefault = isDefault;
    }

    // --- getters ---
    public Long getId() {
        return id;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public AddressType getType() {
        return type;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
