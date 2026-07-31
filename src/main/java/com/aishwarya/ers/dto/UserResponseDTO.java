package com.aishwarya.ers.dto;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;

public class UserResponseDTO {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;

    public UserResponseDTO() {}

    public UserResponseDTO(int id, String username, String firstName,
                           String lastName, String email, Role role) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public UserResponseDTO(int id, String username, Role role) {
    }

    public static UserResponseDTO fromUser(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}