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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.List;

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
                String role = String.valueOf(claims.get("role"));
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (role != null && !role.isEmpty() && !role.equals("null")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.sendRedirect("/login");
            }

        }
        filterChain.doFilter(request, response);
    }
}
