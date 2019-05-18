package com.techie.shoppingstore.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "VerificationToken")
@Data
public class VerificationToken {
    @Id
    private String id;
    private String token;
    private User user;
    private Instant expiryDate;
}
