package com.kaidash.match.service;

import com.kaidash.match.entity.User;
import com.kaidash.match.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getFirstUser(){
        return userRepository.findTopByOrderByIdAsc();
    }

    public User getLastUser(){
        return userRepository.findTopByOrderByIdDesc();
    }

    public Long nextId(){
        Long temp;

        do {
            temp = userRepository.getNextSeriesId();
        }while (findById(temp) == null);

        return temp;
    }

    public void resetId(long id){
        userRepository.resetSeries(id);
    }

    public User findById(long id){
        Optional<User> userOptional =  userRepository.findById(id);
        return userOptional.orElse(null);
    }

    public User findByUserId(long userId){
        return userRepository.findByUserId(userId);
    }

    public void updateUser(User user){
        User user1 = findByUserId(user.getUserId());
        user1.setName(user.getName());
        user1.setNickname(user.getNickname());
        user1.setAge(user.getAge());
        user1.setSex(user.getSex());
        user1.setOppositeSex(user.getOppositeSex());
        user1.setCity(user.getCity());
        user1.setDescription(user.getDescription());
        user1.setOppositeSexId(user.getOppositeSexId());
        user1.setChatStatus(user.getChatStatus());
        saveUser(user1);
    }

    public void saveUser (User user){
        userRepository.save(user);
    }
}
