package com.aishwarya.ers.controller;

import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.service.ReimbursementService;
import io.javalin.http.Context;

import java.util.List;

public class ReimbursementController {

    private final ReimbursementService service;

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
        String statusParam = ctx.queryParam("status");
        String department = ctx.queryParam("department");

        ReimbursementStatus status = statusParam != null
                ? ReimbursementStatus.valueOf(statusParam.toUpperCase())
                : null;

        List<Reimbursement> reimbursements = service.getByFilters(status, department);
        ctx.json(reimbursements);
    }

    public void getById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Reimbursement r = service.getById(id);
        ctx.json(r);
    }

    public void getByUserId(Context ctx) {
        String idParam = ctx.pathParamMap().containsKey("id") ? ctx.pathParam("id") : ctx.pathParam("userId");
        int targetUserId = Integer.parseInt(idParam);
        User loggedInUser = ctx.sessionAttribute("currentUser");
        List<Reimbursement> reimbursements = service.getByUserId(targetUserId, loggedInUser);
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
}