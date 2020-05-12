package com.example.doubletap;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class MainActivity extends AppCompatActivity {
    SensorManager accelerometer = null, orientation = null, pedometer = null, gripSensor = null;
    List listAccelerometer = null, listOrientation = null, listPedometer = null, listGripSensor = null;

    TextView infoDisplay;

    float[] valuesAccelerometer, valuesPedometer;
    int countOrientation = 3, countOrientationWhileWalking = 3;
    float accelerometerZValue = 0, accelerometerXValue = 0, accelerometerYValue = 0;
    float orientationXValue = 0, orientationYValue = 0, orientationZValue = 0;
    float isPedometer = 0;

    SensorEventListener lAccelerometer = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            valuesAccelerometer = event.values;
            accelerometerXValue = valuesAccelerometer[0];
            accelerometerYValue = valuesAccelerometer[1];
            accelerometerZValue = valuesAccelerometer[2];
//            Log.v("DoubleTap " , "X = " + accelerometerXValue + " Y = " + accelerometerYValue
//                    + " Z = " + accelerometerZValue);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    SensorEventListener lOrientation = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] valuesOrientation = event.values;
            orientationXValue = valuesOrientation[0];
            orientationYValue = valuesOrientation[1];
            orientationZValue = valuesOrientation[2];
//            Log.v("DoubleTap " , "X = " + orientationXValue
//                    + " Y = " + orientationYValue + " Z = " + orientationZValue);

            getWindow().getDecorView().setBackgroundColor(Color.WHITE);

             if (isPedometer == 1.0) performWalkingAction();
             else performSittingAction();
           // performSittingAction();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    SensorEventListener lPedometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            valuesPedometer = event.values;
            if(valuesPedometer[0] == 1f) isPedometer = valuesPedometer[0];
            else isPedometer = 0;
            Log.v("DoubleTap", "value of pedometer Sensor :- " + valuesPedometer[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoDisplay = findViewById(R.id.infoDisplay);
        infoDisplay.setText("Double Tap");
    }

    public void onStart() {
        super.onStart();
        accelerometer = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientation = (SensorManager) getSystemService(SENSOR_SERVICE);
        pedometer = (SensorManager) getSystemService(SENSOR_SERVICE);


        listAccelerometer = accelerometer.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        listOrientation = orientation.getSensorList(Sensor.TYPE_GYROSCOPE);
        listPedometer = pedometer.getSensorList(Sensor.TYPE_STEP_DETECTOR);

        if (listOrientation.size() > 0 && listAccelerometer.size() > 0 && listPedometer.size() > 0) {
            accelerometer.registerListener(lAccelerometer, (Sensor) listAccelerometer.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            orientation.registerListener(lOrientation, (Sensor) listOrientation.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            pedometer.registerListener(lPedometer, (Sensor) listPedometer.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            Log.v("DoubleTap", "Successfull");
        } else {
            Toast.makeText(this, "Sensor Issue", Toast.LENGTH_LONG).show();
            Log.v("DoubleTap", "no sensor found");
        }
    }

    public void onStop() {
        super.onStop();
        if (listOrientation.size() > 0 && listAccelerometer.size() > 0) {
            accelerometer.unregisterListener(lAccelerometer);
            orientation.unregisterListener(lOrientation);
            pedometer.unregisterListener(lPedometer);
        }
    }

    // Actions to be performed while user is walking
    public void performWalkingAction() {
        Toast.makeText(this, "Walking", Toast.LENGTH_SHORT).show();
        if (Math.abs(orientationXValue) > 2.5 ||
                Math.abs(orientationYValue) > 2.5 || Math.abs(orientationZValue) > 3) {
            countOrientationWhileWalking = 3;
        }
        if (countOrientationWhileWalking == 0) {
            infoDisplay.setText(" Walking Double Tap");
            if (Math.abs(accelerometerZValue) > 1.4 && Math.abs(orientationXValue) >1
                    && Math.abs(accelerometerYValue) > 1) {
                getWindow().getDecorView().setBackgroundColor(Color.RED);
                infoDisplay.setText("Walking Double Tap Detected");
            }

        } else {
            if (Math.abs(orientationXValue) < 0.7 &&
                    Math.abs(orientationYValue) < 0.7) {
                countOrientationWhileWalking--;
                infoDisplay.setText("Wait Walking" + countOrientationWhileWalking);
            }
        }
    }


    // Action to be performed while user is sitting
    public void performSittingAction() {
        if (Math.abs(orientationXValue) > 1.5 ||
                Math.abs(orientationYValue) > 1 || Math.abs(orientationZValue) > 1) {
            countOrientation = 3;
        }
        if (countOrientation <= 0) {
            infoDisplay.setText("Double Tap");
            int flag = 0;
            if (Math.abs(accelerometerZValue) > 0.70 && Math.abs(orientationXValue) > 0.4
                    && Math.abs(accelerometerYValue) < 1 && flag == 0 &&
                    Math.abs(accelerometerZValue) > Math.abs(accelerometerYValue)) { // Standing cond
                flag = 1;
                getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                infoDisplay.setText("Double Tap Detected");

            } else if (Math.abs(accelerometerZValue) > 0.8 && Math.abs(accelerometerZValue) < 2.5
                    && Math.abs(accelerometerYValue) < 1 && flag == 0 &&
                    Math.abs(orientationXValue) < 0.4 ) { // Standing condition
                getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                infoDisplay.setText("Double Tap Detected");
            }

        } else {
            if ((Math.abs(orientationXValue) < 0.2 && Math.abs(orientationYValue) < 0.2
                    && Math.abs(orientationZValue) < 0.2)) {
                countOrientation--;
                infoDisplay.setText("Wait for " + countOrientation);
            }
        }

    }

}
