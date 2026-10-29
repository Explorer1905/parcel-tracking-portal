package com.parcel.controller;

import com.parcel.model.DashboardSummary;
import com.parcel.service.ParcelService;
import com.parcel.util.Json;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * GET /api/summary — returns the dashboard summary counts as JSON.
 * Publicly accessible (no authentication required).
 */
@WebServlet("/api/summary")
public class ApiSummaryServlet extends HttpServlet {
    private final ParcelService service = new ParcelService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        DashboardSummary s = service.summary();
        resp.getWriter().print(
                "{\"total\":" + s.total()
                + ",\"inTransit\":" + s.inTransit()
                + ",\"delivered\":" + s.delivered()
                + ",\"delayed\":" + s.delayed() + "}");
    }
}
