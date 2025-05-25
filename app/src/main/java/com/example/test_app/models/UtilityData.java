package com.example.test_app.models;

public class UtilityData {
    public String userId;
    public String month;
    public int electricity;
    public int water;
    public double billAmount;
    public boolean isPaid;

    public UtilityData() {
    }

    public UtilityData(String userId, String month, int electricity, int water, double billAmount, boolean isPaid) {
        this.userId = userId;
        this.month = month;
        this.electricity = electricity;
        this.water = water;
        this.billAmount = billAmount;
        this.isPaid = isPaid;
    }

    public String getUserId() {
        return userId;
    }

    public String getMonth() {
        return month;
    }

    public int getWater() {
        return water;
    }

    public int getElectricity() {
        return electricity;
    }

    public double getBill() {
        return billAmount;
    }

    public boolean isPaid() {
        return isPaid;
    }
}
