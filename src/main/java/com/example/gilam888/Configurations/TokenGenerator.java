package com.example.gilam888.Configurations;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class TokenGenerator {
    private String parol = "jn&*gbc%";

    public String token(String username) {
        long vaqt = 2 * 60 * 60 * 1000;
        Date muddat = new Date(System.currentTimeMillis() + vaqt);

        String base64EncodedParol = Base64.getEncoder().encodeToString(parol.getBytes());

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(muddat)
                .signWith(SignatureAlgorithm.HS256, base64EncodedParol)
                .compact();
        return token;
    }
    public boolean tokenCheck(String token){
        Jwts
                .parser()
                .setSigningKey(parol)
                .parseClaimsJws(token);
        return true;
    }
    public String usernameolish(String token){
        String subject = Jwts
                .parser()
                .setSigningKey(Base64.getEncoder().encodeToString(parol.getBytes()))
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return subject;
    }
}
