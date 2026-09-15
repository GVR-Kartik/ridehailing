package com.kartik.ridehailing.service;

import com.kartik.ridehailing.model.User;
import com.kartik.ridehailing.repository.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String userId, String name) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }

        if (userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException("User already exists: " + userId);
        }

        User user = new User(userId, name);
        userRepository.save(user);

        return user;
    }

    public User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}