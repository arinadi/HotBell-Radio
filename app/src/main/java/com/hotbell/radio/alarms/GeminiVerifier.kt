package com.hotbell.radio.alarms

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class VerificationResult(
    val match: Boolean,
    val reason: String
)

class GeminiVerifier(private val apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun verifyMatch(targetImagePath: String, capturedImagePath: String): VerificationResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext VerificationResult(false, "API Key is missing. Please configure it in Settings.")
        }

        val targetFile = File(targetImagePath)
        val capturedFile = File(capturedImagePath)

        if (!targetFile.exists() || !capturedFile.exists()) {
            return@withContext VerificationResult(false, "One or both images are missing.")
        }

        val targetBitmap = BitmapFactory.decodeFile(targetImagePath)
        val capturedBitmap = BitmapFactory.decodeFile(capturedImagePath)

        if (targetBitmap == null || capturedBitmap == null) {
            return@withContext VerificationResult(false, "Failed to decode images.")
        }

        try {
            val prompt = """
                Compare these two images. 
                Image 1 is the 'target' reference image. 
                Image 2 is the 'captured' image just taken by the user to prove they are awake.
                
                Are they pictures of the EXACT same object or location? 
                Ignore minor lighting differences or slight angle changes, but ensure they represent the same subject matter (e.g. the same bathroom sink, the same coffee maker).
                
                Reply ONLY with a JSON object in this format:
                {
                  "match": true/false,
                  "reason": "Brief 1-sentence explanation"
                }
            """.trimIndent()

            val inputContent = content {
                image(targetBitmap)
                image(capturedBitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text ?: ""
            
            // Try to extract JSON from markdown if needed
            val jsonStr = responseText.replace("```json", "").replace("```", "").trim()
            val json = JSONObject(jsonStr)

            val match = json.optBoolean("match", false)
            val reason = json.optString("reason", "No reason provided")

            VerificationResult(match, reason)

        } catch (e: Exception) {
            android.util.Log.e("GeminiVerifier", "Verification failed", e)
            VerificationResult(false, "Verification error: ${e.message}")
        } finally {
            // Free memory
            targetBitmap.recycle()
            capturedBitmap.recycle()
        }
    }
}
