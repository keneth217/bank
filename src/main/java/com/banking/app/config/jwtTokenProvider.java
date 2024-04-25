package com.banking.app.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class jwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;
    @Value("${app.jwt-expiration}")
    private long jwtExpirationDate;
    public String generateToken(Authentication authentication){
        String username = authentication.getName();
        System.out.println("this is my username--------------------------"+username);
        Date currentDate=new Date();
        Date expired=new Date(currentDate.getTime()+ jwtExpirationDate);
        System.out.println("token will expire at "+expired);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expired)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key  key(){
        byte[]  key= Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(key);
    }
    public String getUsername(String token){
        Claims claims=Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return  claims.getSubject();
    }
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return  true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

}
