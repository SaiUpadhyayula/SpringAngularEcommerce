package com.techie.shoppingstore.exceptions;

import lombok.Data;

@Data
public class ApiResponse {
    private Integer status;
    private String message;

    public ApiResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}