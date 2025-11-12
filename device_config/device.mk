# Device Configuration for Custom Vehicle HAL Properties
#
# This file shows how to include the custom vehicle HAL configuration
# in your device build. Copy the relevant sections to your actual device
# makefile (e.g., device/manufacturer/model/device.mk or aosp_model_car.mk)

# ============================================================================
# Vehicle HAL Configuration
# ============================================================================

# Include custom vendor vehicle properties
PRODUCT_PACKAGES += \
    android.hardware.automotive.vehicle@V3-default-service \
    xyz_vehicle_hal_config

# Note: The vehicle HAL service (android.hardware.automotive.vehicle@V3-default-service)
# is typically already included in automotive builds, but we list it here for clarity.
# The important part is including your custom config: xyz_vehicle_hal_config

# ============================================================================
# How to Integrate This Into Your Device Build
# ============================================================================
#
# Option 1: If you have a device-specific makefile (e.g., device/acme/myboard/device.mk)
# Add the above PRODUCT_PACKAGES section to that file.
#
# Option 2: If you're using AOSP car makefiles (e.g., aosp_arm64_car.mk)
# Create or edit your device-specific car makefile and add the PRODUCT_PACKAGES section.
#
# Option 3: If you want to create a complete product makefile from scratch,
# see the example below:

# ============================================================================
# Example: Complete Product Makefile (aosp_xyz_car.mk)
# ============================================================================
# 
# This would typically be placed at: device/xyz/xyz_board/aosp_xyz_car.mk
#
# $(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_arm64.mk)
# $(call inherit-product, packages/services/Car/car_product/build/car.mk)
# 
# PRODUCT_NAME := aosp_xyz_car
# PRODUCT_DEVICE := xyz_board
# PRODUCT_BRAND := AOSP
# PRODUCT_MODEL := Custom Automotive Board
# PRODUCT_MANUFACTURER := YourCompany
# 
# # Vehicle HAL
# PRODUCT_PACKAGES += \
#     android.hardware.automotive.vehicle@V3-default-service \
#     xyz_vehicle_hal_config
#
# # Other automotive packages...
# PRODUCT_PACKAGES += \
#     CarService \
#     CarSettings \
#     LocalMediaPlayer \
#     CarDialerApp \
#     CarRadioApp

# ============================================================================
# Verification After Build
# ============================================================================
#
# After building and flashing, verify the config file is present:
#   adb shell ls -l /vendor/etc/automotive/vhalconfig/3/
#
# You should see: VendorProperties.json
