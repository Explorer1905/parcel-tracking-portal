package com.parcel.model;

public record ParcelEvent(long id, String parcelId, String status,
                          String location, String eventTime, String remarks,
                          boolean delayed) {
}
