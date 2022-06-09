package com.kaidash.match.service;

import com.kaidash.match.entity.User;
import com.kaidash.match.local.UserLocal;
import com.kaidash.match.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public long countUsers(){
        return userRepository.count();
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
