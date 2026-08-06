package com.aishwarya.ers.dto;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;

public class UserResponseDTO {
    private int id;
    private String username;
    private String department;
    private Role role;

    public UserResponseDTO() {

    }

    public UserResponseDTO(int id, String username, String department, Role role) {
        this.id = id;
        this.username = username;
        this.department = department;
        this.role = role;
    }


    public static UserResponseDTO fromUser(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getDepartment(),
                user.getRole()
        );
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDepartment() {
        return department;
    }

    public Role getRole() {
        return role;
    }
}