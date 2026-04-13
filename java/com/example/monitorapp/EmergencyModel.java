package com.example.monitorapp;

public class EmergencyModel {

    public String deviceId;
    public String userEmail;
    public String userName;

    public double lat;
    public double lng;

    public long createdTime;
    public String status;

    public String resolvedBy;
    public Long resolvedTime;

    // 🔥 REQUIRED EMPTY CONSTRUCTOR (VERY IMPORTANT)
    public EmergencyModel(String deviceId, Object email, long l) {
    }

    // 🔥 CONSTRUCTOR USED WHEN SOS IS SENT
    public EmergencyModel(
            String deviceId,
            String userEmail,
            String userName,
            double lat,
            double lng,
            long createdTime
    ) {
        this.deviceId = deviceId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.lat = lat;
        this.lng = lng;
        this.createdTime = createdTime;
        this.status = "ACTIVE";
        this.resolvedBy = null;
        this.resolvedTime = null;
    }
}