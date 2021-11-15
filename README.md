## Kiosk mode app
###### Simple Android web-view-based application.
###### The primary purpose is to show the website in Kiosk mode.

By default, the app works in Screen pinning mode. Users can unpin the app by holding recent and home buttons down for a while. In this case, a pin or pattern lock is required for the device.

To forbid any possible system buttons interactions, need to mark the app as device owner by the next adb command:
```
adb shell dpm set-device-owner com.kiosk.kiosk/.AdminReceiver
```
Inspired by [this article](https://snow.dog/blog/kiosk-mode-android/).
