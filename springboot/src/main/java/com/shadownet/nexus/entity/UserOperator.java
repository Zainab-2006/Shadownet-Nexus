package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_operators")
@IdClass(UserOperatorId.class)
public class UserOperator {

    @Id
    private String userId;

    @Id
    private String operatorId;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Boolean selected = false;

    // Getters and setters

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

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}