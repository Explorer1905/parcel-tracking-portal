package com.parcel.controller;

import com.parcel.service.ParcelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/parcels")
public class ParcelServlet extends HttpServlet {
    private final ParcelService service = new ParcelService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        show(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String id = req.getParameter("parcelId");
        try {
            service.addParcel(
                    id,
                    req.getParameter("sender"),
                    req.getParameter("receiver"),
                    req.getParameter("origin"),
                    req.getParameter("destination"),
                    req.getParameter("weight"),           // optional
                    req.getParameter("priority"),         // optional
                    req.getParameter("expectedDelivery"), // optional
                    req.getParameter("agent")             // optional
            );
            resp.sendRedirect("parcels?added=" + URLEncoder.encode(
                    id == null ? "" : id.trim(), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            show(req, resp);
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("parcels", service.listParcels());
        req.getRequestDispatcher("/WEB-INF/views/parcels.jsp").forward(req, resp);
    }
}