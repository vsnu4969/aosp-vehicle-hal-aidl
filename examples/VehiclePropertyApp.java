/*
 * Example Android App showing how to use custom vendor vehicle properties
 * 
 * This demonstrates reading and writing to the custom vendor property
 * defined in VendorVehicleProperty.aidl
 * 
 * IMPORTANT: Add these to your AndroidManifest.xml:
 * 
 * <uses-permission android:name="android.car.permission.CAR_VENDOR_EXTENSION"/>
 * <uses-permission android:name="android.permission.INTERNET"/>
 */

package com.example.vehiclepropertyapp;

import android.app.Activity;
import android.car.Car;
import android.car.hardware.property.CarPropertyManager;
import android.car.hardware.CarPropertyValue;
import android.car.VehicleAreaType;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VehiclePropertyActivity extends Activity {
    private static final String TAG = "VehiclePropertyApp";
    
    // Custom vendor property ID from VendorVehicleProperty.aidl
    // CUSTOM_VENDOR_PROPERTY = 0x21400001 = 557087745
    private static final int CUSTOM_VENDOR_PROPERTY = 557087745;
    
    private Car mCar;
    private CarPropertyManager mCarPropertyManager;
    private TextView mPropertyValueText;
    private Button mReadButton;
    private Button mWriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mPropertyValueText = findViewById(R.id.propertyValue);
        mReadButton = findViewById(R.id.readButton);
        mWriteButton = findViewById(R.id.writeButton);
        
        // Initialize Car API
        initializeCar();
        
        // Set up button listeners
        mReadButton.setOnClickListener(v -> readProperty());
        mWriteButton.setOnClickListener(v -> writeProperty());
    }

    private void initializeCar() {
        try {
            // Create Car instance
            mCar = Car.createCar(this, null, Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER,
                (car, ready) -> {
                    if (ready) {
                        Log.i(TAG, "Car service connected");
                        setupCarPropertyManager();
                    } else {
                        Log.w(TAG, "Car service disconnected");
                        mCarPropertyManager = null;
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Car instance", e);
            showToast("Failed to connect to Car service");
        }
    }

    private void setupCarPropertyManager() {
        try {
            mCarPropertyManager = (CarPropertyManager) mCar.getCarManager(Car.PROPERTY_SERVICE);
            
            // Register for property change notifications
            mCarPropertyManager.registerCallback(mPropertyCallback, 
                CUSTOM_VENDOR_PROPERTY, 
                CarPropertyManager.SENSOR_RATE_NORMAL);
                
            Log.i(TAG, "CarPropertyManager initialized");
            showToast("Connected to Vehicle HAL");
            
            // Read initial value
            readProperty();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup CarPropertyManager", e);
            showToast("Failed to access vehicle properties");
        }
    }

    private final CarPropertyManager.CarPropertyEventCallback mPropertyCallback =
            new CarPropertyManager.CarPropertyEventCallback() {
        @Override
        public void onChangeEvent(CarPropertyValue value) {
            Log.i(TAG, "Property changed: " + value.getValue());
            runOnUiThread(() -> {
                mPropertyValueText.setText("Current Value: " + value.getValue());
                showToast("Property updated to: " + value.getValue());
            });
        }

        @Override
        public void onErrorEvent(int propId, int zone) {
            Log.e(TAG, "Property error: " + propId + " zone: " + zone);
            runOnUiThread(() -> showToast("Error reading property"));
        }
    };

    private void readProperty() {
        if (mCarPropertyManager == null) {
            showToast("Car service not ready");
            return;
        }

        try {
            // Read the property value
            int value = mCarPropertyManager.getIntProperty(
                CUSTOM_VENDOR_PROPERTY,
                VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL
            );
            
            Log.i(TAG, "Read property value: " + value);
            mPropertyValueText.setText("Current Value: " + value);
            showToast("Read value: " + value);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to read property", e);
            showToast("Failed to read property: " + e.getMessage());
        }
    }

    private void writeProperty() {
        if (mCarPropertyManager == null) {
            showToast("Car service not ready");
            return;
        }

        try {
            // Generate a new value (for demo, just increment)
            int currentValue = mCarPropertyManager.getIntProperty(
                CUSTOM_VENDOR_PROPERTY,
                VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL
            );
            
            int newValue = currentValue + 1;
            
            // Write the new value
            mCarPropertyManager.setIntProperty(
                CUSTOM_VENDOR_PROPERTY,
                VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL,
                newValue
            );
            
            Log.i(TAG, "Wrote property value: " + newValue);
            showToast("Wrote value: " + newValue);
            
            // Read it back to confirm
            readProperty();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to write property", e);
            showToast("Failed to write property: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unregister callback
        if (mCarPropertyManager != null) {
            try {
                mCarPropertyManager.unregisterCallback(mPropertyCallback);
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister callback", e);
            }
        }
        
        // Disconnect from Car service
        if (mCar != null) {
            mCar.disconnect();
        }
    }
}

/*
 * Example AndroidManifest.xml entries:
 * 
 * <manifest xmlns:android="http://schemas.android.com/apk/res/android"
 *     package="com.example.vehiclepropertyapp">
 * 
 *     <!-- Required permissions -->
 *     <uses-permission android:name="android.car.permission.CAR_VENDOR_EXTENSION"/>
 *     
 *     <!-- Optional: For logging -->
 *     <uses-permission android:name="android.permission.INTERNET"/>
 *     
 *     <application
 *         android:label="Vehicle Property Demo"
 *         android:icon="@drawable/ic_launcher">
 *         
 *         <activity android:name=".VehiclePropertyActivity"
 *             android:exported="true">
 *             <intent-filter>
 *                 <action android:name="android.intent.action.MAIN"/>
 *                 <category android:name="android.intent.category.LAUNCHER"/>
 *             </intent-filter>
 *         </activity>
 *     </application>
 * </manifest>
 */

/*
 * Example activity_main.xml layout:
 * 
 * <?xml version="1.0" encoding="utf-8"?>
 * <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:orientation="vertical"
 *     android:padding="16dp"
 *     android:gravity="center">
 *     
 *     <TextView
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="Custom Vehicle Property Demo"
 *         android:textSize="24sp"
 *         android:layout_marginBottom="32dp"/>
 *     
 *     <TextView
 *         android:id="@+id/propertyValue"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="Current Value: --"
 *         android:textSize="18sp"
 *         android:layout_marginBottom="24dp"/>
 *     
 *     <Button
 *         android:id="@+id/readButton"
 *         android:layout_width="200dp"
 *         android:layout_height="wrap_content"
 *         android:text="Read Property"
 *         android:layout_marginBottom="16dp"/>
 *     
 *     <Button
 *         android:id="@+id/writeButton"
 *         android:layout_width="200dp"
 *         android:layout_height="wrap_content"
 *         android:text="Increment Value"/>
 * </LinearLayout>
 */
