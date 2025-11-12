/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vendor.xyz.automotive.vehicle;

/**
 * Vendor-specific vehicle properties for custom automotive implementation.
 *
 * Property ID format:
 *   Property ID = VehiclePropertyGroup | VehiclePropertyType | VehicleArea | PropertyId
 *
 * VehiclePropertyGroup.VENDOR = 0x20000000
 * VehiclePropertyType.INT32   = 0x00400000
 * VehicleArea.GLOBAL          = 0x01000000
 */
@Backing(type="int")
enum VendorVehicleProperty {
    /**
 * Custom vendor property for embedded automotive system
 * 
 * This is a simple read-write integer property that can store any value.
     * Use it to communicate custom data between your app and the vehicle HAL.
     * 
     * Property ID = 0x20000000 (VENDOR) | 0x00400000 (INT32) | 0x01000000 (GLOBAL) | 0x0001
     *               = 0x21400001 = 557087745
     * 
     * @change_mode VehiclePropertyChangeMode::ON_CHANGE
     * @access VehiclePropertyAccess::READ_WRITE
     */
    CUSTOM_VENDOR_PROPERTY = (0x20000000 | 0x00400000 | 0x01000000 | 0x0001),
}
