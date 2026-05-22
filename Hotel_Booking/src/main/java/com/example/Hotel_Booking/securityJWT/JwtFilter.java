package com.example.Hotel_Booking.securityJWT;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🔓 Public endpoints skip
        if (path.startsWith("/api/auth")
                || ("GET".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/hotels"))
                || ("GET".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/rooms"))
                || ("GET".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/reviews"))
                || ("POST".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/contact"))
                || ("GET".equalsIgnoreCase(request.getMethod()) && path.equals("/api/bookings/available"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {

                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractRole(token);

                    // 🔥 IMPORTANT FIX (email ko request me set karo)
                    request.setAttribute("email", email);

                    if (role == null || role.isEmpty()) {
                        System.out.println("❌ Role missing in token");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // ROLE format fix
                    role = role.toUpperCase();
                    role = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                    System.out.println("AUTH USER: " + email);
                    System.out.println("AUTH ROLE: " + role);

                    // Authority set
                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority(role);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    List.of(authority)
                            );

                    // 🔐 Set authentication
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception e) {
                System.out.println("❌ JWT ERROR: " + e.getMessage());
            }
        } else {
            System.out.println("❌ No Authorization Header");
        }

        filterChain.doFilter(request, response);
    }
}
