package com.parcel.controller;

import com.parcel.model.Parcel;
import com.parcel.service.ParcelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final ParcelService service = new ParcelService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("query", value(req.getParameter("q")));
        req.setAttribute("status", value(req.getParameter("status")));
        req.setAttribute("parcels", service.search(req.getParameter("q")));
        req.setAttribute("summary", service.summary());
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            service.addEvent(req.getParameter("parcelId"), req.getParameter("eventStatus"),
                    req.getParameter("eventLocation"), req.getParameter("eventTime"),
                    req.getParameter("remarks"));
            resp.sendRedirect("parcel?id=" + URLEncoder.encode(req.getParameter("parcelId").trim(), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
