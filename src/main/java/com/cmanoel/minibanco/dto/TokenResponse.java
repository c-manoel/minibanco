package com.cmanoel.minibanco.dto;

public class TokenResponse {

    private final String token;
    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpiresAt;
    private final long refreshTokenExpiresAt;
    private final String tokenType;

    public TokenResponse(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresAt,
            long refreshTokenExpiresAt) {
        this.token = accessToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.tokenType = "Bearer";
    }

    public String getToken() {
        return token;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public long getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public String getTokenType() {
        return tokenType;
    }
}
