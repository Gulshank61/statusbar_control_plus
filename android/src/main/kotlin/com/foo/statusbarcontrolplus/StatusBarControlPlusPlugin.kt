package com.foo.statusbarcontrolplus

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** StatusBarControlPlusPlugin */
class StatusBarControlPlusPlugin :
    FlutterPlugin, ActivityAware,
    MethodCallHandler {
    // The MethodChannel that will the communication between Flutter and native Android
    //
    // This local reference serves to register the plugin with the Flutter Engine and unregister it
    // when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity:Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.d("StatusBarControlPlus", "StatusBarControlPlus: Attached to Flutter Engine")
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "status_bar_control_plus")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        Log.d("StatusBarControlPlus", "StatusBarControlPlus: Detached from Flutter Engine")
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Log.d("StatusBarControlPlus", "StatusBarControlPlus: Attached to Activity")
        activity = binding.activity;
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.d("StatusBarControlPlus", "StatusBarControlPlus: Detached from Activity for Config changes")
        activity = null;
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.d("StatusBarControlPlus", "StatusBarControlPlus: Reattached to Activity for Config changes")
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        Log.d("StatusBarControlPlus", "StatusBarControlPlus: Detached from Activity")
        activity = null;
    }

    override fun onMethodCall(
        call: MethodCall,
        result: Result
    ) {
        when(call.method){
            "getPlatformVersion" -> result.success("Android ${Build.VERSION.RELEASE}")
            "setColor" -> handleSetColor(call, result)
            "setTranslucent" -> handleSetTranslucent(call, result)
            "setHidden" -> handleSetHidden(call, result)
            "setStyle" -> handleSetStyle(call, result)
            "getHeight" -> handleGetHeight(result)
            "setNetworkActivityIndicatorVisible" -> result.success(true)
            "setNavigationBarColor" -> handleSetNavigationBarColor(call, result)
            "setNavigationBarStyle" -> handleSetNavigationBarStyle(call, result)
            else -> result.notImplemented()
        }
    }

    private fun handleSetColor(call: MethodCall, result: Result) {
        val act = activity ?: return errorActivity(result)

            val color = (call.argument<Number>("color") ?: 0).toInt()
            val animated = call.argument<Boolean>("animated") ?: false

            act.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            if (animated) {
                val curColor = act.window.statusBarColor
                val colorAnimation =
                    ValueAnimator.ofObject(ArgbEvaluator(), curColor, color).apply {
                        addUpdateListener { animator ->
                            act.window.statusBarColor = animator.animatedValue as Int
                        }
                        duration = 300
                        startDelay = 0
                    }
                colorAnimation.start()
            } else {
                act.window.statusBarColor = color
            }
            result.success(true)

    }

    private fun handleSetTranslucent(call: MethodCall, result: Result) {
        val act = activity ?: return errorActivity(result)

            val translucent = call.argument<Boolean>("translucent") ?: false
            val decorView = act.window.decorView
            if (translucent) {
                decorView.setOnApplyWindowInsetsListener { v, insets ->
                    val defaultInsets = v.onApplyWindowInsets(insets)
                    defaultInsets.replaceSystemWindowInsets(
                        defaultInsets.systemWindowInsetLeft, 0,
                        defaultInsets.systemWindowInsetRight, defaultInsets.systemWindowInsetBottom
                    )
                }
            } else {
                decorView.setOnApplyWindowInsetsListener(null)
            }
            ViewCompat.requestApplyInsets(decorView)
            result.success(true)
    }

    private fun handleSetHidden(call: MethodCall, result: Result) {
        val act = activity ?: return errorActivity(result)

        val hidden = call.argument<Boolean>("hidden") ?: false
        if (hidden) {
            act.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            act.window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            act.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            act.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        result.success(true)
    }

    private fun handleSetStyle(call: MethodCall, result: Result) {
        val act = activity ?: return errorActivity(result)

            val style = call.argument<String>("style")
            val decorView = act.window.decorView
            var flags = decorView.systemUiVisibility
            flags = if (style == "dark-content") {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = flags
            result.success(true)

    }

    private fun handleGetHeight(result: Result) {
        val act = activity ?: return errorActivity(result)

        var height = 0
        val resourceId = act.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            height = act.resources.getDimensionPixelSize(resourceId)
        }
        result.success(toDIPFromPixel(height).toDouble())
    }

    private fun handleSetNavigationBarColor(call: MethodCall, result: Result) {
        val act = activity ?: return errorActivity(result)

        val color = (call.argument<Number>("color") ?: 0).toInt()
        val animated = call.argument<Boolean>("animated") ?: false

        if (animated) {
            val curColor = act.window.navigationBarColor
            val colorAnimation =
                ValueAnimator.ofObject(ArgbEvaluator(), curColor, color).apply {
                    addUpdateListener { animator ->
                        act.window.navigationBarColor = animator.animatedValue as Int
                    }
                    duration = 300
                    startDelay = 0
                }
            colorAnimation.start()
        } else {
            act.window.navigationBarColor = color
        }
        result.success(true)
    }

    private fun handleSetNavigationBarStyle(call: MethodCall, result: Result) {
        val act = activity ?: return errorActivity(result)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val style = call.argument<String>("style")
            val decorView = act.window.decorView
            var flags = decorView.systemUiVisibility
            flags = if (style == "dark") {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            } else {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            decorView.systemUiVisibility = flags
            result.success(true)
        } else {
            result.error("StatusBarControl",
                "Cannot change navigation bar style in pre O versions", null)
        }
    }

    private fun toDIPFromPixel(pixel: Int): Int {
        val scale = getDensity()
        return ((pixel - 0.5f) / scale).toInt()
    }

    private fun getDensity(): Float {
        return activity?.resources?.displayMetrics?.density ?: 1f
    }

    private fun errorActivity(result: Result): Nothing {
        Log.e("StatusBarControlPlus",
            "StatusBarControlPlus: Ignored status bar change, current activity is null.")
        result.error("StatusBarControlPlus",
            "StatusBarControlPlus: Ignored status bar change, current activity is null.", null)
        throw IllegalStateException("Activity is null")
    }


}
