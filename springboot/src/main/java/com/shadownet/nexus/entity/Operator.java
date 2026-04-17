package com.shadownet.nexus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "operators")
public class Operator {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String abilities;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer unlockCost = 0;

    @Column(columnDefinition = "TEXT")
    private String backstory;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAbilities() {
        return abilities;
    }

    public void setAbilities(String abilities) {
        this.abilities = abilities;
    }

    public Integer getUnlockCost() {
        return unlockCost;
    }

    public void setUnlockCost(Integer unlockCost) {
        this.unlockCost = unlockCost;
    }

    public String getBackstory() {
        return backstory;
    }

    public void setBackstory(String backstory) {
        this.backstory = backstory;
    }
}