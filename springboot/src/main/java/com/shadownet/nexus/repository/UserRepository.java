package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    User findByEmailHash(String emailHash);

    User findByUsername(String username);

}