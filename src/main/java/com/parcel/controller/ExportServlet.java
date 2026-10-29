package com.parcel.controller;

import com.parcel.model.Parcel;
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
 * GET /export/parcels.csv — exports all parcels as a CSV file.
 * Content-Disposition: attachment triggers browser download.
 */
@WebServlet("/export/parcels.csv")
public class ExportServlet extends HttpServlet {
    private final ParcelService service = new ParcelService();

    private static final String[] HEADERS = {
        "Parcel ID", "Sender", "Receiver", "Origin", "Destination",
        "Status", "Priority", "Weight (kg)", "Expected Delivery",
        "Assigned Agent", "Created At"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"parcels.csv\"");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        PrintWriter out = resp.getWriter();

        // Header row
        out.println(csvRow(HEADERS));

        // Data rows
        List<Parcel> parcels = service.listParcels();
        for (Parcel p : parcels) {
            out.println(csvRow(
                    p.id(), p.sender(), p.receiver(), p.origin(), p.destination(),
                    p.status(), p.priority(),
                    String.valueOf(p.weightKg()),
                    p.expectedDelivery() == null ? "" : p.expectedDelivery(),
                    p.assignedAgent() == null ? "" : p.assignedAgent(),
                    p.createdAt() == null ? "" : p.createdAt()
            ));
        }
    }

    private static String csvRow(String... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(Json.csvCell(cells[i] == null ? "" : cells[i]));
        }
        return sb.toString();
    }
}
