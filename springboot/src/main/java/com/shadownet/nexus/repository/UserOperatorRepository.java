package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.UserOperator;
import com.shadownet.nexus.entity.UserOperatorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserOperatorRepository extends JpaRepository<UserOperator, UserOperatorId> {

    List<UserOperator> findByUserId(String userId);

    UserOperator findByUserIdAndOperatorId(String userId, String operatorId);

}