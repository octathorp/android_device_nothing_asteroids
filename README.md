# Nothing Phone 3a (Asteroids) device tree
NOTICE: this is a work in progress, do not attempt to use it for your main device as it lacks a lot of features at this moment.
<br>
Keep in mind that this information could change without notice.
<br>

## Current status

- LineageOS 22.2 is booting normally (userdebug variant).
- Vendor files are obtained from 2506101841 build. Should be updated at some point.  
- Some patches must be applied beforehand so that building process is completed successfully (*patches* folder).  
<br>

| Feature         | Status     | Comments                                                     |
| --------------- |------------|--------------------------------------------------------------|
| Display         |     OK     | Requires further testing, color adjusting needs improvements |
| Touch           |     OK     | Seems to be ok                                               |
| Charging        |     OK     | Requires further testing (PD and fast charging)              |
| WiFi            |     OK     | Requires further testing, basic functionality seems to be ok |
| Bluetooth       |     OK     | Scan and pairing ok, needs further testing                   |
| Modem           |     OK     | System recognizes it, IMEIs displayed, SIM cards detected    |
| Mobile data     |     OK     | Working properly at 5G speeds, APN automatically applied     |
| Calls           |     OK     | Works on a happy path, needs further review                  |
| Audio           |     OK     | Speaker, BT and USB-C wired working                          |
| Sensors         |     OK     | Sensors seem to be reporting properly                        |
| 120Hz refresh   |     OK     | Automatic refresh rate (60-90-120Hz)                         |
| NFC             |     OK     | Requires further testing, NFC tag recognized properly        |
| GPS/GNSS        |     OK     | Tested with GPS Test, real world testing required            |
| Haptics         |     OK     | Working, but fine-tuning may be necessary                    |
| Cameras         |     OK     | Working, needs further review                                |
| Fingerprint     |     OK     | Working, requires resources-related work (broken visuals)    |
| USB             |     OK     | File transfer (MTP), ADB and OTG working properly            |
| SELinux         |     OK     | System booting, main functions appear to be working ATM      |
| Extra key       |     NO     | Not working                                                  |
| Glyph           |     NO     | Not working                                                  |


<br>

## Dirty patches ##

There are 4 patches that should be applied before launching build process. It's not the right thing to do, but at this moment it works for me. It should be fixed as soon as possible.  

- *hardware_qcom-caf_sm8650_dataipa.patch*: resolves some imports not being found.
- *hardware_qcom-caf_sm8650_data-ipa-cfg-mgr.patch*: resolves some imports not being found.
- *hardware_qcom-caf_sm8650_display.patch*: resolves some imports not being found + reverts commit 8791e48 as it was causing a display-composer crash.
- *vendor_lineage.patch*: forces dtb.img to be created with only the necessary dtb files + forces symbols to be retained during dtbs building.
