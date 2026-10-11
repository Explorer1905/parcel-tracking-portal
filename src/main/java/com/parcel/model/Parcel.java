package com.parcel.model;

public record Parcel(String id, String sender, String receiver,
                     String origin, String destination,
                     String status, String createdAt) {
}