package com.aishwarya.ers;

import com.aishwarya.ers.controller.ReimbursementController;
import com.aishwarya.ers.controller.UserController;
import com.aishwarya.ers.exception.ErrorResponse;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.ReimbursementService;
import com.aishwarya.ers.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

public class Main {

    public static void main(String[] args) {

        UserRepository userRepo = new UserRepository();
        ReimbursementRepository reimbursementRepo = new ReimbursementRepository();

        UserService userService = new UserService(userRepo);
        ReimbursementService reimbursementService = new ReimbursementService(reimbursementRepo, userRepo);

        UserController userController = new UserController(userService);
        ReimbursementController reimbursementController = new ReimbursementController(reimbursementService);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, true));
        }).start(8080);

        app.get("/", ctx -> ctx.result("ERS API is running"));

        app.post("/api/users/register", userController::register);
        app.get("/api/users", userController::getAllUsers);
        app.get("/api/users/{id}", userController::getUserById);
        app.put("/api/users/{id}", userController::updateUser);
        app.delete("/api/users/{id}", userController::deleteUser);

        app.post("/api/reimbursements", reimbursementController::submit);
        app.get("/api/reimbursements", reimbursementController::getAll);
        app.get("/api/reimbursements/{id}", reimbursementController::getById);
        app.get("/api/reimbursements/user/{userId}", reimbursementController::getByUserId);
        app.get("/api/reimbursements/status/{status}", reimbursementController::getByStatus);
        app.put("/api/reimbursements/{id}", reimbursementController::updatePending);
        app.patch("/api/reimbursements/{id}/approve", reimbursementController::approve);
        app.patch("/api/reimbursements/{id}/deny", reimbursementController::deny);

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(400);
            ctx.json(new ErrorResponse(
                    e.getMessage() != null ? e.getMessage() : "Invalid request."
            ));
        });

        app.exception(RuntimeException.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(400);
            ctx.json(new ErrorResponse(
                    e.getMessage() != null ? e.getMessage() : "Request could not be processed."
            ));
        });

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(new ErrorResponse("An unexpected server error occurred."));
        });
    }
}