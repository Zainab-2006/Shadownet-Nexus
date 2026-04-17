package com.shadownet.nexus.dto;

public class OperatorDto {
    private String id;
    private String name;
    private String role;
    private String abilities;
    private Integer unlockCost;
    private String backstory;
    private boolean unlocked;
    private boolean selected;
    private String portraitUrl;
    private String fullImageUrl;

    // Constructors
    public OperatorDto() {}

public OperatorDto(String id, String name, String role, String abilities, Integer unlockCost, String backstory, boolean unlocked, boolean selected, String portraitUrl, String fullImageUrl) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.abilities = abilities;
        this.unlockCost = unlockCost;
        this.backstory = backstory;
        this.unlocked = unlocked;
        this.selected = selected;
        this.portraitUrl = portraitUrl;
        this.fullImageUrl = fullImageUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAbilities() { return abilities; }
    public void setAbilities(String abilities) { this.abilities = abilities; }

    public Integer getUnlockCost() { return unlockCost; }
    public void setUnlockCost(Integer unlockCost) { this.unlockCost = unlockCost; }

    public String getBackstory() { return backstory; }
    public void setBackstory(String backstory) { this.backstory = backstory; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getPortraitUrl() { return portraitUrl; }
    public void setPortraitUrl(String portraitUrl) { this.portraitUrl = portraitUrl; }

    public String getFullImageUrl() { return fullImageUrl; }
    public void setFullImageUrl(String fullImageUrl) { this.fullImageUrl = fullImageUrl; }
}

