package com.shadownet.nexus.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserOperatorId implements Serializable {

    private String userId;
    private String operatorId;

    public UserOperatorId() {
    }

    public UserOperatorId(String userId, String operatorId) {
        this.userId = userId;
        this.operatorId = operatorId;
    }

    // Getters, setters, equals, hashCode

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserOperatorId that = (UserOperatorId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(operatorId, that.operatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, operatorId);
    }
}