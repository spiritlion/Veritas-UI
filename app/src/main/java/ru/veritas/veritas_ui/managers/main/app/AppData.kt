package ru.veritas.veritas_ui.managers.main.app

import android.graphics.drawable.Drawable

data class AppData(
    val appName: String,
    val icon: Drawable,
    val packageName: String,
    val isEnabled: Boolean
)