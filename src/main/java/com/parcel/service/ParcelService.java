package com.parcel.service;

import com.parcel.dao.ParcelDao;
import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.model.DashboardSummary;
import java.util.List;

public class ParcelService {
    private final ParcelDao dao = new ParcelDao();

    public void addParcel(String id, String sender, String receiver, String origin, String destination) {
        id = clean(id);
        sender = clean(sender);
        receiver = clean(receiver);
        origin = clean(origin);
        destination = clean(destination);

        if (id.isEmpty() || sender.isEmpty() || receiver.isEmpty()
                || origin.isEmpty() || destination.isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }
        if (!id.matches("[A-Za-z0-9-]{3,30}")) {
            throw new IllegalArgumentException(
                    "Parcel ID must be 3 to 30 characters: letters, digits or hyphen.");
        }
        if (dao.exists(id)) {
            throw new IllegalArgumentException("Parcel ID " + id + " already exists.");
        }
        dao.add(new Parcel(id, sender, receiver, origin, destination, "Registered", null));
    }

    public List<Parcel> listParcels() {
        return dao.findAll();
    }

    public List<Parcel> search(String query) {
        return dao.find(query);
    }

    public Parcel find(String id) {
        return id == null || id.isBlank() ? null : dao.findById(id.trim());
    }

    public List<ParcelEvent> events(String id) {
        return dao.eventsFor(id);
    }

    public DashboardSummary summary() {
        return dao.summary();
    }

    public void addEvent(String id, String status, String location, String remarks) {
        id = clean(id);
        status = clean(status);
        location = clean(location);
        remarks = clean(remarks);
        if (id.isEmpty() || status.isEmpty()) {
            throw new IllegalArgumentException("Parcel and status are required.");
        }
        if (find(id) == null) {
            throw new IllegalArgumentException("Parcel " + id + " was not found.");
        }
        if (!List.of("In Transit", "Delivered", "Delayed", "Exception").contains(status)) {
            throw new IllegalArgumentException("Choose a valid status.");
        }
        dao.addEvent(id, status, location, remarks);
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}