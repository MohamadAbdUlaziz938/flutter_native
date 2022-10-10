package com.examples.flutter_native

import android.R
import android.app.Activity
import android.content.*
import android.os.*
import android.view.View
import android.widget.TextView
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity() {
    private val BATTERY_CHANNEL = "battery"
    private val CHARGING_CHANNEL = "charging"
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BATTERY_CHANNEL)
        eventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, CHARGING_CHANNEL)

        channel.setMethodCallHandler { call, result ->
            if (call.method == "getBatteryLevel") {
                result.success(getBatteryLevel(getContext()))
            }
        }
        eventChannel.setStreamHandler(MyStreamHandler(getContext()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            channel.invokeMethod("reportBatteryLevel", getBatteryLevel(getContext()))
        }, 0)
    }

    private fun getBatteryLevel(context: Context): Int {
        val batterLevel: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batterLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            var intent = ContextWrapper(context).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level / scale.toDouble()
            batterLevel = (batteryPct * 100).toInt()
        }
        return batterLevel
    }
}

class MyStreamHandler(private val context: Context) : EventChannel.StreamHandler {
    private var receiver: BroadcastReceiver? = null
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        if (events == null) return
        receiver = initReceiver(events)
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    }

    override fun onCancel(arguments: Any?) {
        context.unregisterReceiver(receiver)
        receiver = null

    }

    private fun initReceiver(events: EventChannel.EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val status = p1?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                if (status == null || status == -1)
                    return
                when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> events.success("battery is charging")
                    BatteryManager.BATTERY_STATUS_FULL -> events.success("battery is full charged")
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> events.success("battery is  discharging")
                }
                
            }
        }
    }

}