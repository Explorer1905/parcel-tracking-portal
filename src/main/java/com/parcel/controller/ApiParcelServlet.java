package com.parcel.controller;

import com.parcel.model.DashboardSummary;
import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.service.ParcelService;
import com.parcel.util.Json;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * REST API for parcels:
 *
 *   GET  /api/parcels?q=&status=&page=&size=   paginated list with total count
 *   GET  /api/parcels/{id}                     single parcel with events
 *   POST /api/parcels                          create parcel (JSON body), 201 or 400
 *   POST /api/parcels/{id}/events              add event
 */
@WebServlet("/api/parcels/*")
public class ApiParcelServlet extends HttpServlet {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private final ParcelService service = new ParcelService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // null or "/id" or "/id/events"
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/parcels?q=&status=&page=&size=
            String q      = nvl(req.getParameter("q"));
            String status = nvl(req.getParameter("status"));
            int page      = intParam(req.getParameter("page"), 1);
            int size      = intParam(req.getParameter("size"), DEFAULT_PAGE_SIZE);
            if (size < 1 || size > 100) size = DEFAULT_PAGE_SIZE;
            if (page < 1) page = 1;
            int total  = service.countFind(q);
            int offset = (page - 1) * size;
            List<Parcel> parcels = service.findPage(q, offset, size);
            // Filter by status in-memory (small pages, acceptable)
            if (!status.isBlank()) {
                parcels = parcels.stream().filter(p -> status.equals(p.status())).toList();
            }
            PrintWriter out = resp.getWriter();
            out.print("{\"total\":" + total + ",\"page\":" + page + ",\"size\":" + size
                    + ",\"parcels\":[");
            for (int i = 0; i < parcels.size(); i++) {
                if (i > 0) out.print(",");
                out.print(parcelJson(parcels.get(i)));
            }
            out.print("]}");
        } else {
            // GET /api/parcels/{id}
            String[] parts = pathInfo.split("/");
            if (parts.length < 2 || parts[1].isBlank()) {
                resp.sendError(400, "Missing parcel id in path");
                return;
            }
            String id = parts[1];
            Parcel p = service.find(id);
            if (p == null) {
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Parcel " + Json.esc(id) + " not found\"}");
                return;
            }
            List<ParcelEvent> events = service.events(p.id());
            PrintWriter out = resp.getWriter();
            out.print(parcelWithEventsJson(p, events));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        // POST /api/parcels/{id}/events
        if (pathInfo != null && pathInfo.matches("/.+/events")) {
            String id = pathInfo.split("/")[1];
            String body = req.getReader().lines()
                    .collect(java.util.stream.Collectors.joining());
            // Parse simple JSON fields
            String status   = jsonField(body, "status");
            String location = jsonField(body, "location");
            String eventTime = jsonField(body, "eventTime");
            String remarks  = jsonField(body, "remarks");
            try {
                service.addEvent(id, status, location, eventTime, remarks);
                resp.setStatus(201);
                resp.getWriter().print("{\"message\":\"Event added\"}");
            } catch (IllegalArgumentException e) {
                resp.setStatus(400);
                resp.getWriter().print("{\"error\":" + Json.esc(e.getMessage()) + "}");
            }
            return;
        }

        // POST /api/parcels
        req.setCharacterEncoding("UTF-8");
        String body = req.getReader().lines()
                .collect(java.util.stream.Collectors.joining());
        String id          = jsonField(body, "id");
        String sender      = jsonField(body, "sender");
        String receiver    = jsonField(body, "receiver");
        String origin      = jsonField(body, "origin");
        String destination = jsonField(body, "destination");
        String weight      = jsonField(body, "weight");
        String priority    = jsonField(body, "priority");
        String expDelivery = jsonField(body, "expectedDelivery");
        String agent       = jsonField(body, "assignedAgent");
        try {
            service.addParcel(id, sender, receiver, origin, destination,
                    weight, priority, expDelivery, agent);
            resp.setStatus(201);
            Parcel created = service.find(id.trim());
            resp.getWriter().print(parcelJson(created));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":" + Json.esc(e.getMessage()) + "}");
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static String parcelJson(Parcel p) {
        return "{"
                + "\"id\":"              + Json.val(p.id())              + ","
                + "\"sender\":"          + Json.val(p.sender())          + ","
                + "\"receiver\":"        + Json.val(p.receiver())        + ","
                + "\"origin\":"          + Json.val(p.origin())          + ","
                + "\"destination\":"     + Json.val(p.destination())     + ","
                + "\"status\":"          + Json.val(p.status())          + ","
                + "\"priority\":"        + Json.val(p.priority())        + ","
                + "\"weightKg\":"        + p.weightKg()                  + ","
                + "\"expectedDelivery\":" + Json.val(p.expectedDelivery()) + ","
                + "\"assignedAgent\":"   + Json.val(p.assignedAgent())   + ","
                + "\"createdAt\":"       + Json.val(p.createdAt())
                + "}";
    }

    private static String parcelWithEventsJson(Parcel p, List<ParcelEvent> events) {
        StringBuilder sb = new StringBuilder(parcelJson(p));
        sb.setLength(sb.length() - 1); // remove trailing }
        sb.append(",\"events\":[");
        for (int i = 0; i < events.size(); i++) {
            if (i > 0) sb.append(",");
            ParcelEvent e = events.get(i);
            sb.append("{\"id\":").append(e.id())
              .append(",\"status\":").append(Json.val(e.status()))
              .append(",\"location\":").append(Json.val(e.location()))
              .append(",\"eventTime\":").append(Json.val(e.eventTime()))
              .append(",\"remarks\":").append(Json.val(e.remarks()))
              .append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String nvl(String s) {
        return s == null ? "" : s.trim();
    }

    private static int intParam(String s, int def) {
        try { return s == null || s.isBlank() ? def : Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    /**
     * Very simple JSON field extractor. Handles string values only.
     * Sufficient for the controlled API request bodies expected by this endpoint.
     */
    public static String jsonField(String json, String key) {
        if (json == null) return "";
        // Match: "key":"value" (with optional spaces)
        String pattern = "\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
        }
        return "";
    }
}
