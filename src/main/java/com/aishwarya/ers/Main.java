package com.aishwarya.ers;

import com.aishwarya.ers.controller.UserController;
import com.aishwarya.ers.controller.ReimbursementController;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.ReimbursementService;
import com.aishwarya.ers.service.UserService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();
        ReimbursementRepository reimbursementRepository = new ReimbursementRepository();

        UserService userService = new UserService(userRepository);
        ReimbursementService reimbursementService = new ReimbursementService(reimbursementRepository, userRepository);

        UserController userController = new UserController(userService);
        ReimbursementController reimbursementController = new ReimbursementController(reimbursementService);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(".", Location.EXTERNAL);
        });

        app.post("/api/auth/login", userController::login);
        app.post("/api/auth/register", userController::register);
        app.post("/api/auth/logout", userController::logout);

        app.get("/api/reimbursements/filter", reimbursementController::getFiltered);
        app.get("/api/reimbursements/{id}", reimbursementController::getById);
        app.get("/api/reimbursements", reimbursementController::getAll);
        app.post("/api/reimbursements", reimbursementController::submit);
        app.put("/api/reimbursements/{id}", reimbursementController::updatePending);
        app.put("/api/reimbursements/{id}/approve", reimbursementController::approve);
        app.put("/api/reimbursements/{id}/deny", reimbursementController::deny);
        app.get("/api/reimbursements/{id}/status/{status}", reimbursementController::getByUserIdAndStatus);

        app.start(8080);
    }
}