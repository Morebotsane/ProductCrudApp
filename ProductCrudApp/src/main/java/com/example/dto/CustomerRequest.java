package com.example.dto;

/**
 * DTO used for creating or updating customers.
 */
public class CustomerRequest {

    private String firstName;
    private String lastName;
    private String email;

    public CustomerRequest() {}

    public CustomerRequest(String firstName,String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;       
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFirstName(){
    	return firstName;
    }
    
    public void setFirstName(String firstName){
    	this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
