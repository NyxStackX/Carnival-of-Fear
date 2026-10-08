package com.epitech.fnah.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class GameClock {

    public static final int SECONDS_PER_HOUR = 60;
    public static final int NIGHT_END_HOUR = 6;

    private double realSeconds;
    private int lastNotifiedHour;

    private final List<IntConsumer> hourListeners = new ArrayList<>();

    public GameClock() {
        this.realSeconds = 0.0;
        this.lastNotifiedHour = 0;
    }


    public void update(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        this.realSeconds += deltaSeconds;
        int currentHour = getHour();
        while (lastNotifiedHour < currentHour) {
            lastNotifiedHour++;
            notifyHour(lastNotifiedHour);
        }
    }

    public int getHour() {
        int h = (int) (realSeconds / SECONDS_PER_HOUR);
        return Math.min(h, NIGHT_END_HOUR);
    }

    public int getMinute() {
        double hourFraction = (realSeconds / SECONDS_PER_HOUR) - getHour();
        return (int) (hourFraction * 60);
    }

    public String getFormattedTime() {
        return String.format("%02d:%02d", getHour(), getMinute());
    }

    public boolean isNightOver() {
        return getHour() >= NIGHT_END_HOUR;
    }

    public double getElapsedSeconds() {
        return realSeconds;
    }

    public void reset() {
        realSeconds = 0.0;
        lastNotifiedHour = 0;
    }

    public void onHourChange(IntConsumer listener) {
        hourListeners.add(listener);
    }

    private void notifyHour(int hour) {
        for (IntConsumer l : hourListeners) {
            l.accept(hour);
        }
    }
}