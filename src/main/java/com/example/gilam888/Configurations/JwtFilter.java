package com.example.gilam888.Configurations;

import com.example.gilam888.Entity.Users;
import com.example.gilam888.Repository.UsersRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final TokenGenerator tokenGenerator;
    private final UsersRepository usersRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // 1. Authorization: Bearer <token> headerdan olish
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. Agar header yo'q bo'lsa, Cookie'dan olish
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("Auth".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            try {
                boolean valid = tokenGenerator.tokenCheck(token);
                if (valid) {
                    String username = tokenGenerator.usernameolish(token);
                    Optional<Users> userOpt = usersRepository.findByUsername(username);
                    if (userOpt.isPresent()) {
                        Users user = userOpt.get();
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {
                // Token noto'g'ri yoki muddati o'tgan
            }
        }

        filterChain.doFilter(request, response);
    }
}
