package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleRepository extends JpaRepository<Puzzle, String> {

}