package com.example.yuuura87.minesweeper;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class SensorService extends Service {

    private final IBinder sensorBinder = new MyLocalBinder();
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private SensorEventListener rotationEventListener;
    private ServiceCallbacks serviceCallbacks;

    public interface ServiceCallbacks {
        void sensorChanged(int x, int y, int z);
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(rotationSensor == null)
            Toast.makeText(this, "Rotation sensor is not available on this device", Toast.LENGTH_SHORT).show();
        else{
            rotationEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    if(serviceCallbacks != null)
                        serviceCallbacks.sensorChanged((int)(sensorEvent.values[0] * 10), (int)(sensorEvent.values[1] * 10), (int)(sensorEvent.values[2] * 10));
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int i) { }
            };
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sensorBinder;
    }

    public void onResume() {
        sensorManager.registerListener(rotationEventListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onPause() {
        sensorManager.unregisterListener(rotationEventListener);
    }

    public class MyLocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }
}
