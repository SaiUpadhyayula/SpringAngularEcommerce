package com.techie.shoppingstore.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class ApiError {
    private Integer status;
    private String message;
    private String developerMessage;
}
