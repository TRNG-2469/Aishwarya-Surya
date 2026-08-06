package com.aishwarya.ers.util;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

public class UserRepositoryTest {
    public static void main(String[] args) {
        UserRepository repository = new UserRepository();
        User user = new User();
        user.setUsername("ScaryFinalEvilBoss");

        String rawPassword = "ssreht56ut4hrt44!";
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));

        user.setRole(Role.MANAGER);
        user.setDepartment("Help Desk");

        boolean created = repository.createUser(user);
        System.out.println("User created: " + created);

        User foundUser = repository.findByUsername("ScaryFinalEvilBoss");
        if (foundUser != null) {
            System.out.println("Found user: " + foundUser.getUsername());
            System.out.println("Role: " + foundUser.getRole());
            System.out.println("Department: " + foundUser.getDepartment());

            boolean verifies = BCrypt.checkpw(rawPassword, foundUser.getPasswordHash());
            boolean wrongPasswordFails = !BCrypt.checkpw("WrongPassword!", foundUser.getPasswordHash());
            System.out.println("Password verifies correctly: " + verifies);
            System.out.println("Wrong password rejected: " + wrongPasswordFails);
        } else {
            System.out.println("User not found.");
        }
    }
}