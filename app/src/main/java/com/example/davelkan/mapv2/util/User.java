package com.example.davelkan.mapv2.util;

/**
 * Created by mwismer on 12/16/14.
 */
public class User {
    private String name;
    private RawUser userInfo;

    public User(String username, String color) {
        this.name = username;
        this.userInfo = new RawUser(color, "0", 0);
    }

    public User(String name, RawUser user) {
        this.name = name;
        userInfo = user;
    }

    public void updateWith(RawUser user) {
        user.setPoints(user.getPoints() + userInfo.getPoints());
        userInfo = user;
    }

    public void postUpdate() {

    }

    public String getName() {
        return name;
    }
    public RawUser getRawUser() {
        return userInfo;
    }
    public int getPoints() {
        return userInfo.getPoints();
    }
    public String getTimestamp() {
        return userInfo.getCreated();
    }
    public String getColor() {
        return userInfo.getColor();
    }
    public String getEnemyColor() {
        if (userInfo.getColor().equals("red")) { return "blue"; }
        if (userInfo.getColor().equals("blue")) { return "red"; }
        return "default";
    }

    public void setName(String name) {
        this.name = name;
    }
    public void addPoints(int points) {
        userInfo.setPoints(userInfo.getPoints() + points);
    }
    public void setPoints(int points) {
        userInfo.setPoints(points);
    }
    public void setColor(String color) {
        userInfo.setColor(color);
    }
}
