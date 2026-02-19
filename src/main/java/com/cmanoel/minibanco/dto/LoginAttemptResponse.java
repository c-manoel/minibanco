package com.cmanoel.minibanco.dto;

import java.time.OffsetDateTime;

public class LoginAttemptResponse {

    private final OffsetDateTime timestamp;
    private final String email;
    private final String ip;
    private final String status;
    private final String reason;

    public LoginAttemptResponse(
            OffsetDateTime timestamp,
            String email,
            String ip,
            String status,
            String reason) {
        this.timestamp = timestamp;
        this.email = email;
        this.ip = ip;
        this.status = status;
        this.reason = reason;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getEmail() {
        return email;
    }

    public String getIp() {
        return ip;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}
