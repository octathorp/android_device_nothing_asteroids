# Notice: DEPRECATED
This repo is deprecated, use https://github.com/AKoskovich/android_device_nothing_asteroids instead.
No further work will be done here.
   
# Nothing Phone 3a (Asteroids) device tree
NOTICE: this is a work in progress, do not attempt to use it for your main device as it is not stable enough at this moment.
<br>
Keep in mind that this information could change without notice.
<br>

## Current status

- LineageOS 22.2 is booting normally (userdebug variant).
- Vendor files are obtained from 2506101841 build. Should be updated at some point.  
- Some patches must be applied beforehand so that building process is completed successfully (*patches* folder).  
<br>

| Feature         | Status     | To Do                                                        |
| --------------- |------------|--------------------------------------------------------------|
| Display         |     OK     |                                                              |
| Touch           |     OK     |                                                              |
| Charging        |     OK     | Requires further testing (PD and fast charging)              |
| WiFi            |     OK     | Requires further testing                                     |
| Bluetooth       |     OK     | Needs further testing                                        |
| Modem           |     OK     |                                                              |
| Mobile data     |     OK     |                                                              |
| Calls           |     OK     | Needs further review (noise cancelling, VoLTE?)              |
| Audio           |     OK     |                                                              |
| Sensors         |     OK     |                                                              |
| 120Hz refresh   |     OK     |                                                              |
| NFC             |     OK     | Requires further testing, NFC tag recognized properly        |
| GPS/GNSS        |     OK     | Real world testing required                                  |
| Haptics         |     OK     |                                                              |
| Cameras         |     OK     |                                                              |
| Fingerprint     |     OK     | Requires some minor, cosmetic improvements                   |
| USB             |     OK     |                                                              |
| SELinux         |     OK     |                                                              |
| Essential key   |     OK     |                                                              |
| Glyph           |     OK     |                                                              |


<br>

## Dirty patches ##

There are 4 patches that should be applied before launching build process. It's not the right thing to do, but at this moment it works for me. It should be fixed as soon as possible.  

- *hardware_qcom-caf_sm8650_dataipa.patch*: resolves some imports not being found.
- *hardware_qcom-caf_sm8650_data-ipa-cfg-mgr.patch*: resolves some imports not being found.
- *hardware_qcom-caf_sm8650_display.patch*: resolves some imports not being found + reverts commit 8791e48 as it was causing a display-composer crash.
- *vendor_lineage.patch*: forces dtb.img to be created with only the necessary dtb files + forces symbols to be retained during dtbs building.


<br>

## Back to stock ##

Get stock NothingOS 2506101841 package and reboot into bootloader:

	fastboot -w
	fastboot flash boot boot.img
	fastboot flash dtbo dtbo.img
	fastboot flash vendor_boot vendor_boot.img
	fastboot flash recovery recovery.img
	fastboot flash init_boot init_boot.img
	fastboot flash pvmfw pvmfw.img
	fastboot flash vbmeta vbmeta.img
	fastboot flash vbmeta_system vbmeta_system.img
	fastboot flash vbmeta_vendor vbmeta_vendor.img
	fastboot reboot recovery

Once in recovery, sideload stock Asteroids_V3.1-250610-1841_3.1.zip full OTA:

	adb sideload Asteroids_V3.1-250610-1841_3.1.zip
