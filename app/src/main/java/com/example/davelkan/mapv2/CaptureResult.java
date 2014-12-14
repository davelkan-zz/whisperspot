package com.example.davelkan.mapv2;

public class CaptureResult {
    private int usedPoints;
    private boolean wasCaptured;

    public CaptureResult() {}

    public CaptureResult(int usedPoints, boolean wasCaptured) {
        this.usedPoints = usedPoints;
        this.wasCaptured = wasCaptured;
    }

    public void setUsedPoints(int usedPoints) {
        this.usedPoints = usedPoints;
    }
    public void setWasCaptured(boolean wasCaptured) {
        this.wasCaptured = wasCaptured;
    }

    public int getUsedPoints() {
        return usedPoints;
    }
    public boolean getWasCaptured() {
        return wasCaptured;
    }
}
