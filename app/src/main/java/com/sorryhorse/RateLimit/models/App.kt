package com.sorryhorse.RateLimit.models

import android.graphics.drawable.Drawable
import java.io.Serializable

data class App (
    val title: String,
    val icon: Drawable,
    val id: String
) : Serializable