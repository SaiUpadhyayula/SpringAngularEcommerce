package com.techie.shoppingstore.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ApiResponse {
    private Integer status;
    private String message;

    public ApiResponse(Integer status, String message){
        this.status = status;
        this.message = message;
    }
}