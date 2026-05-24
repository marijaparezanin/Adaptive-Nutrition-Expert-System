package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.AnalyticStatusType;
import com.ftn.sbnz.model.enums.SeverityLevel;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

import java.time.LocalDateTime;

@Role(Role.Type.EVENT)
@Expires("7d")
public class AnalyticStatus {

    private AnalyticStatusType type;

    private SeverityLevel severity;

    private LocalDateTime expiresAt;

    private Long userId;

    public AnalyticStatus() {
    }

    public AnalyticStatus(AnalyticStatusType type,
                          SeverityLevel severity,
                          LocalDateTime expiresAt,
                          Long userId) {

        this.type = type;
        this.severity = severity;
        this.expiresAt = expiresAt;
        this.userId = userId;
    }

    public AnalyticStatusType getType() {
        return type;
    }

    public void setType(AnalyticStatusType type) {
        this.type = type;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}