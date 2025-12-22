override suspend fun loadLinks(
    data: String,
    isCasting: Boolean,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
): Boolean {
    val doc = app.get(data, headers = headers).document

    // 1️⃣ SUBTÍTULOS (ya funcionaba)
    doc.select("track[kind=subtitles]").forEach { track ->
        val src = track.attr("src")
        if (src.isNotBlank()) {
            subtitleCallback(
                newSubtitleFile(
                    track.attr("srclang") ?: "es",
                    src
                )
            )
        }
    }

    // 2️⃣ IFRAMES (donde está el reproductor real)
    doc.select("iframe").forEach { iframe ->
        val src = iframe.attr("src")
        if (src.isNotBlank()) {
            callback(
                newExtractorLink(
                    name = "iframe",
                    source = "iframe",
                    url = src
                ).apply {
                    this.referer = mainUrl
                    this.isM3u8 = false
                }
            )
        }
    }

    // 3️⃣ MASTER.TXT (índice HLS)
    val masterScript = doc.select("script").map { it.data() }.firstOrNull { it.contains("master.txt") }
    if (masterScript != null) {
        val masterUrl = Regex("""(https?://[^"'\s]+master\.txt)""").find(masterScript)?.value
        if (masterUrl != null) {
            callback(
                newExtractorLink(
                    name = "HLS",
                    source = "HLS",
                    url = masterUrl
                ).apply {
                    this.referer = mainUrl
                    this.isM3u8 = true // Es índice HLS
                }
            )
        }
    }

    // 4️⃣ SERVIDORES EN SCRIPT (por si aún existen)
    val scriptElement = doc.selectFirst("script:containsData(var servers =)")
    if (scriptElement != null) {
        val serversJson = scriptElement.data().substringAfter("var servers = ").substringBefore(";").trim()
        return try {
            val servers = AppUtils.parseJson<List<ServerData>>(serversJson)
            servers.forEach { server ->
                val cleanUrl = server.url.replace("\\/", "/")
                callback(
                    newExtractorLink(
                        name = server.title,
                        source = server.title,
                        url = cleanUrl
                    ).apply {
                        this.quality = getQuality(server.title)
                        this.isM3u8 = cleanUrl.contains(".m3u8", ignoreCase = true)
                        this.referer = mainUrl
                    }
                )
            }
            servers.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    return doc.select("iframe").isNotEmpty() || masterScript != null
}
