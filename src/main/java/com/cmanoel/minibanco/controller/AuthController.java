package com.cmanoel.minibanco.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cmanoel.minibanco.dto.LoginAttemptResponse;
import com.cmanoel.minibanco.dto.LoginRequest;
import com.cmanoel.minibanco.dto.RefreshTokenRequest;
import com.cmanoel.minibanco.dto.TokenResponse;
import com.cmanoel.minibanco.exception.CredenciaisInvalidasException;
import com.cmanoel.minibanco.security.JwtUtil;
import com.cmanoel.minibanco.security.LoginSecurityService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginSecurityService loginSecurityService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          LoginSecurityService loginSecurityService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.loginSecurityService = loginSecurityService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest servletRequest) {
        String ip = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");
        loginSecurityService.assertCanAttempt(request.getEmail(), ip, userAgent);

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.gerarAccessToken(userDetails.getUsername());
            String refreshToken = jwtUtil.gerarRefreshToken(userDetails.getUsername());

            loginSecurityService.recordSuccess(request.getEmail(), ip, userAgent);
            return ResponseEntity.ok(new TokenResponse(
                accessToken,
                refreshToken,
                jwtUtil.extrairExpiracao(accessToken),
                jwtUtil.extrairExpiracao(refreshToken)
            ));
        } catch (BadCredentialsException ex) {
            loginSecurityService.recordFailure(request.getEmail(), ip, userAgent, "Credenciais invalidas");
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new CredenciaisInvalidasException("Refresh token inválido");
            }

            String email = jwtUtil.extrairEmail(refreshToken);
            String newAccessToken = jwtUtil.gerarAccessToken(email);
            String newRefreshToken = jwtUtil.gerarRefreshToken(email);

            return ResponseEntity.ok(new TokenResponse(
                newAccessToken,
                newRefreshToken,
                jwtUtil.extrairExpiracao(newAccessToken),
                jwtUtil.extrairExpiracao(newRefreshToken)
            ));
        } catch (CredenciaisInvalidasException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CredenciaisInvalidasException("Refresh token inválido");
        }
    }

    @GetMapping("/login-attempts")
    public ResponseEntity<List<LoginAttemptResponse>> loginAttempts(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(loginSecurityService.findRecentByEmail(email, 30));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
