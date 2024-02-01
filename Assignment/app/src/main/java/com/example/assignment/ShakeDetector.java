package com.example.assignment;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_INTERVAL = 500;
    private long lastShakeTime;
    private OnShakeListener onShakeListener;
    public ShakeDetector(OnShakeListener listener) {
        this.onShakeListener = listener;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    private void detectShake(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
        if (acceleration > SHAKE_THRESHOLD) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShakeTime > SHAKE_INTERVAL) {
                lastShakeTime = currentTime;
                onShakeListener.onShake();
            }
        }
    }
    public interface OnShakeListener {
        void onShake();
    }
}

