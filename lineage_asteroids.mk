#
# Copyright (C) 2025 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from asteroids device
$(call inherit-product, device/nothing/asteroids/device.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_DEVICE := asteroids
PRODUCT_NAME := lineage_asteroids
PRODUCT_BRAND := Nothing
PRODUCT_MODEL := Nothing Phone (3a)
PRODUCT_MANUFACTURER := Nothing

PRODUCT_BRAND_FOR_ATTESTATION := Nothing
PRODUCT_MODEL_FOR_ATTESTATION := Nothing Phone (3a)
PRODUCT_NAME_FOR_ATTESTATION := asteroids

PRODUCT_GMS_CLIENTID_BASE := android-nothing

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="qssi_64-user 15 AQ3A.241015.001 2506101841 release-keys" \
    BuildFingerprint=Nothing/Asteroids/Asteroids:14/UKQ1.241011.001/2506101841:user/release-keys \
    DeviceName=Asteroids \
    DeviceProduct=Asteroids \
    SystemDevice=Asteroids \
    SystemName=Asteroids
