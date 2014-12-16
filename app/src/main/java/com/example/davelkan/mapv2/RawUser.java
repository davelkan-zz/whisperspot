package com.example.davelkan.mapv2;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mwismer on 12/16/14.
 */
public class RawUser {
    private String color;
    private String created;
    private int points;

    public RawUser() {}

    public RawUser(String color, String created, int score) {
        this.created = created;
        this.color = color;
        this.points = score;
    }

    public String getColor() {
        return color;
    }
    public int getPoints() {
        return points;
    }
    public String getCreated() {
        return created;
    }

    public void setColor(String color) {
        this.color = color;
    }
    public void setCreated(String created) {
        this.created = created;
    }
    public void setPoints(int points) {
        this.points = points;
    }

}
