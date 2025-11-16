package com.pm.taskapp.users.service;

import com.pm.taskapp.users.dto.UserRequest;
import com.pm.taskapp.users.entity.User;

import java.util.List;

public interface UserService {
    User createUser(UserRequest userRequest);
    User getUserByEmail(String email);
    User getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id,UserRequest user);
    void deleteUser(Long id);
    List<User> searchUsers(String term);
}
