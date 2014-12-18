package com.example.davelkan.mapv2.util;

public class Owner {
    private String userName;
    private int points;

    public Owner() { }

    public Owner(String userName, int points) {
        this.userName = userName;
        this.points = points;
    }

    // should never have 0 points...
    public void subtractPoints(int points) {
        this.points -= points;
    }

    public int getPoints() {
        return points;
    }
    public String getUserName() {
        return userName;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
