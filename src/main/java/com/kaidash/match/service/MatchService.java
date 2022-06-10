package com.kaidash.match.service;

import com.kaidash.match.entity.Match;
import com.kaidash.match.repository.MatchRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    public void deleteById(Match match){
        matchRepository.delete(match);
    }

    public void saveMatch (Match match){
        matchRepository.save(match);
    }

    public List<Match> findAllByUserId(long id){
        return matchRepository.findAllByUserId(id);
    }
}
