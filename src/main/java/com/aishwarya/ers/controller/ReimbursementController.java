package com.aishwarya.ers.controller;

import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.ReimbursementService;
import io.javalin.http.Context;

import java.util.List;

public class ReimbursementController {

    private static ReimbursementService service = new ReimbursementService(new ReimbursementRepository(), new UserRepository());

    public ReimbursementController(ReimbursementService service) {
        this.service = service;
    }

    public void submit(Context ctx) {
        Reimbursement payload = ctx.bodyAsClass(Reimbursement.class);
        Reimbursement created = service.submit(payload);
        ctx.status(201);
        ctx.json(created);
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
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));

            List<Reimbursement> reimbursements = service.getByUserId(userId, userId);

            if (reimbursements == null || reimbursements.isEmpty()) {
                ctx.status(404).result("No reimbursements found for user ID " + userId);
                return;
            }

            ctx.json(reimbursements);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid ID format");
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

    public void updatePending(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Reimbursement payload = ctx.bodyAsClass(Reimbursement.class);
        payload.setId(id);
        Reimbursement updated = service.updatePending(payload);
        ctx.json(updated);
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
        User loggedInUser = ctx.sessionAttribute("currentUser");
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
}