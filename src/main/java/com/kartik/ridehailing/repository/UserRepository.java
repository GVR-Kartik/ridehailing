package com.kartik.ridehailing.repository;

import com.kartik.ridehailing.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(String userId);

    List<User> findAll();
}