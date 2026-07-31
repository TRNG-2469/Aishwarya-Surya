package com.aishwarya.ers.util;

import com.aishwarya.ers.model.User;
import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.UserService;

import java.util.List;

public class UserServiceTest {

    public static void main(String[] args) {

        UserService userService = new UserService(new UserRepository());

        User newUser = new User();
        newUser.setUsername("Bob");
        newUser.setDepartment("HR");

        UserResponseDTO registered = userService.register(newUser, "password123");
        System.out.println("Registered user: " + registered.getUsername());

        UserResponseDTO byId = userService.getUserById(registered.getId());
        System.out.println("Found by id: " + byId.getUsername());

        UserResponseDTO byUsername = userService.getUserByUsername("testuser2");
        System.out.println("Found by username: " + byUsername.getUsername());

        List<UserResponseDTO> all = userService.getAllUsers();
        System.out.println("Total users: " + all.size());

        User update = new User();
        update.setDepartment("Finance");
        UserResponseDTO updated = userService.updateUser(registered.getId(), update, null);
        System.out.println("Updated department: " + update.getDepartment());

        userService.deleteUser(registered.getId());
        System.out.println("User deleted.");

        try {
            userService.getUserById(registered.getId());
            System.out.println("ERROR: user still exists after delete");
        } catch (RuntimeException e) {
            System.out.println("Confirmed deletion: " + e.getMessage());
        }
    }
}