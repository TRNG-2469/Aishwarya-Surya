package com.aishwarya.ers.controller;

import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.ReimbursementService;
import com.aishwarya.ers.exception.ForbiddenException;
import io.javalin.http.Context;

import java.util.List;

public class ReimbursementController {

    private static ReimbursementService service = new ReimbursementService(new ReimbursementRepository(), new UserRepository());

    public ReimbursementController(ReimbursementService service) {
        this.service = service;
    }

    public static void submit(Context ctx) {
        UserResponseDTO loggedInUser = ctx.sessionAttribute("currentUser");

        if (loggedInUser == null) {
            ctx.status(401).result("Not logged in");
            return;
        }

        try {
            Reimbursement payload = ctx.bodyAsClass(Reimbursement.class);

            // Always use the logged-in user's ID
            payload.setUserId(loggedInUser.getId());

            Reimbursement created = service.submit(payload);

            ctx.status(201).json(created);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (RuntimeException e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    public void getFiltered(Context ctx) {
        int callerId = Integer.parseInt(ctx.queryParam("callerId"));
        String statusParam = ctx.queryParam("status");
        String department = ctx.queryParam("department");

        ReimbursementStatus status = statusParam != null
                ? ReimbursementStatus.valueOf(statusParam.toUpperCase())
                : null;

        List<Reimbursement> reimbursements = service.getByFilters(callerId, status, department);
        ctx.json(reimbursements);
    }

    public static void getById(Context ctx) {
        UserResponseDTO loggedInUser = ctx.sessionAttribute("currentUser");

        if (loggedInUser == null) {
            ctx.status(401).result("Not logged in");
            return;
        }

        try {
            int targetUserId = Integer.parseInt(ctx.pathParam("id"));

            List<Reimbursement> reimbursements =
                    service.getByUserId(
                            targetUserId,
                            loggedInUser.getId()
                    );

            ctx.status(200).json(reimbursements);

        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid user ID");
        } catch (ForbiddenException e) {
            ctx.status(403).result(e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    public static void getByUserId(Context ctx) {
        String idParam = ctx.pathParamMap().containsKey("id") ? ctx.pathParam("id") : ctx.pathParam("userId");
        int targetUserId = Integer.parseInt(idParam);
        UserResponseDTO loggedInUser = ctx.sessionAttribute("currentUser");
        if (loggedInUser == null) {
            ctx.status(401).result("Not logged in");
            return;
        }

        List<Reimbursement> reimbursements = service.getByUserId(targetUserId, loggedInUser.getId());
        if (reimbursements.isEmpty()) {
            ctx.status(200).result("No reimbursements found");
            return;
        }
        ctx.json(reimbursements);
    }

    public void getByStatus(Context ctx) {
        ReimbursementStatus status = ReimbursementStatus.valueOf(
                ctx.pathParam("status").toUpperCase()
        );
        List<Reimbursement> reimbursements = service.getByStatus(status);
        ctx.json(reimbursements);
    }

    public static void updatePending(Context ctx) {
        UserResponseDTO loggedInUser = ctx.sessionAttribute("currentUser");

        if (loggedInUser == null) {
            ctx.status(401).result("Not logged in");
            return;
        }

        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            Reimbursement payload = ctx.bodyAsClass(Reimbursement.class);

            payload.setId(id);
            payload.setUserId(loggedInUser.getId());

            Reimbursement updated = service.updatePending(payload);

            ctx.status(200).json(updated);

        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid reimbursement ID");
        } catch (RuntimeException e) {
            ctx.status(400).result(e.getMessage());
        }
    }
    public void approve(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int resolverId = Integer.parseInt(ctx.queryParam("resolverId"));
        service.approve(id, resolverId);
        ctx.status(204);
    }

    public void deny(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int resolverId = Integer.parseInt(ctx.queryParam("resolverId"));
        service.deny(id, resolverId);
        ctx.status(204);
    }

    public static void getAll(Context ctx) {
        UserResponseDTO loggedInUser = ctx.sessionAttribute("currentUser");
        if (loggedInUser == null) {
            ctx.status(401).result("Not logged in");
            return;
        }
        if (loggedInUser.getRole() != Role.MANAGER) {
            ctx.status(403).result("Access Denied: You are not a manager.");
            return;
        }

        List<Reimbursement> reimbursements = service.getAll();
        if (reimbursements.isEmpty()) {
            ctx.status(200).result("No reimbursements found");
            return;
        }
        ctx.json(reimbursements);
    }
    public static void getByUserIdAndStatus(Context ctx) {
        UserResponseDTO loggedInUser = ctx.sessionAttribute("currentUser");

        if (loggedInUser == null) {
            ctx.status(401).result("Not logged in");
            return;
        }

        try {
            int targetUserId = Integer.parseInt(ctx.pathParam("id"));

            ReimbursementStatus status = ReimbursementStatus.valueOf(
                    ctx.pathParam("status").toUpperCase()
            );

            List<Reimbursement> reimbursements =
                    service.getByUserIdAndStatus(
                            targetUserId,
                            loggedInUser.getId(),
                            status
                    );

            ctx.json(reimbursements);

        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid user ID");
        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Invalid reimbursement status");
        } catch (ForbiddenException e) {
            ctx.status(403).result(e.getMessage());
        }
    }
}