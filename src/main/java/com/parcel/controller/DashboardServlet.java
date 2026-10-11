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
        String selectedId = req.getParameter("parcel");
        Parcel selected = service.find(selectedId);
        req.setAttribute("query", value(req.getParameter("q")));
        req.setAttribute("parcels", service.search(req.getParameter("q")));
        req.setAttribute("summary", service.summary());
        req.setAttribute("selected", selected);
        req.setAttribute("events", selected == null ? java.util.List.of() : service.events(selected.id()));
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            service.addEvent(req.getParameter("parcelId"), req.getParameter("status"),
                    req.getParameter("location"), req.getParameter("remarks"));
            String id = req.getParameter("parcelId").trim();
            resp.sendRedirect("dashboard?parcel=" + URLEncoder.encode(id, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
