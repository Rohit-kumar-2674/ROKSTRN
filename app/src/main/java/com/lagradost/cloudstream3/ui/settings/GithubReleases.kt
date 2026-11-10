package com.lagradost.cloudstream3.ui.settings

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.RokstrnConfig
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream4.viewmodels.GithubFile
import com.lagradost.cloudstream4.viewmodels.GithubRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.FileNotFoundException

object GithubReleases : GithubRepository {
    @Serializable
    private data class GithubAsset(
        @JsonProperty("name") @SerialName("name") val name: String,
        @JsonProperty("size") @SerialName("size") val size: Int, // Size in bytes
        @JsonProperty("browser_download_url") @SerialName("browser_download_url") val browserDownloadUrl: String,
        @JsonProperty("content_type") @SerialName("content_type") val contentType: String, // application/vnd.android.package-archive
        @JsonProperty("digest") @SerialName("digest") val digest: String? = null, // sha256:..., may be null
    )

    @Serializable
    private data class GithubRelease(
        @JsonProperty("tag_name") @SerialName("tag_name") val tagName: String, // Version code
        @JsonProperty("body") @SerialName("body") val body: String, // Description
        @JsonProperty("assets") @SerialName("assets") val assets: List<GithubAsset>,
        @JsonProperty("target_commitish") @SerialName("target_commitish") val targetCommitish: String, // Branch
        @JsonProperty("prerelease") @SerialName("prerelease") val prerelease: Boolean,
        @JsonProperty("node_id") @SerialName("node_id") val nodeId: String,
        @JsonProperty("created_at") @SerialName("created_at") val createdAt: String, // YYYY-MM-DDTHH:MM:SSZ
    )

    @Serializable
    private data class GithubObject(
        @JsonProperty("sha") @SerialName("sha") val sha: String, // SHA-256 hash
        //@JsonProperty("type") @SerialName("type") val type: String,
        ///@JsonProperty("url") @SerialName("url") val url: String,
    )

    @Serializable
    private data class GithubTag(
        //@JsonProperty("node_id") @SerialName("node_id") val nodeId: String,
        @JsonProperty("object") @SerialName("object") val githubObject: GithubObject,
    )

    const val APK_PRERELEASE = "pre-release"
    const val APK_CONTENT_TYPE = "application/vnd.android.package-archive"

    private val defaultHeaders = mapOf("Accept" to "application/vnd.github.v3+json")

    private val releaseTagPattern = Regex("""v\d+\.\d+\.\d+-rokstrn\.\d+""")
    private val sha256Pattern = Regex("sha256:[0-9a-fA-F]{64}")

    @Throws
    override suspend fun getSha(
        userName: String,
        repository: String,
        tag: String,
    ): String {
        check(RokstrnConfig.updateFeedUrl != null && "$userName/$repository" == RokstrnConfig.repository)
        return app.get(
            url = "https://api.github.com/repos/$userName/$repository/git/ref/tags/$tag",
            headers = defaultHeaders
        ).parsed<GithubTag>().githubObject.sha
    }

    @Throws
    override suspend fun getRelease(
        userName: String,
        repository: String,
        prerelease: Boolean,
        prereleaseTag: String,
        contentType: String,
    ): GithubFile {
        check(!prerelease && "$userName/$repository" == RokstrnConfig.repository)
        val latestReleaseUrl = checkNotNull(RokstrnConfig.updateFeedUrl)

        val latestRelease = app.get(
            url = latestReleaseUrl,
            headers = defaultHeaders
        ).parsed<GithubRelease>()

        val tag = latestRelease.tagName
        check(releaseTagPattern.matches(tag) && !latestRelease.prerelease)
        val assetName = "${RokstrnConfig.RELEASE_ASSET_PREFIX}$tag-release.apk"
        val downloadUrl = "${RokstrnConfig.releasesUrl}/download/$tag/$assetName"
        val foundAsset = latestRelease.assets.firstOrNull { asset ->
            asset.contentType == contentType &&
                asset.name == assetName &&
                asset.browserDownloadUrl == downloadUrl &&
                asset.digest?.matches(sha256Pattern) == true
        }

        if (foundAsset == null) {
            throw FileNotFoundException()
        }

        return GithubFile(
            digest = foundAsset.digest,
            downloadUrl = foundAsset.browserDownloadUrl,
            displayName = tag.removePrefix("v"),
            changeLog = latestRelease.body,
            tagName = latestRelease.tagName,
            nodeId = latestRelease.nodeId
        )
    }
}