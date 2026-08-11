package com.aishwarya.ers.dto;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;

public class UserResponseDTO {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private Role role;

    public UserResponseDTO(int id, String username, String firstName,
                           String lastName, Role role) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public static UserResponseDTO fromUser(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }
}