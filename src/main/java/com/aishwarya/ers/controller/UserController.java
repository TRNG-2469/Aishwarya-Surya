package com.aishwarya.ers.controller;

import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.http.Context;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void login(Context ctx) {
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
    }

    public void register(Context ctx) {
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
    }

    public void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.status(200).result("Logged out successfully");
    }
}