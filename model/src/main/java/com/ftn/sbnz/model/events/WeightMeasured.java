package com.ftn.sbnz.model.events;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

import java.time.LocalDateTime;

@Role(Role.Type.EVENT)
@Expires("90d")
public class WeightMeasured {

    private Long userId;
    private double weight;
    private LocalDateTime timestamp;

    public WeightMeasured() {
    }

    public WeightMeasured(Long userId, double weight, LocalDateTime timestamp) {
        this.userId = userId;
        this.weight = weight;
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
