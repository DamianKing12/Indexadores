package com.DamianKing12

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class SeriesKaoPlugin: Plugin() {
    override fun load(context: Context) {
        // Todos los nombres de las API deben estar aquí
        registerMainAPI(SeriesKaoProvider())
    }
}
