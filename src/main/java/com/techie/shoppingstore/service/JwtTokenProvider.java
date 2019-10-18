package com.techie.shoppingstore.service;

import com.techie.shoppingstore.exceptions.SpringStoreException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;

@Service
@Slf4j
public class JwtTokenProvider {

    private static final String AUTH_STORE = "ecommerce-auth-store";
    @Value("${keystore.password}")
    private String keyStorePassword;
    @Value("${jwt.expirationTimeMs}")
    private int jwtExpirationTimeInMillis;
    private KeyStore keyStore;

    @PostConstruct
    public void loadKeyStore() {
        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/auth.jks");
            keyStore.load(resourceAsStream, keyStorePassword.toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SpringStoreException("Exception occured while loading keystore");
        }

    }

    String generateToken(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationTimeInMillis);

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(AUTH_STORE, keyStorePassword.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new SpringStoreException("Exception occurred while retrieving public key from keystore");
        }
    }

    private PublicKey getPublicKey() {
        try {
            Certificate certificate = keyStore.getCertificate(AUTH_STORE);
            return certificate.getPublicKey();
        } catch (KeyStoreException e) {
            throw new SpringStoreException("Exception occurred while retrieving public key from keystore");
        }
    }

    String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getPublicKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(getPrivateKey()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature", ex);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token", ex);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.", ex);
        }
        return false;
    }
}
