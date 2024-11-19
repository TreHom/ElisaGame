package com.example.elisagame

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionsHandler(
    private val activity: ComponentActivity,
    private val onPermissionGranted: () -> Unit
) {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    init {
        // Initialize permission launcher
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                onPermissionGranted()
            } else {
                Toast.makeText(activity, "Some permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkPermission(): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermission() {
        val permissions = getRequiredPermissions()
        val rationaleNeeded = permissions.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }

        if (rationaleNeeded) {
            Toast.makeText(activity, "Permissions are needed to access the gallery.", Toast.LENGTH_LONG).show()
        } else if (permissions.any { permission ->
                ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED &&
                        !activity.shouldShowRequestPermissionRationale(permission)
            }) {
            handlePermissionManually()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun handlePermissionManually() {
        Toast.makeText(activity, "Please enable permissions in settings.", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }

    private fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
