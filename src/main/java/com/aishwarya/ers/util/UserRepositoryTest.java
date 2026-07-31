package com.aishwarya.ers.util;
import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.UserRepository;
public class UserRepositoryTest {
    public static void main(String[] args) {
        UserRepository repository = new UserRepository();
        User user = new User();
        user.setUsername("testuser1");
        user.setPasswordHash("temporary_hash");
        user.setRole(Role.EMPLOYEE);
        user.setDepartment("IT");
        boolean created = repository.createUser(user);
        System.out.println("User created: " + created);
        User foundUser = repository.findByUsername("testuser1");
        if (foundUser != null) {
            System.out.println("Found user: " + foundUser.getUsername());
            System.out.println("Role: " + foundUser.getRole());
            System.out.println("Department: " + foundUser.getDepartment());
        } else {
            System.out.println("User not found.");
        }
    }
}