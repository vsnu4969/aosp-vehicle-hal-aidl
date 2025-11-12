# Custom Vehicle HAL Properties

Quick reference for adding custom vehicle properties to Android Automotive using AIDL.

## What This Does

Allows you to define custom vehicle properties (sensors, settings, controls) that Android apps can read and write through the Car API. This uses the modern AIDL approach (not the deprecated HIDL).

## Repository Structure

```
aosp-rpi4-vehicle-hal-aidl/
│
├── hardware/
│   └── interfaces/
│       └── automotive/
│           └── vehicle/
│               └── aidl/
│                   ├── VendorVehicleProperty.aidl    # Define property IDs here
│                   └── Android.bp                     # Build config
│
├── vehicle_hal/
│   ├── VendorProperties.json                         # Configure property behavior
│   └── Android.bp                                    # Install config
│
├── device_config/
│   └── device.mk                                     # Example device integration
│
└── examples/
    └── VehiclePropertyApp.java                       # Android app example
```

## How It Works

```
Android App
    ↓
CarPropertyManager
    ↓
Car Service
    ↓
Vehicle HAL Service (reads config on boot)
    ↓
/vendor/etc/automotive/vhalconfig/3/VendorProperties.json
```

## Setup Steps

Three main steps to integrate into your AOSP build:

### Step 1: Copy Files to AOSP Tree

Navigate to your AOSP directory and create the vendor structure:

```bash
cd /path/to/your/aosp

# Create vendor directories
mkdir -p vendor/xyz/hardware/interfaces/automotive/vehicle/aidl
mkdir -p vendor/xyz/vehicle_hal

# Copy AIDL definitions
cp -r hardware/interfaces/automotive/vehicle/aidl/* \
      vendor/xyz/hardware/interfaces/automotive/vehicle/aidl/

# Copy HAL configuration
cp -r vehicle_hal/* vendor/xyz/vehicle_hal/
```

> **Note:** Replace `xyz` with your actual vendor name throughout (e.g., `acme`, `mycompany`, etc.)

### Step 2: Add to Device Makefile

Edit your device's product makefile (usually `device/manufacturer/model/device.mk` or `aosp_model_car.mk`):

```makefile
# Vehicle HAL with custom properties
PRODUCT_PACKAGES += \
    xyz_vehicle_hal_config
```

The Vehicle HAL service itself is typically already included in automotive builds. This line just adds your custom configuration.

See `device_config/device.mk` for a complete example with more details.

### Step 3: Build and Flash

```bash
# Setup build environment
source build/envsetup.sh
lunch your_device-userdebug

# Build the AIDL interface
m vendor.xyz.automotive.vehicle-cpp

# Build full system
m -j$(nproc)

# Flash to device (method varies)
fastboot flashall -w
# or
adb reboot bootloader && fastboot flashall
```

## Verify Installation

After flashing, check if your configuration is installed:

```bash
# Check config file exists
adb shell ls -l /vendor/etc/automotive/vhalconfig/3/
# Should show: VendorProperties.json

# Check Vehicle HAL is running
adb shell ps -A | grep vehicle
```

## Testing Your Property

Test the custom property using command line:

```bash
# Read property value (ID 557087745 = 0x21400001)
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --get 557087745

# Write a new value
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --set 557087745 -i 42

# Read it back to verify
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --get 557087745
```

## Understanding Property IDs

Property IDs are 32-bit integers constructed from four parts:

```
Property ID: 0x21400001 (decimal: 557087745)

Breakdown:
0x21400001 = 0x20000000 | 0x00400000 | 0x01000000 | 0x0001
             └─────┬────┘   └────┬───┘   └────┬───┘   └─┬─┘
                VENDOR        INT32       GLOBAL       ID

• 0x20000000 = VENDOR (marks this as vendor-specific property)
• 0x00400000 = INT32 (property data type)
• 0x01000000 = GLOBAL (applies to whole vehicle)
• 0x0001 = Unique ID (your custom identifier)
```

**Common data types:**
- `0x00400000` - INT32 (whole numbers)
- `0x00600000` - FLOAT (decimal numbers, good for sensors)
- `0x00100000` - STRING (text values)
- `0x00200000` - BOOLEAN (true/false)

## Adding More Properties

To add additional custom properties, edit both the AIDL and JSON files.

### 1. Define Property ID in AIDL

Edit `hardware/interfaces/automotive/vehicle/aidl/VendorVehicleProperty.aidl`:

```java
enum VendorVehicleProperty {
    // Existing property
    CUSTOM_VENDOR_PROPERTY = (0x20000000 | 0x00400000 | 0x01000000 | 0x0001),
    
    // New temperature sensor (float type)
    CUSTOM_TEMPERATURE = (0x20000000 | 0x00600000 | 0x01000000 | 0x0002),
    //                                    └───────┬────────┘
    //                                      FLOAT type
}
```

### 2. Configure Property Behavior in JSON

Edit `vehicle_hal/VendorProperties.json`:

```json
{
    "comment": "Temperature sensor in Celsius",
    "property": 557150210,
    "access": "VehiclePropertyAccess::READ_WRITE",
    "changeMode": "VehiclePropertyChangeMode::ON_CHANGE",
    "areas": [{
        "defaultValue": {"floatValues": [20.0]},
        "areaId": 0
    }]
}
```

**Access modes:**
- `READ_WRITE` - Apps can read and write (for settings)
- `READ` - Apps can only read (for sensors)
- `WRITE` - Apps can only write (for commands)

**Change modes:**
- `ON_CHANGE` - Updates only when value changes (most properties)
- `CONTINUOUS` - Periodic updates with sample rate (for sensors)
- `STATIC` - Never changes after boot (like VIN)

### 3. Rebuild

```bash
m vendor.xyz.automotive.vehicle-cpp
m xyz_vehicle_hal_config
m -j$(nproc)
```

## Using in Android Apps

See `examples/VehiclePropertyApp.java` for a complete working example.

### Basic Usage

```java
// Get Car service
Car car = Car.createCar(context);
CarPropertyManager pm = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);

// Read property
int value = pm.getIntProperty(
    557087745,  // Property ID
    VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL
);

// Write property
pm.setIntProperty(
    557087745,
    VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL,
    100  // New value
);
```

### Required Permission

Add to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.car.permission.CAR_VENDOR_EXTENSION"/>
```

### Listening for Changes

```java
pm.registerCallback(new CarPropertyManager.CarPropertyEventCallback() {
    @Override
    public void onChangeEvent(CarPropertyValue value) {
        Log.d(TAG, "Property changed: " + value.getValue());
    }
    
    @Override
    public void onErrorEvent(int propId, int zone) {
        Log.e(TAG, "Error reading property");
    }
}, 557087745, CarPropertyManager.SENSOR_RATE_NORMAL);
```

## Troubleshooting

### Property Not Found

```bash
# Verify config file is installed
adb shell ls -l /vendor/etc/automotive/vhalconfig/3/

# Check if Vehicle HAL service is running
adb shell ps -A | grep vehicle

# List all available properties to find yours
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --list | grep 557087745
```

**Fix:** Rebuild and reflash the device.

### Build Errors

If you get `vendor.xyz.automotive.vehicle-cpp not found`:

```bash
# Build the AIDL interface first
m vendor.xyz.automotive.vehicle-cpp
```

### Permission Denied in App

**Fix:** Make sure you added the permission to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.car.permission.CAR_VENDOR_EXTENSION"/>
```

## Important Notes

- **Replace `xyz`** with your actual vendor name in all files
- Properties are loaded from `/vendor/etc/automotive/vhalconfig/3/` on boot
- Property values **reset on reboot** (not persistent by default)
- Use property IDs in range **0x0001 to 0x9FFF** to avoid conflicts
- The Vehicle HAL loads **all** `.json` files from the config directory

## Examples

### Read-Only Sensor (Temperature)
```java
// AIDL
SENSOR_TEMP = (0x20000000 | 0x00600000 | 0x01000000 | 0x0010),

// JSON
{"property": 557153296, "access": "READ", "changeMode": "CONTINUOUS",
 "minSampleRate": 1.0, "maxSampleRate": 10.0,
 "areas": [{"defaultValue": {"floatValues": [25.0]}, "areaId": 0}]}
```

### Read-Write Setting (User Preference)
```java
// AIDL
USER_SETTING = (0x20000000 | 0x00400000 | 0x01000000 | 0x0020),

// JSON
{"property": 557087776, "access": "READ_WRITE", "changeMode": "ON_CHANGE",
 "areas": [{"defaultValue": {"int32Values": [0]}, "areaId": 0}]}
```

## License

Apache License 2.0 - See LICENSE file.

This is a derivative work based on the Android Open Source Project (AOSP).
