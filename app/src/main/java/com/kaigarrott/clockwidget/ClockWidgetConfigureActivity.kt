package com.kaigarrott.clockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton

/**
 * The configuration screen for the [ClockWidget] AppWidget.
 */
class ClockWidgetConfigureActivity : Activity() {
    internal var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    internal var mIs24h: Boolean = false
    internal var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@ClockWidgetConfigureActivity

        // When the button is clicked, store the value locally
        saveFormatPref(context, mAppWidgetId, mIs24h)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        ClockWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.clock_widget_configure)
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        when(loadFormatPref(this@ClockWidgetConfigureActivity, mAppWidgetId)) {
            true ->
                findViewById<RadioButton>(R.id.clock_format_24).setChecked(true)
            else ->
                findViewById<RadioButton>(R.id.clock_format_12).setChecked(true)
        }

    }

    fun formatTime(view: View) {
        if(view is RadioButton) {
            when(view.id) {
                R.id.clock_format_12 ->
                    if(view.isChecked) {
                        mIs24h = false
                    }
                R.id.clock_format_24 ->
                    if(view.isChecked) {
                        mIs24h = true
                    }
            }
        }

    }

    companion object {

        private val PREFS_NAME = "com.kaigarrott.clockwidget.ClockWidget"
        private val PREF_PREFIX_KEY = "appwidget_24h_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveFormatPref(context: Context, appWidgetId: Int, is24h: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putBoolean(PREF_PREFIX_KEY + appWidgetId, is24h)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        internal fun loadFormatPref(context: Context, appWidgetId: Int): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val is24h = prefs.getBoolean(PREF_PREFIX_KEY + appWidgetId, false)
            return is24h
        }

        internal fun deleteFormatPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }

    }
}

