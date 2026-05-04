package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.TeamSession;
import com.shadownet.nexus.repository.TeamSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccusationService {

    @Autowired
    private TeamSessionRepository teamSessionRepository;

}
