package com.aishwarya.ers;

import com.aishwarya.ers.controller.ReimbursementController;
import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.UserService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.fasterxml.jackson.databind.JsonNode;

import static com.aishwarya.ers.Auth.Authenticate;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(".", Location.EXTERNAL);
        });

        Authenticate(app);
        app.start(8080);

    }
}