package com.shadownet.nexus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_skills")
@IdClass(UserSkillId.class)
public class UserSkill {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "category")
    private String category;

    @Column(name = "xp", nullable = false)
    private int xp = 0;

    @Column(name = "level", nullable = false)
    private int level = 1;

    // Constructors
    public UserSkill() {
    }

    public UserSkill(String userId, String category) {
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

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
        this.level = Math.max(1, (xp / 250) + 1);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
