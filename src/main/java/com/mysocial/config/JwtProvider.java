package com.mysocial.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtProvider {
    private SecretKey secretKey = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
    public String generateToken(Authentication authentication){
        String jwt = Jwts.builder()
                .claim("email", authentication.getName())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime()+10000000))
                .signWith(secretKey)
                .compact();
        return jwt;
    }
    public String getEmailFromToken(String jwt){
        if(jwt.startsWith("Bearer")){
            jwt = jwt.substring(7);

        }
        Claims claims = Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(jwt).getBody();
        return String.valueOf(claims.get("email"));
    }

    public SecretKey getSecretKey(){
        return secretKey;
    }
}
