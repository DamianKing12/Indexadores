package com.DamianKing12

import android.content.Context
import com.lagradost.cloudstream3.CloudstreamPlugin
import com.lagradost.cloudstream3.Plugin

@CloudstreamPlugin
class CuevanaPlugin : Plugin() {
    override fun load(context: Context) {
        // Plugin indexador, no necesita inicialización
    }
}
