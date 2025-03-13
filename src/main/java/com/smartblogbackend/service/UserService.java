package com.smartblogbackend.service;

import com.smartblogbackend.model.User;
import com.smartblogbackend.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserService() {
    }

    public Optional<User> findUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return (User)this.userRepository.save(user);
    }
}
