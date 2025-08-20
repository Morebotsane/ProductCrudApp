package com.example.dto;

import com.example.entities.Customer;

/**
 * DTO used for returning customer data to API clients.
 */
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public CustomerResponse() {}

    public CustomerResponse(Long id, String firstName,String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName(){
    	return this.lastName;
    }
    
    public void setLastName(String lastName){
    	this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Map from entity to DTO.
     */
    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail()
        );
    }
}
