package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, String> {

}