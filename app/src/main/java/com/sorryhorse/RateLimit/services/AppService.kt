package com.sorryhorse.RateLimit.services

import android.content.Context
import android.content.Intent
import com.sorryhorse.RateLimit.models.App

class AppService(context: Context) : ArrayList<App>(){
    init {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        for (packageInfo in resolveInfos) {
            val app = App(
                packageInfo.loadLabel(pm) as String,
                packageInfo.loadIcon(pm),
                packageInfo.activityInfo.packageName
            )
            this.add(app)
        }
    }
}