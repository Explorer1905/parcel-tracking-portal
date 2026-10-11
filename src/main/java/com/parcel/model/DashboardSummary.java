package com.parcel.model;

public record DashboardSummary(int total, int registered, int inTransit,
                               int delivered, int exceptions) {
}
