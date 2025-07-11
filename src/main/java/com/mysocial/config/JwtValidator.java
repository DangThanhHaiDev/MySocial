package com.mysocial.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtValidator extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);
        if(jwt != null) {
            jwt = jwt.substring(7);
            String email = null;
            try {
                Claims claims = Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes())).build().parseClaimsJws(jwt).getBody();
                email = String.valueOf(claims.get("email"));
                if (email != "" || email.isEmpty()) {
                }
                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.sendRedirect("/login");
            }

        }
        filterChain.doFilter(request, response);
    }
}
