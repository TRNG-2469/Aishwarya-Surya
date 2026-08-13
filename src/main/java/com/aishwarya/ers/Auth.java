package com.aishwarya.ers;

import com.aishwarya.ers.controller.ReimbursementController;
import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Javalin;

public class Auth {


    public static void Authenticate(Javalin app){
        UserRepository repo = new UserRepository();
        UserService userService = new UserService(repo);

        app.post("/api/auth/login", ctx -> {
            JsonNode body = ctx.bodyAsClass(JsonNode.class);

            if (body == null || !body.has("username") || !body.has("password")) {
                ctx.status(400).result("Bad Request: Username and password are required");
                return;
            }

            try {
                String username = body.get("username").asText();
                String password = body.get("password").asText();

                UserResponseDTO user = userService.login(username, password);

                ctx.sessionAttribute("currentUser", user);
                ctx.status(200).json(user);

            } catch (RuntimeException e) {
                ctx.status(401).result("Invalid credentials");
            }
        });

        app.post("/api/auth/register", ctx -> {
            JsonNode body = ctx.bodyAsClass(JsonNode.class);

            if (body == null || !body.has("username") || !body.has("password")) {
                ctx.status(400).result("Bad Request: Username and password are required");
                return;
            }

            try {
                User newUser = new User();

                newUser.setUsername(body.get("username").asText());

                if (body.has("department")) {
                    newUser.setDepartment(body.get("department").asText());
                }

                String plainPassword = body.get("password").asText();

                UserResponseDTO createdUser =
                        userService.register(newUser, plainPassword);

                ctx.status(201).json(createdUser);

            } catch (IllegalArgumentException e) {
                ctx.status(400).result(e.getMessage());
            }
        });

        app.get(
                "/api/reimbursements/filter",
                ReimbursementController::getFiltered
        );

        app.get("/api/reimbursements/{id}", ReimbursementController::getById);
        app.get("/api/reimbursements", ReimbursementController::getAll);
        app.post("/api/reimbursements", ReimbursementController::submit);

        app.put(
                "/api/reimbursements/{id}",
                ReimbursementController::updatePending
        );

        app.put(
                "/api/reimbursements/{id}/approve",
                ReimbursementController::approve
        );

        app.put(
                "/api/reimbursements/{id}/deny",
                ReimbursementController::deny
        );

        app.get(
                "/api/reimbursements/{id}/status/{status}",
                ReimbursementController::getByUserIdAndStatus
        );

        app.post("/api/auth/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.status(200).result("Logged out successfully");
        });
    }
}
