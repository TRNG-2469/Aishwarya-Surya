package com.aishwarya.ers;

import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.UserService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.fasterxml.jackson.databind.JsonNode;

public class Main {
    public static void main(String[] args) {

        UserRepository repo = new UserRepository();
        UserService userService = new UserService(repo);

        Javalin app = Javalin.create(config -> {
            // "." points to your project root folder on the file system
            config.staticFiles.add(".", Location.EXTERNAL);
        });

        app.post("/", ctx -> {
            JsonNode body = ctx.bodyAsClass(JsonNode.class);

            try {
                String action = body.has("action") ? body.get("action").asText() : "";

                if ("login".equalsIgnoreCase(action)) {
                    String username = body.get("username").asText();
                    String password = body.get("password").asText();

                    UserResponseDTO user = userService.login(username, password);
                    ctx.status(200).json(user);

                } else if ("register".equalsIgnoreCase(action)) {
                    User newUser = new User();
                    newUser.setUsername(body.get("username").asText());
                    if (body.has("department")) newUser.setDepartment(body.get("department").asText());
                    if (body.has("role")) newUser.setRole(Role.valueOf(body.get("role").asText()));

                    String plainPassword = body.get("password").asText();

                    UserResponseDTO createdUser = userService.register(newUser, plainPassword);
                    ctx.status(201).json(createdUser);

                } else {
                    // Triggers the 400 catch block below for invalid actions
                    throw new IllegalArgumentException("Bad Request: Missing or invalid action");
                }

            } catch (IllegalArgumentException | NullPointerException e) {
                // Single place for all 400 errors (missing fields, bad Role enum values, or invalid action)
                ctx.status(400).result(e.getMessage() != null ? e.getMessage() : "Bad Request");

            } catch (RuntimeException e) {
                // Handles authentication failures (401)
                ctx.status(401).result("Invalid credentials");
            }
        });

        app.start(8080);
    }
}