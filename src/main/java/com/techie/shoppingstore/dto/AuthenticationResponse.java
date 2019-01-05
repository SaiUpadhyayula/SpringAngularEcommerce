package com.techie.shoppingstore.dto;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private static final String TOKEN_TYPE = "Bearer";

    private String accessToken;
}
