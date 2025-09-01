package com.example.config;

import java.util.HashSet;
import java.util.Set;

import com.example.exception.ConstraintViolationExceptionMapper;
import com.example.resources.*;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ProductResource.class); // this is the main API class
        classes.add(ConstraintViolationExceptionMapper.class); // this is the error handler
        classes.add(CartResource.class);//this is the cart resource class
        classes.add(OrderResource.class);//this is the order resource class
        classes.add(CustomerResource.class);//this is the customer resource  
        classes.add(CustomerAddressResource.class);
        classes.add(PaymentResource.class);
        
        return classes;
    }
}
