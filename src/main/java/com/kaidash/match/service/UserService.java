package com.kaidash.match.service;

import com.kaidash.match.entity.User;
import com.kaidash.match.local.UserLocal;
import com.kaidash.match.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public Optional<User> findById(long id){
        return userRepository.findById(id);
    }

    public void saveLocalUsers(UserLocal data) {
        User user = new User();
        user.setUserId(data.getUserId());
        user.setName(data.getName());
        user.setAge(data.getAge());
        user.setSex(data.getSex());
        user.setOppositeSex(data.getOppositeSex());
        user.setCity(data.getCity());
        user.setDescription(data.getDescription());

        userRepository.save(user);
    }

    public void saveUser (User user){

        userRepository.save(user);
    }
}
