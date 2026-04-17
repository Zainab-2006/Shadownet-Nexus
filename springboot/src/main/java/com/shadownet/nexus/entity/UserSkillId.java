package com.shadownet.nexus.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserSkillId implements Serializable {
    private String userId;
    private String category;

    public UserSkillId() {
    }

    public UserSkillId(String userId, String category) {
        this.userId = userId;
        this.category = category;
    }

    // Getters/Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserSkillId that = (UserSkillId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, category);
    }
}
