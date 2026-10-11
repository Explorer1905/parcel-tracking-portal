package com.parcel.service;

import com.parcel.dao.ParcelDao;
import com.parcel.model.Parcel;
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

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}