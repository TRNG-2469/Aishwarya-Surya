package com.aishwarya.ers.controller;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.service.UserService;
import io.javalin.http.Context;

import java.util.List;

public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    public void register(Context ctx) {
        RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);

        User user = new User();
        user.setUsername(req.username);
        user.setDepartment(req.department);
        user.setRole(req.role);

        UserResponseDTO created = service.register(user, req.password);
        ctx.status(201);
        ctx.json(created);
    }

    public void getAllUsers(Context ctx) {
        List<UserResponseDTO> users = service.getAllUsers();
        ctx.json(users);
    }

    public void getUserById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        UserResponseDTO user = service.getUserById(id);
        ctx.json(user);
    }

    public void updateUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        UpdateRequest req = ctx.bodyAsClass(UpdateRequest.class);

        User updatedFields = new User();
        updatedFields.setDepartment(req.department);

        UserResponseDTO updated = service.updateUser(id, updatedFields, req.password);
        ctx.json(updated);
    }

    public void deleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        service.deleteUser(id);
        ctx.status(204);
    }

    // Small request-shaping classes so raw passwords never land
    // directly on the User model (which only stores passwordHash).
    public static class RegisterRequest {
        public String username;
        public String password;
        public String department;
        public Role role;
    }

    public static class UpdateRequest {
        public String password;
        public String department;
    }
}