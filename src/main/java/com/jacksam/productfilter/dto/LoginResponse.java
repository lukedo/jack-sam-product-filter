package com.jacksam.productfilter.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserDTO user
) {}
