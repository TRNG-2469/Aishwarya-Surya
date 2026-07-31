package com.aishwarya.ers.service;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.repository.UserRepository;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // create a new user
    public UserResponseDTO register(User user, String plainPassword) {

        User existing = userRepository.findByUsername(user.getUsername());
        if (existing != null) {
            throw new RuntimeException("Username already taken: " + user.getUsername());
        }

        // scramble the password before saving it
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);

        user.setRole(Role.EMPLOYEE);

        boolean wasCreated = userRepository.createUser(user);
        if (!wasCreated) {
            throw new RuntimeException("Failed to create user: " + user.getUsername());
        }

        return UserResponseDTO.fromUser(user);
    }

    // get one user by their id
    public UserResponseDTO getUserById(int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("No user with id " + id);
        }
        return UserResponseDTO.fromUser(user);
    }

    // get one user by their username
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("No user with username " + username);
        }
        return UserResponseDTO.fromUser(user);
    }

    // get every user
    public List<UserResponseDTO> getAllUsers() {
        List<User> allUsers = userRepository.findAll();

        List<UserResponseDTO> result = new ArrayList<>();
        for (User user : allUsers) {
            result.add(UserResponseDTO.fromUser(user));
        }
        return result;
    }

    // update a user's department, and optionally their password
    public UserResponseDTO updateUser(int id, User updatedFields, String newPlainPassword) {
        User existing = userRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("No user with id " + id);
        }

        existing.setDepartment(updatedFields.getDepartment());

        if (newPlainPassword != null && !newPlainPassword.isBlank()) {
            String hashedPassword = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
            existing.setPasswordHash(hashedPassword);
        }

        boolean wasUpdated = userRepository.update(existing);
        if (!wasUpdated) {
            throw new RuntimeException("No user with id " + id);
        }

        return UserResponseDTO.fromUser(existing);
    }

    // delete a user
    public void deleteUser(int id) {
        boolean wasDeleted = userRepository.delete(id);
        if (!wasDeleted) {
            throw new RuntimeException("No user with id " + id);
        }
    }
}