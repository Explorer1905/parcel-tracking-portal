package com.parcel.controller;

import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.service.ParcelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Public parcel tracking page – no authentication required.
 * GET /track        → search form
 * GET /track?id=XYZ → shows parcel status, route, priority, events
 */
@WebServlet("/track")
public class TrackServlet extends HttpServlet {
    private final ParcelService service = new ParcelService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id != null && !id.isBlank()) {
            Parcel parcel = service.find(id.trim());
            if (parcel != null) {
                List<ParcelEvent> events = service.events(parcel.id());
                req.setAttribute("parcel", parcel);
                req.setAttribute("events", events);
            } else {
                req.setAttribute("notFound", id.trim());
            }
        }
        req.setAttribute("trackId", id == null ? "" : id.trim());
        req.getRequestDispatcher("/WEB-INF/views/track.jsp").forward(req, resp);
    }
}
