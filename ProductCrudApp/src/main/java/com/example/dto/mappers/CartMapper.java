package com.example.dto.mappers;

import com.example.dto.CartItemResponse;
import com.example.dto.CartResponse;
import com.example.entities.Cart;
//import com.example.entities.CartItem;
import com.example.services.CartService; // we’ll need it for totals

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CartMapper {

    @Inject
    private CartService cartService;

    public CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(CartItemResponse::fromEntity)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getStatus(),
                itemResponses,
                cartService.getTotal(cart),
                cartService.getTotalWithVAT(cart)
        );
    }
}
