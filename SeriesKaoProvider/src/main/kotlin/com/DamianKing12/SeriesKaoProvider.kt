package com.DamianKing12

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class SeriesKaoProvider : MainAPI() {
    override var name = "Series Kao Indexador"
    override var mainUrl = "https://serieskao.top"
    override var supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "es" // Añadimos el idioma explícitamente

    override suspend fun search(query: String): List<SearchResponse> {
        // Limpiamos la query para evitar errores con espacios
        val url = "$mainUrl/?s=${query.replace(" ", "+")}"
        val document = app.get(url).document

        return document.select("div.result-item").mapNotNull {
            val titleElement = it.selectFirst("div.title a")
            val title = titleElement?.text() ?: return@mapNotNull null
            val href = titleElement.attr("href")

            // Buscamos el poster en src o data-src por si hay lazy-loading
            val img = it.selectFirst("img")
            val poster = img?.attr("data-src")?.ifBlank { img.attr("src") }

            newMovieSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document

        // Buscamos iframes de video y también enlaces de servidores en botones
        // Muchos sitios ocultan el link real en un atributo data-url o similares
        doc.select("iframe, a[href*='embed'], li.dooplay_player_option").amap {
            var iframeUrl = it.attr("src").ifBlank {
                it.attr("data-url").ifBlank { it.attr("href") }
            }

            if (iframeUrl.isNotEmpty()) {
                if (iframeUrl.startsWith("//")) {
                    iframeUrl = "https:$iframeUrl"
                }

                // Solo intentamos extraer si es un link válido
                if (iframeUrl.startsWith("http")) {
                    loadExtractor(iframeUrl, data, subtitleCallback, callback)
                }
            }
        }

        return true
    }
}
