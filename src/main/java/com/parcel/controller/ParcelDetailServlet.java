package com.parcel.controller;

import com.parcel.model.Parcel;
import com.parcel.service.ParcelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/parcel")
public class ParcelDetailServlet extends HttpServlet {
    private final ParcelService service = new ParcelService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Parcel parcel = service.find(req.getParameter("id"));
        if (parcel == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Parcel not found");
            return;
        }
        req.setAttribute("parcel", parcel);
        req.setAttribute("events", service.events(parcel.id()));
        req.getRequestDispatcher("/WEB-INF/views/parcel.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            service.addEvent(req.getParameter("parcelId"), req.getParameter("eventStatus"),
                    req.getParameter("eventLocation"), req.getParameter("eventTime"),
                    req.getParameter("remarks"));
            resp.sendRedirect("parcel?id=" + req.getParameter("parcelId"));
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }
}
