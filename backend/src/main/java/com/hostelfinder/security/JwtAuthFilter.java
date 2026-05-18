package com.hostelfinder.security;

import com.hostelfinder.entity.User;
import com.hostelfinder.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtTokenService jwtTokenService;
  private final UserRepository userRepository;

  public JwtAuthFilter(JwtTokenService jwtTokenService, UserRepository userRepository) {
    this.jwtTokenService = jwtTokenService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String auth = request.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring("Bearer ".length()).trim();
      try {
        JwtUser ju = jwtTokenService.parse(token);
        User u = userRepository.findById(ju.userId()).orElse(null);
        if (u != null && "ACTIVE".equalsIgnoreCase(u.getStatus())) {
          List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()));
          var authentication = new UsernamePasswordAuthenticationToken(u, null, auths);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception e) {
        System.err.println("JWT verification failed: " + e.getMessage());
        // Invalid token -> treat as unauthenticated
      }
    }
    filterChain.doFilter(request, response);
  }
}
