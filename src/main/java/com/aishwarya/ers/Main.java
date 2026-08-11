package com.aishwarya.ers;

import com.aishwarya.ers.controller.*;
import com.aishwarya.ers.exception.ErrorResponse;
import com.aishwarya.ers.model.*;
import com.aishwarya.ers.repository.*;
import com.aishwarya.ers.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        ReimbursementRepository reimbursementRepo = new ReimbursementRepository();
        UserService userService = new UserService(userRepo);
        ReimbursementService reimbursementService = new ReimbursementService(reimbursementRepo, userRepo);
        UserController userController = new UserController(userService);
        ReimbursementController reimbursementController = new ReimbursementController(reimbursementService);

        // 1. Launch web server first
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Javalin app = Javalin.create(config -> config.jsonMapper(new JavalinJackson(mapper, true))).start(8080);

        app.get("/", ctx -> ctx.result("ERS API is running"));

        app.post("/api/users/register", userController::register);
        app.get("/api/users", userController::getAllUsers);
        app.get("/api/users/{id}", userController::getUserById);
        app.put("/api/users/{id}", userController::updateUser);
        app.delete("/api/users/{id}", userController::deleteUser);

        app.post("/api/reimbursements", reimbursementController::submit);
        app.get("/api/reimbursements", reimbursementController::getFiltered);
        app.get("/api/reimbursements/{id}", reimbursementController::getById);
        app.get("/api/reimbursements/user/{userId}", reimbursementController::getByUserId);
        app.get("/api/reimbursements/status/{status}", reimbursementController::getByStatus);
        app.put("/api/reimbursements/{id}", reimbursementController::updatePending);
        app.patch("/api/reimbursements/{id}/approve", reimbursementController::approve);
        app.patch("/api/reimbursements/{id}/deny", reimbursementController::deny);


        app.exception(UserNotFoundException.class, (e, ctx) -> {
            ctx.status(404);
            ctx.json(new ErrorResponse(e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            if (e instanceof RuntimeException) {
                ctx.status(400);
                String msg = e.getMessage() != null ? e.getMessage() : "Invalid request.";
                ctx.json(new ErrorResponse(msg));
            } else {
                ctx.status(500);
                ctx.json(new ErrorResponse("An unexpected server error occurred."));
            }
        });

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Please enter your username:");
            String username = scanner.nextLine();

            System.out.println("Please enter your password:");
            String rawPassword = scanner.nextLine();

            Optional<User> existingUserOpt = Optional.ofNullable(userRepo.findByUsername(username));

            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                if (BCrypt.checkpw(rawPassword, existingUser.getPasswordHash())) {
                    System.out.println("Login successful! Welcome back, " + existingUser.getUsername() + ".");
                    System.out.println("Your role is: " + existingUser.getRole());
                    if (existingUser.getDepartment() != null) {
                        System.out.println("Department: " + existingUser.getDepartment());
                    }
                } else {
                    System.out.println("Invalid credentials. Password does not match.");
                }
            } else {
                System.out.println("No account found. Registering new user...");
                Role role = Role.EMPLOYEE;
                System.out.println("Your default role is Employee. Press 'M' to change to Manager, or press Enter to continue.");
                if (scanner.nextLine().equalsIgnoreCase("M")) {
                    role = Role.MANAGER;
                    System.out.println("You are assigned as a Manager.");
                } else {
                    System.out.println("You are assigned as an Employee.");
                }

                User user = new User();
                user.setUsername(username);
                user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
                user.setRole(role);

                System.out.println("Please enter your department:");
                user.setDepartment(scanner.nextLine());
                userRepo.createUser(user);

                System.out.println("User registered and saved successfully with role: "
                        + user.getRole() + " and department: " + user.getDepartment());
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}