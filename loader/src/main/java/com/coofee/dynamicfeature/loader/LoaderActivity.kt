package com.coofee.dynamicfeature.loader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class LoaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)

        packageManager.getInstalledPackages(0)?.forEach {
            Log.e("LoaderActivity", it.toString())
        }
    }
}