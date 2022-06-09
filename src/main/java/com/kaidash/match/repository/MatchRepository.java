package com.kaidash.match.repository;

import com.kaidash.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByUserId(long id);
}
