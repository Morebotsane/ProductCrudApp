package com.example.resources;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

public class CartResourceIT {

    private static final String BASE_URI = "http://localhost:8080/ProductCrudApp/api";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    public void testCreateCart() {
        RestAssured.given()
            .contentType(ContentType.JSON)   // Request body type (if any)
            .accept(ContentType.JSON)        // Response type expected
        .when()
            .post("/carts")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("status", equalTo("NEW"))
            .body("items", hasSize(0));
    }

    @Test
    public void testAddProductToCart() {
        // First create cart
        int cartId = RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .post("/carts")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Add product with productId=2, quantity=1
        String jsonBody = "{ \"productId\": 2, \"quantity\": 1 }";

        RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(jsonBody)
        .when()
            .post("/carts/" + cartId + "/items")
        .then()
            .statusCode(200)
            .body("items.size()", greaterThan(0))
            .body("items[0].product.id", equalTo(2))
            .body("items[0].quantity", equalTo(1));
    }

    @Test
    public void testGetCartDetails() {
        // Create cart and add product first
        int cartId = RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .post("/carts")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        String jsonBody = "{ \"productId\": 2, \"quantity\": 1 }";

        RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(jsonBody)
            .post("/carts/" + cartId + "/items")
            .then()
            .statusCode(200);

        // Now get cart details
        RestAssured.given()
            .accept(ContentType.JSON)
        .when()
            .get("/carts/" + cartId)
        .then()
            .statusCode(200)
            .body("id", equalTo(cartId))
            .body("status", equalTo("NEW"))
            .body("items", not(empty()))
            .body("total", greaterThan(0f))
            .body("totalWithVAT", greaterThan(0f));
    }
}

