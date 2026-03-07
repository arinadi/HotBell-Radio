package com.hotbell.radio.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class GithubUpdater(private val context: Context) {

    private val client = OkHttpClient()
    private val repoOwner = "arinadi"
    private val repoName = "HotBell-Radio"
    private val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    suspend fun checkAndInstallUpdate(manual: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Get Latest Release
                val request = Request.Builder()
                    .url("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("GithubUpdater", "Failed to check update: ${response.code}")
                    if (manual) showToast("Failed to check update")
                    return@withContext
                }

                val json = JsonParser.parseString(response.body?.string()).asJsonObject
                val latestTag = json.get("tag_name").asString
                
                val cleanTag = latestTag.removePrefix("v")
                
                if (cleanTag != currentVersion) {
                    Log.i("GithubUpdater", "Update found: $latestTag (Current: $currentVersion)")
                    showToast("Downloading update: $latestTag")
                    
                    // 2. Find APK asset
                    val assets = json.getAsJsonArray("assets")
                    var apkUrl: String? = null
                    var apkName: String? = null
                    
                    for (asset in assets) {
                        val name = asset.asJsonObject.get("name").asString
                        if (name.endsWith("-release.apk")) {
                            apkUrl = asset.asJsonObject.get("browser_download_url").asString
                            apkName = name
                            break
                        }
                    }

                    if (apkUrl != null && apkName != null) {
                        downloadAndInstall(apkUrl, apkName)
                    } else {
                        Log.e("GithubUpdater", "No release APK found in assets")
                        if (manual) showToast("No release APK found")
                    }
                } else {
                    Log.i("GithubUpdater", "App is up to date")
                    if (manual) showToast("App is up to date")
                }

            } catch (e: Exception) {
                Log.e("GithubUpdater", "Update check failed", e)
                if (manual) showToast("Update check failed")
            }
        }
    }

    private suspend fun downloadAndInstall(url: String, fileName: String) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e("GithubUpdater", "Download failed")
                return
            }

            val file = File(context.externalCacheDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(response.body?.bytes())
            fos.close()

            Log.i("GithubUpdater", "Download complete: ${file.absolutePath}")
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Download complete. Installing...", Toast.LENGTH_LONG).show()
                installApk(file)
            }

        } catch (e: Exception) {
            Log.e("GithubUpdater", "Download failed", e)
        }
    }

    private fun installApk(file: File) {
        try {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Log.w("GithubUpdater", "Requesting install permission")
                Toast.makeText(context, "Please allow 'Install unknown apps' permission", Toast.LENGTH_LONG).show()
                
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:${context.packageName}")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("GithubUpdater", "Install failed", e)
            Toast.makeText(context, "Install failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
