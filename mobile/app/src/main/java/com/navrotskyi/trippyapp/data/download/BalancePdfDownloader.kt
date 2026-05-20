package com.navrotskyi.trippyapp.data.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.navrotskyi.trippyapp.BuildConfig
import com.navrotskyi.trippyapp.api.TokenManager

sealed class DownloadResult {
    data class Started(val downloadId: Long) : DownloadResult()
    object NoAuth : DownloadResult()
    object NoBaseUrl : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

object BalancePdfDownloader {

    fun download(context: Context, tripId: String): DownloadResult {
        val token = TokenManager.getToken()?.takeIf { it.isNotBlank() }
            ?: return DownloadResult.NoAuth

        val baseUrl = BuildConfig.BASE_URL.trimEnd('/').takeIf { it.isNotBlank() }
            ?: return DownloadResult.NoBaseUrl

        val url = "$baseUrl/api/trips/$tripId/balances/export"
        val fileName = "rozliczenia-$tripId.pdf"

        return try {
            val request = DownloadManager.Request(Uri.parse(url))
                .addRequestHeader("Authorization", "Bearer $token")
                .setMimeType("application/pdf")
                .setTitle("Raport rozliczeń")
                .setDescription("Pobieranie raportu PDF")
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "Trippy/$fileName"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            DownloadResult.Started(manager.enqueue(request))
        } catch (e: Exception) {
            DownloadResult.Error(e.localizedMessage ?: "Nieznany błąd pobierania")
        }
    }
}
