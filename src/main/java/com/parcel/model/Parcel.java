package com.parcel.model;

/**
 * Immutable parcel record. The 7-argument constructor (id through createdAt) is kept
 * for backward compatibility; the 11-argument form adds the Phase A1 fields.
 * Default values: weightKg=1.0, priority="Standard", expectedDelivery=null, assignedAgent=null.
 */
public record Parcel(String id, String sender, String receiver,
                     String origin, String destination,
                     String status, String createdAt,
                     double weightKg, String priority,
                     String expectedDelivery, String assignedAgent) {

    /** Convenience constructor (V1 compatibility): new columns receive defaults. */
    public Parcel(String id, String sender, String receiver,
                  String origin, String destination,
                  String status, String createdAt) {
        this(id, sender, receiver, origin, destination, status, createdAt,
             1.0, "Standard", null, null);
    }
}