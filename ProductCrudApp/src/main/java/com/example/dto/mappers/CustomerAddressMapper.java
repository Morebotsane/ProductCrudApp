package com.example.dto.mappers;

import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.entities.Address;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerAddressMapper {

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountry(),
                address.getType(),
                address.isDefault()
        );
    }

    public Address toEntity(AddressRequest request) {
        Address address = new Address();
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setRegion(request.getRegion());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setType(request.getType());
        address.setDefault(request.isDefault());
        return address;
    }

    public void updateEntity(Address address, AddressRequest request) {
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setRegion(request.getRegion());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setType(request.getType());
        address.setDefault(request.isDefault());
    }
}
