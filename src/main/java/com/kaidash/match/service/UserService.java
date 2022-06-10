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
            System.out.println(temp);
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

    public boolean checkByUserId(long userId){
        return findByUserId(userId) != null;
    }

    public User findByUserId(long userId){
        return userRepository.findByUserId(userId);
    }

    public void updateUser(UserLocal userLocal){
        User user = findByUserId(userLocal.getUserId());
        setUser(userLocal, user);
    }

    private void setUser(UserLocal userLocal, User user) {
        user.setName(userLocal.getName());
        user.setAge(userLocal.getAge());
        user.setSex(userLocal.getSex());
        user.setOppositeSex(userLocal.getOppositeSex());
        user.setCity(userLocal.getCity());
        user.setDescription(userLocal.getDescription());
        user.setOppositeSexId(userLocal.getOppositeSexId());
        user.setUserName(userLocal.getUserName());

        saveUser(user);
    }

    public void saveLocalUsers(UserLocal userLocal) {
        User user = new User();
        user.setUserId(userLocal.getUserId());
        setUser(userLocal, user);
    }

    public void saveUser (User user){
        userRepository.save(user);
    }
}
