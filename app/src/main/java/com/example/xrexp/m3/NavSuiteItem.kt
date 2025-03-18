package com.example.xrexp.m3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavSuiteItem {
    HOME,
    SETTINGS,
}

val NavSuiteItem.label: String
    get() =
        when (this) {
            NavSuiteItem.HOME -> "Home"
            NavSuiteItem.SETTINGS -> "Settings"
        }

val NavSuiteItem.icon: ImageVector
    get() =
        when (this) {
            NavSuiteItem.HOME -> Icons.Default.Home
            NavSuiteItem.SETTINGS -> Icons.Default.Settings
        }
