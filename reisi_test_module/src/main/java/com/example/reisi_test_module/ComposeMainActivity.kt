package com.example.reisi_test_module

import android.app.admin.DevicePolicyManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.reisi_test_module.ui.RootView

class ComposeMainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mDevicePolicyManager = this.getSystemService(DevicePolicyManager::class.java)
        val currentNearbyStreamingPolicy = mDevicePolicyManager.nearbyAppStreamingPolicy
        val provisionMode = getProvisionMode(mDevicePolicyManager)

        setContent {
            RootView(
                provisionMode = provisionMode,
                currentStreamingState = currentNearbyStreamingPolicy,
            ) { selectedStreamingState ->
                mDevicePolicyManager.nearbyAppStreamingPolicy = selectedStreamingState
                finish()
            }
        }
    }

    private fun getProvisionMode(
        mDevicePolicyManager: DevicePolicyManager
    ): String {
        return if (mDevicePolicyManager.isDeviceOwnerApp(this.packageName)) {
            "Device owner"
        } else if (mDevicePolicyManager.isProfileOwnerApp(this.packageName)) {
            "Profile owner"
        } else {
            "App is not administrator"
        }
    }
}