package com.vendor.cavli.cavlitestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vendor.cavli.automotive.vehicle.CavliVehicle;
import com.vendor.cavli.automotive.vehicle.ICavliVehicleCallback;

import java.util.ArrayList;

import vendor.cavli.automotive.vehicle.V1_0.SubscribeFlags;
import vendor.cavli.automotive.vehicle.V1_0.SubscribeOptions;
import vendor.cavli.automotive.vehicle.V1_0.VehiclePropValue;
import vendor.cavli.automotive.vehicle.V1_0.VehicleProperty;

public class MainActivity extends AppCompatActivity {

    final String TAG = "cavlitestapp";
    CavliVehicle mVehicle;
    HandlerThread handlerThread;
    Looper looper;
    ICavliVehicleCallback vehicleCallback;
    SubscribeOptions opts;
    TextView odometer_val;
    TextView speed_val;
    boolean isSubscribing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start_read = findViewById(R.id.read_data);
        odometer_val = (TextView)findViewById(R.id.odometer);
        speed_val = (TextView)findViewById(R.id.speedmeter);

        handlerThread = new HandlerThread("VehicleThread");
        handlerThread.start();
        looper = handlerThread.getLooper();
        vehicleCallback = new VehicleCallback();
        mVehicle = new CavliVehicle(looper, vehicleCallback);
        odometer_val.setText("0");
        speed_val.setText("0");

        start_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVehicle != null) {
                    if (!isSubscribing) {
                        startSubscribing();
                        start_read.setText("Stop");
                    } else {
                        stopSubscribing();
                        start_read.setText("Start");
                        odometer_val.setText("0");
                        speed_val.setText("0");
                    }
                    isSubscribing = !isSubscribing;
                } else {
                    Log.e(TAG, "mVehicle is null");
                }
            }
        });
    }

    // Method to start subscribing
    protected void startSubscribing() {
        opts = new SubscribeOptions();
        opts.propId = VehicleProperty.PERF_ODOMETER;
        opts.sampleRate = 100;
        opts.flags = SubscribeFlags.EVENTS_FROM_CAR;

        if (mVehicle != null) {
            try {
                Log.i(TAG, "subscribe");
                mVehicle.subscribe(opts);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "mVehicle is null in startSubscribing()");
        }
    }

    protected  void stopSubscribing() {
        if (mVehicle != null) {
            try {
                Log.i(TAG, "unsubscribe");
                mVehicle.unsubscribe(VehicleProperty.PERF_ODOMETER);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "mVehicle is null in startSubscribing()");
        }
    }
    private class VehicleCallback implements ICavliVehicleCallback {
        @Override
        public void onPropertyEvent(ArrayList<VehiclePropValue> propValues) {
            Log.d(TAG, "onPropertyEvent propValues.size()=  " + propValues.size());
            for (VehiclePropValue vpv : propValues) {
                Log.d(TAG, "vpv.prop " + vpv.prop);
                if (vpv.prop == VehicleProperty.PERF_ODOMETER && vpv.value != null
                        && vpv.value.floatValues != null
                        && vpv.value.floatValues.size() == 1) {
                    float odometer_value = vpv.value.floatValues.get(0);
                    Log.d(TAG, "odometer_value " + odometer_value);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            odometer_val.setText(String.valueOf(odometer_value));
                        }
                    });
                }
                if (vpv.prop == VehicleProperty.PERF_VEHICLE_SPEED && vpv.value != null
                        && vpv.value.floatValues != null
                        && vpv.value.floatValues.size() == 1) {
                    float speed_value = vpv.value.floatValues.get(0);
                    Log.d(TAG, "speed_value " + speed_value);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            speed_val.setText(String.valueOf(speed_value));
                        }
                    });
                }
            }
        }

        @Override
        public void onPropertySet(VehiclePropValue propValue) {
        }

        @Override
        public void onPropertySetError(int errorCode, int propId, int areaId) {
        }

        @Override
        public void onConnected() {
            Log.i(TAG, "connected");
        }
    }
}