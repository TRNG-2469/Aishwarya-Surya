package com.aishwarya.ers.service;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.repository.UserRepository;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.stream.Collectors;

public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserResponseDTO register(User user, String plainPassword) {
        if (repo.findByUsername(user.getUsername()) != null)
            throw new RuntimeException("Username already taken: " + user.getUsername());
        user.setPasswordHash(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
        if (user.getRole() == Role.MANAGER) {
            user.setRole(Role.MANAGER);
        } else {
            user.setRole(Role.EMPLOYEE);
        }
        if(!repo.createUser(user))
            throw new RuntimeException("Failed to create user: " + user.getUsername());

        return UserResponseDTO.fromUser(user);
    }

    public UserResponseDTO getUserById(int id) {
        User user = repo.findById(id);
        if (user == null) throw new RuntimeException("No user with id " + id);
        return UserResponseDTO.fromUser(user);
    }

    public UserResponseDTO getUserByUsername(String username) {
        User user = repo.findByUsername(username);
        if (user == null) throw new RuntimeException("No user with username " + username);
        return UserResponseDTO.fromUser(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return repo.findAll().stream()
                .map(UserResponseDTO::fromUser)
                .collect(Collectors.toList());
    }

    public Role getRole(int id) {
        User user = repo.findById(id);
        if (user == null) throw new RuntimeException("No user with id " + id);
        return user.getRole();
    }

    public String getDepartment(int id) {
        User user = repo.findById(id);
        if (user == null) throw new RuntimeException("No user with id " + id);
        return user.getDepartment();
    }

    public UserResponseDTO updateUser(int id, User updatedFields, String newPlainPassword) {
        User existing = repo.findById(id);
        if (existing == null) throw new RuntimeException("No user with id " + id);

        existing.setDepartment(updatedFields.getDepartment());

        if (newPlainPassword != null && !newPlainPassword.isBlank())
            existing.setPasswordHash(BCrypt.hashpw(newPlainPassword, BCrypt.gensalt()));

        if (!repo.update(existing)) throw new RuntimeException("No user with id " + id);

        return UserResponseDTO.fromUser(existing);
    }

    public void deleteUser(int id) {
        if (!repo.delete(id)) throw new RuntimeException("No user with id " + id);
    }
}