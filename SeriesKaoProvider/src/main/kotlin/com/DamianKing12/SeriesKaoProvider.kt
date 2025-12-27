package com.DamianKing12

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin // <--- IMPORTANTE
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element

@CloudstreamPlugin // <--- ESTA ES LA ETIQUETA QUE FALTABA
class SeriesKaoProvider : MainAPI() {
    override var name = "Series Kao Indexador"
    override var mainUrl = "https://serieskao.top"
    override var supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "es"

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=${query.replace(" ", "+")}"
        val document = app.get(url).document

        return document.select("div.result-item").mapNotNull {
            val titleElement = it.selectFirst("div.title a")
            val title = titleElement?.text() ?: return@mapNotNull null
            val href = titleElement.attr("href")

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

        doc.select("iframe, a[href*='embed'], li.dooplay_player_option").amap {
            var iframeUrl = it.attr("src").ifBlank {
                it.attr("data-url").ifBlank { it.attr("href") }
            }

            if (iframeUrl.isNotEmpty()) {
                if (iframeUrl.startsWith("//")) {
                    iframeUrl = "https:$iframeUrl"
                }

                if (iframeUrl.startsWith("http")) {
                    loadExtractor(iframeUrl, data, subtitleCallback, callback)
                }
            }
        }

        return true
    }
}