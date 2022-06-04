package com.kaidash.match.service;

import com.kaidash.match.entity.User;
import com.kaidash.match.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll (){
        return userRepository.findAll();
    }

    public void saveUser (User user){
        userRepository.save(user);
    }
}
