package com.kaidash.match.service;

import com.kaidash.match.entity.Match;
import com.kaidash.match.repository.MatchRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public void saveMatch (Match match){
        matchRepository.save(match);
    }
}
