package com.vendor.cavli.cavlitestapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static vendor.cavli.automotive.vehicle.V1_0.VehicleProperty.HW_KEY_INPUT;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.vendor.cavli.automotive.vehicle.CavliVehicle;
import com.vendor.cavli.automotive.vehicle.ICavliVehicleCallback;

import java.util.ArrayList;

import vendor.cavli.automotive.vehicle.V1_0.SubscribeFlags;
import vendor.cavli.automotive.vehicle.V1_0.SubscribeOptions;
import vendor.cavli.automotive.vehicle.V1_0.VehiclePropValue;
import vendor.cavli.automotive.vehicle.V1_0.VehicleProperty;

public class MainActivity extends AppCompatActivity {

    final String TAG = "cavlitestapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HandlerThread handlerThread = new HandlerThread("VehicleThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        // Create an instance of IVehicleCallback to handle callbacks
        ICavliVehicleCallback vehicleCallback = new VehicleCallback();

        SubscribeOptions opts = new SubscribeOptions();
        opts.propId = VehicleProperty.PERF_ODOMETER;
        opts.sampleRate = 100;
        opts.flags = SubscribeFlags.EVENTS_FROM_CAR;

        CavliVehicle mVehicle = new CavliVehicle(looper, vehicleCallback);

        try {
            Log.i(TAG, "subscribe");
            mVehicle.subscribe(opts);
        } catch (RemoteException e) {

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
                }
                if (vpv.prop == VehicleProperty.PERF_VEHICLE_SPEED && vpv.value != null
                        && vpv.value.floatValues != null
                        && vpv.value.floatValues.size() == 1) {
                    float speed_value = vpv.value.floatValues.get(0);
                    Log.d(TAG, "speed_value " + speed_value);
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