package com.DamianKing12

import com.lagradost.cloudstream3.*
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin // IMPORTANTE
@CloudstreamPlugin

class CuevanaProvider : MainAPI() {
    override var name = "Cuevana 3"
    override var mainUrl = "https://cue.cuevana3.nu"
    override var lang = "es"
    override var supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=${query.replace(" ", "+")}"
        val document = app.get(url).document
        return document.select("ul.results-post article, div.result-item").mapNotNull {
            val title = it.selectFirst("h2, .title")?.text() ?: return@mapNotNull null
            val href = fixUrl(it.selectFirst("a")?.attr("href") ?: return@mapNotNull null)
            val posterUrl = fixUrl(it.selectFirst("img")?.attr("src") ?: "")

            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
            }
        }
    }
}