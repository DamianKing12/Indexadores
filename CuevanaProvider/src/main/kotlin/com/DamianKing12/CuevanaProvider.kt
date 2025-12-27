package com.DamianKing12

import com.lagradost.cloudstream3.*
import org.jsoup.nodes.Element

class CuevanaProvider : MainAPI() {
    override var mainUrl = "https://cue.cuevana3.nu"
    override var name = "Cuevana 3 Indexador"
    override val hasMainPage = true
    override var lang = "es"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("ul.results-post article, div.result-item").mapNotNull {
            it.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2, .title")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrl(this.selectFirst("img")?.attr("src") ?: "")

        return if (href.contains("/serie/")) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1.Title")?.text() ?: ""
        val poster = document.selectFirst(".Image img")?.attr("src")
        val plot = document.selectFirst(".Description p")?.text()

        val type = if (url.contains("/serie/")) TvType.TvSeries else TvType.Movie

        return if (type == TvType.TvSeries) {
            val episodes = document.select("ul.EpisodesList li").mapNotNull {
                val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst(".name")?.text() ?: "Episodio"
                val seasonNum = it.selectFirst(".season")?.text()?.toIntOrNull()
                val episodeNum = it.selectFirst(".episode")?.text()?.toIntOrNull()

                newEpisode(href) {
                    this.name = name
                    this.season = seasonNum
                    this.episode = episodeNum
                }
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }
}
