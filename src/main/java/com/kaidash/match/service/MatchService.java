package com.kaidash.match.service;

import com.kaidash.match.entity.Match;
import com.kaidash.match.repository.MatchRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    public Match findById(long id){
        Optional<Match> matchOptional = matchRepository.findById(id);
        return matchOptional.orElse(null);
    }

    public void saveMatch (Match match){
        matchRepository.save(match);
    }

    public List<Match> findAllByUserId(long id){
        return matchRepository.findAllByUserId(id);
    }

    public void deleteAllByUserId(List<Match> matches){
        matchRepository.deleteAll(matches);
    }
}
