package com.examples.flutter_native

import android.R
import android.app.Activity
import android.content.*
import android.os.*
import android.view.View
import android.widget.TextView
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity() {
    private val BATTERY_CHANNEL = "battery"
    private lateinit var channel: MethodChannel
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BATTERY_CHANNEL)
        channel.setMethodCallHandler { call, result ->
            if (call.method == "getBatteryLevel") {
                result.success(getBatteryLevel(getContext()))
            }
        }

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

class Main : Activity() {

    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context?, intent: Intent) {
            // TODO Auto-generated method stub
            val level = intent.getIntExtra("level", 0)

        }
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)


        this.registerReceiver(mBatInfoReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
}