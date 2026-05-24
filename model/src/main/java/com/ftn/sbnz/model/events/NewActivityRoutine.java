package com.ftn.sbnz.model.events;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

import java.time.LocalDateTime;

@Role(Role.Type.EVENT)
@Expires("30d")
public class NewActivityRoutine {

    private Long userId;
    private double activityFactor;
    private LocalDateTime timestamp;

    public NewActivityRoutine() {
    }

    public NewActivityRoutine(Long userId, double activityFactor, LocalDateTime timestamp) {
        this.userId = userId;
        this.activityFactor = activityFactor;
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getActivityFactor() {
        return activityFactor;
    }

    public void setActivityFactor(double activityFactor) {
        this.activityFactor = activityFactor;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
