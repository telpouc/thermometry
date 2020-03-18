package com.telpo.thermometry;

import android.annotation.SuppressLint;
import android.graphics.Point;

/**
 * Point holds two integer coordinates and temperature value
 */
public class TemperaturePoint extends Point implements Comparable<TemperaturePoint> {
    public float value;

    public TemperaturePoint(int x, int y, float value) {
        super(x, y);
        this.value = value;
    }

    /**
     * Update the temperature value while this point's x or y coordinates changed
     */
    public void updateValue(float[][] data) {
        this.value = data[y][x];
    }

    @Override @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format("{%.1f(%02d,%02d)}", value, x, y);
    }

    @Override
    public int compareTo(TemperaturePoint o) {
        return Float.compare(value, o.value);
    }
}
