# ChillOut with Bluetooth Low Energy in Android!

<table style= padding:10px">
  <tr>
    <td>  <img src="./Docs/img1.png"  alt="1" width = 400px height = 100px ></td>
  </tr>
</table>

### For What?
---

With ChillOutBLE Library you can setup ble connection, writing, reading characteristics, make enable/disable of notifications in few minutes :) Based on Coroutines and Flow.

Bonus functional: unbondig, default method to know battery level and etc.

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
2. DON`T Forget change BLE address from below EXAMPLE !!
```kotlin

 /**
 * Simple initilizing in Activity:
 */ 
val bleStarter = BLEStarter(this)

CoroutineScope(lifecycleScope.coroutineContext).launch { 
    BLEStarter.bleCommandTrain.emit(mutableListOf(
        StartScan(), 
        DelayOpera(6000L), 
        Connect("44:44:44:44:44:0C", isImportant = true), 
        StopScan(), 
        ReadFromCharacteristic("44:44:44:44:44:0C",UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"), isImportant = true),
        EnableNotifications("44:44:44:44:44:0C",UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8"), isImportant = true),
        EnableNotifications("44:44:44:44:44:0C", UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8"), isImportant = true))
    )
}

```
#Tips
1. DON`T Forget change BLE address from below EXAMPLE :) I am not joke, if you try this - you just dont may connect and make another operations
2. 


Example of BLE Server in Arduino (maybe needed for debugging):

https://wikihandbk.com/wiki/ESP32:%D0%9F%D1%80%D0%B8%D0%BC%D0%B5%D1%80%D1%8B/Bluetooth_Low_Energy:_%D1%83%D0%B2%D0%B5%D0%B4%D0%BE%D0%BC%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F
