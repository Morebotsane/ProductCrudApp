package com.example.config;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.auth.LoginConfig;

import com.example.exception.ConstraintViolationExceptionMapper;
import com.example.resources.*;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
@LoginConfig(authMethod = "MP-JWT") // tells GlassFish to use MicroProfile JWT
@DeclareRoles({"ROLE_CUSTOMER", "ROLE_ADMIN"})
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
        classes.add(ShippingResource.class);
        classes.add(AuditResource.class);
        
        return classes;
    }
}
