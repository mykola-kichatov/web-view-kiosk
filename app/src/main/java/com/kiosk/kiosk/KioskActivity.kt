package com.kiosk.kiosk

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.kiosk.adminlocktasktest.R


class KioskActivity : AppCompatActivity() {

    companion object {
        const val URL = "https://android.com"
    }

    private lateinit var webView: WebView
    private lateinit var reloadOnConnected: ReloadOnConnected
    private lateinit var adminComponentName: ComponentName
    private lateinit var policyManager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showInFullScreen(findViewById(R.id.root))
        initVars()
        setupWebView()
        listenToConnectionChange()
    }

    override fun onResume() {
        super.onResume()
        setupKiosk()
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack())
            webView.goBack()
        else
            super.onBackPressed()
    }

    override fun onDestroy() {
        if (::reloadOnConnected.isInitialized)
            reloadOnConnected.onActivityDestroy()
        super.onDestroy()
    }

    private fun showInFullScreen(rootView: View) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, rootView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    private fun initVars() {
        webView = findViewById(R.id.webView)
        reloadOnConnected = ReloadOnConnected(webView)
        adminComponentName = AdminReceiver.getComponentName(this)
        policyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    private fun setupWebView() {
        webView.webViewClient = WebViewClient()
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            userAgentString = "Android"
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isScrollbarFadingEnabled = false
        webView.loadUrl(URL)
    }

    private fun listenToConnectionChange() = reloadOnConnected.onActivityCreate(this)

    private fun setupKiosk() {
        if (policyManager.isDeviceOwnerApp(packageName)) {
            setupAutoStart()
            stayAwake()
            policyManager.setLockTaskPackages(adminComponentName, arrayOf(packageName))
        }
        startLockTask()
    }

    private fun setupAutoStart() {
        val intentFilter = IntentFilter(Intent.ACTION_MAIN)
        intentFilter.addCategory(Intent.CATEGORY_HOME)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        policyManager.addPersistentPreferredActivity(
            adminComponentName,
            intentFilter, ComponentName(packageName, KioskActivity::class.java.name)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            policyManager.setKeyguardDisabled(adminComponentName, true)
    }

    private fun stayAwake() {
        policyManager.setGlobalSetting(
            adminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            (BatteryManager.BATTERY_PLUGGED_AC
                    or BatteryManager.BATTERY_PLUGGED_USB
                    or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        )
    }

}