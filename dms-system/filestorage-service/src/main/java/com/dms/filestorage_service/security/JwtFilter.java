package com.dms.filestorage_service.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;

// @Component
// public class JwtFilter extends OncePerRequestFilter {

//     @Value("${jwt.secret}")
//     private String secretKey;

//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain)
//             throws ServletException, IOException {

//         String authHeader = request.getHeader("Authorization");

//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//             String token = authHeader.substring(7);
//             try {
//                 Claims claims = Jwts.parserBuilder()
//                         .setSigningKey(secretKey.getBytes())
//                         .build()
//                         .parseClaimsJws(token)
//                         .getBody();
//                 request.setAttribute("userId", claims.getSubject());
//             } catch (Exception e) {
//                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                 response.getWriter().write("Invalid JWT Token");
//                 return;
//             }
//         } else {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.getWriter().write("Missing JWT Token");
//             return;
//         }

//         filterChain.doFilter(request, response);
//     }
// }



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class JwtFilter extends OncePerRequestFilter {

    private final String secret = "MySuperSecretKeyForJWTGeneration12345"; // same as auth-service

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Key key = Keys.hmacShaKeyFor(secret.getBytes());
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            } catch (JwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization Header");
            return;
        }
        filterChain.doFilter(request, response);
    }
}