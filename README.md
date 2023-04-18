# ChillOut with Bluetooth Low Energy in Android!

<table style= padding:10px">
  <tr>
    <td>  <img src="./Docs/img1.png"  alt="1" width = 400px height = 100px ></td>
  </tr>
</table>

### For What?
---

With ChillOutBLE Library you can setup BLE connection, writing, reading characteristics, make enable/disable of notifications in few minutes :) Based on Coroutines and Flow.

Bonus functional: unbonding, method to know battery level and etc.

### How to Use?
---
Simple:

0. Accept all permissions in your app for BLE (need for all projects who use this feature)
1. Initialize core of lib
2. Launch Coroutine with initialized array of commands inside in Flow (`bleCommandTrain`) for your BLE adapter  
..  
3. PROFIT!

### What need for work?
1. Just small knowing about BLE
2. DON`T Forget CHANGE BLE ADDRESS & UUID characteristic from below example !!
```kotlin

 /**
 * Simple initilizing in Activity:
 */ 
val bleStarter = BLEStarter(this)

CoroutineScope(lifecycleScope.coroutineContext).launch { 
    BLEStarter.bleCommandTrain.emit(mutableListOf(
        StartScan(),
        Retard(6000L),
        Connect("44:44:44:44:44:0C"),
        Retard(1000L),
        StopScan(),
        ReadFromCharacteristic("44:44:44:44:44:0C", characteristicUuid = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")),
        Retard(1000L),
        EnableNotifications("44:44:44:44:44:0C", characteristicUuid = UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8")),
        Retard(1000L),
        DisableNotifications("44:44:44:44:44:0C", characteristicUuid = UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8"))
    ))
}

```

How to get a Git project into your build:

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Step 2. Add the dependency
```
dependencies {
        implementation 'com.github.arstagaev:ChillOut-with-BLE-Library:0.1'
}
	
```
### Available BLE operations in our Lib:
1. StartScan()
2. StopScan()
3. Connect()
4. Disconnect()
5. WriteToCharacteristic()
6. ReadFromCharacteristic()
7. EnableNotifications()
8. DisableNotifications()
9. GetBatteryLevel()
10. UnBondDeviceFromPhone()
11. Retard() // just delay, added for not repeating with other default Delay classes

Don`t forget add parameters in Operations (like is important or not operation, time delay, sending bytes and etc.)
### Tips
1. DON`T Forget change BLE address from below EXAMPLE :) I am not joke, if you try this - you just dont may connect and make another operations
2. You just need setup in commands uuid of characteristics, NOT uuid of services (because lib can find needed characteristic without knowing services)


Example of BLE Server in Arduino (maybe needed for debugging):

https://wikihandbk.com/wiki/ESP32:%D0%9F%D1%80%D0%B8%D0%BC%D0%B5%D1%80%D1%8B/Bluetooth_Low_Energy:_%D1%83%D0%B2%D0%B5%D0%B4%D0%BE%D0%BC%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F

### Contribute 


### License

This software is available under the Apache License 2.0, allowing you to use the library in your applications. Free to any kind use.

If you want to help (or have some suggestions) with the open source project, please contact😉: arsen@revolna.com

