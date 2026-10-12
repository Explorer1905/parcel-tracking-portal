package com.parcel.controller;

import com.parcel.service.ParcelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/alerts")
public class AlertsServlet extends HttpServlet {
    private final ParcelService service = new ParcelService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("alerts", service.alerts());
        req.getRequestDispatcher("/WEB-INF/views/alerts.jsp").forward(req, resp);
    }
}
