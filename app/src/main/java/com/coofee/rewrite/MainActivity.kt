package com.coofee.rewrite

import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.coofee.rewrite.hook.sharedpreferences.ShadowSharedPreferences
import com.coofee.rewrite.hook.sharedpreferences.SharedPreferencesProxy
import java.net.NetworkInterface

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test_Context_getSharedPreferences_string()

        test_Activity_getSharedPreferences_int()

        test_PreferenceManager_getSharedPreferences_string()

        SharedPreferencesProxy.print()

        testPackageManager()

        testQueryContacts()

        testGetDeviceId()
    }

    private fun test_Context_getSharedPreferences_string() {
        getSharedPreferences("sp_context", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("test_context_getSharedPreferences_string", true)
            .commit()
    }

    private fun test_Activity_getSharedPreferences_int() {
        getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putBoolean("test_Activity_getSharedPreferences_int", true)
            .commit()
    }

    private fun test_PreferenceManager_getSharedPreferences_string() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean("test_PreferenceManager_getSharedPreferences_string", true)
            .commit()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val defaultSharedPreferencesName = PreferenceManager.getDefaultSharedPreferencesName(this)
        }
    }

    private fun test_shadow_preferences() {
        ShadowSharedPreferences.getSharedPreferences(this, "shadow", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("test_shadow_preferences", true)
            .commit()
    }

    private fun testPackageManager() {
        val installedApplications = packageManager.getInstalledApplications(0)
        Log.e("rewrite", "testPackageManager; installedApplications.size=${installedApplications.size}")
        if (installedApplications.size > 0) {
            val applicationInfo = installedApplications[0]
            Log.e(
                "rewrite",
                "testPackageManager; name=${applicationInfo.name}, packageName=${applicationInfo.packageName}"
            )
        }

        val installedPackages = packageManager.getInstalledPackages(0)
        Log.e("rewrite", "testPackageManager; installedPackages.size=${installedPackages.size}")
        if (installedApplications.size > 0) {
            val packageInfo = installedPackages[0]
            Log.e(
                "rewrite",
                "testPackageManager; packageName=${packageInfo.packageName}, versionName=${packageInfo.versionName}"
            )
        }
    }

    private fun testQueryContacts() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    val cursor = this.contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI, arrayOf(
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME
                        ), null, null, null
                    );
                    cursor?.close();
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun testGetDeviceId() {
        try {
            val telephonyManager = getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            val deviceId = telephonyManager.getDeviceId()
            println("deviceId=$deviceId")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun testGetMacAddress() {
        val wifiManager = applicationContext.getSystemService(Service.WIFI_SERVICE) as WifiManager
        println("wifiManager.connectionInfo.macAddress=${wifiManager.connectionInfo.macAddress}")

        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        for (networkInterface in networkInterfaces) {
            println("networkInterface.hardwareAddress=${networkInterface.hardwareAddress}")
        }
    }

}