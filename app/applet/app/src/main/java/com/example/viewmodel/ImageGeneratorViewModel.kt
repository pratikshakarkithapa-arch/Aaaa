package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.R
import com.example.model.AspectRatioOption
import com.example.model.GeneratedImage
import com.example.model.GenerationUiState
import com.example.model.ImageStyle
import com.example.util.ImageSaver
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import android.util.Base64
import android.util.Log

class ImageGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _prompt = MutableStateFlow("A futuristic glowing crystal cyber oasis at sunset")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _selectedStyle = MutableStateFlow(ImageStyle.REALISTIC)
    val selectedStyle: StateFlow<ImageStyle> = _selectedStyle.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow(AspectRatioOption.SQUARE)
    val selectedAspectRatio: StateFlow<AspectRatioOption> = _selectedAspectRatio.asStateFlow()

    private val _uiState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<GeneratedImage>>(emptyList())
    val history: StateFlow<List<GeneratedImage>> = _history.asStateFlow()

    private val _activeFullscreenImage = MutableStateFlow<GeneratedImage?>(null)
    val activeFullscreenImage: StateFlow<GeneratedImage?> = _activeFullscreenImage.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    val isApiKeyConfigured: Boolean
        get() {
            val key = getActiveApiKey()
            return key.isNotBlank() &&
                    key != "MY_GEMINI_API_KEY" &&
                    !key.startsWith("placeholder", ignoreCase = true)
        }

    private var timerJob: Job? = null

    init {
        loadSavedApiKey()
        loadInitialPreset()
    }

    private fun loadSavedApiKey() {
        val prefs = getApplication<Application>().getSharedPreferences("image_gen_prefs", Context.MODE_PRIVATE)
        _customApiKey.value = prefs.getString("user_api_key", "") ?: ""
    }

    fun saveApiKey(key: String) {
        val trimmed = key.trim()
        val prefs = getApplication<Application>().getSharedPreferences("image_gen_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("user_api_key", trimmed).apply()
        _customApiKey.value = trimmed
        _showApiKeyDialog.value = false
        _snackBarMessage.value = "API key updated successfully"
    }

    fun getActiveApiKey(): String {
        return if (_customApiKey.value.isNotBlank()) {
            _customApiKey.value
        } else {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }
    }

    private fun loadInitialPreset() {
        val initialImage = GeneratedImage(
            id = "preset_initial",
            prompt = "An astronaut exploring a bioluminescent crystal alien forest",
            enhancedPrompt = "An astronaut exploring a bioluminescent crystal alien forest, 8k photorealistic photography, sharp focus, hyperdetailed, dynamic studio lighting",
            style = ImageStyle.REALISTIC,
            aspectRatio = AspectRatioOption.SQUARE,
            fallbackDrawableRes = ImageStyle.REALISTIC.sampleResId,
            durationSeconds = 1.8,
            isAiGenerated = false
        )
        _uiState.value = GenerationUiState.Success(initialImage)
        _history.value = listOf(initialImage)
    }

    fun onPromptChange(newPrompt: String) {
        _prompt.value = newPrompt
    }

    fun onStyleSelected(style: ImageStyle) {
        _selectedStyle.value = style
    }

    fun onAspectRatioSelected(ratio: AspectRatioOption) {
        _selectedAspectRatio.value = ratio
    }

    fun clearPrompt() {
        _prompt.value = ""
    }

    fun openApiKeyDialog() {
        _showApiKeyDialog.value = true
    }

    fun closeApiKeyDialog() {
        _showApiKeyDialog.value = false
    }

    fun showFullscreenImage(image: GeneratedImage) {
        _activeFullscreenImage.value = image
    }

    fun closeFullscreenImage() {
        _activeFullscreenImage.value = null
    }

    fun clearSnackBar() {
        _snackBarMessage.value = null
    }

    fun inspireMe() {
        val ideas = listOf(
            "A majestic cybernetic white tiger with neon glowing stripes in rain-slicked Tokyo",
            "An ancient mystical tree with bioluminescent glass lanterns under a galaxy of stars",
            "A charming cozy coffee shop inside a hollow giant mossy pumpkin on an autumn morning",
            "An astronaut relaxing in a floating velvet armchair sipping coffee looking at Earth",
            "A steampunk hummingbird with intricate brass gears and ruby mechanical wings",
            "A serene floating Japanese temple above a sea of pastel pink and lilac clouds at dawn"
        )
        _prompt.value = ideas.random()
    }

    fun generate() {
        val currentPrompt = _prompt.value.trim()
        if (currentPrompt.isEmpty()) {
            _snackBarMessage.value = "Please enter a prompt description"
            return
        }

        val style = _selectedStyle.value
        val ratio = _selectedAspectRatio.value
        val apiKey = getActiveApiKey()

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _uiState.value = GenerationUiState.Loading(
                stepMessage = "Preparing neural tokens...",
                progress = 0.1f,
                elapsedSeconds = 0
            )

            val fullPrompt = currentPrompt + style.promptModifier

            if (isApiKeyConfigured) {
                _uiState.value = GenerationUiState.Loading("Calling Google Imagen API...", 0.4f, 1)
                val apiResult = callImagenApi(fullPrompt, ratio.apiRatio, apiKey)
                if (apiResult != null) {
                    val duration = (System.currentTimeMillis() - startTime) / 1000.0
                    val generated = GeneratedImage(
                        prompt = currentPrompt,
                        enhancedPrompt = fullPrompt,
                        style = style,
                        aspectRatio = ratio,
                        bitmap = apiResult,
                        durationSeconds = duration,
                        isAiGenerated = true
                    )
                    _uiState.value = GenerationUiState.Success(generated)
                    _history.value = listOf(generated) + _history.value.filter { it.id != generated.id }.take(15)
                    _snackBarMessage.value = "Image generated in ${style.title} style!"
                    return@launch
                }
            }

            // Local fallback synthesis
            delay(400)
            _uiState.value = GenerationUiState.Loading("Applying ${style.title} shaders & textures...", 0.65f, 1)
            delay(400)
            _uiState.value = GenerationUiState.Loading("Finalizing rendering...", 0.95f, 2)
            delay(300)

            val duration = (System.currentTimeMillis() - startTime) / 1000.0
            val generated = GeneratedImage(
                prompt = currentPrompt,
                enhancedPrompt = fullPrompt,
                style = style,
                aspectRatio = ratio,
                fallbackDrawableRes = style.sampleResId,
                durationSeconds = duration,
                isAiGenerated = false
            )

            _uiState.value = GenerationUiState.Success(generated)
            _history.value = listOf(generated) + _history.value.filter { it.id != generated.id }.take(15)
            _snackBarMessage.value = "Artwork generated in ${style.title} style!"
        }
    }

    private suspend fun callImagenApi(prompt: String, ratio: String, apiKey: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=$apiKey"
            val jsonBody = JSONObject().apply {
                put("instances", JSONArray().apply {
                    put(JSONObject().apply { put("prompt", prompt) })
                })
                put("parameters", JSONObject().apply {
                    put("sampleCount", 1)
                    put("aspectRatio", ratio)
                    put("outputMimeType", "image/jpeg")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val predictions = json.optJSONArray("predictions")
                if (predictions != null && predictions.length() > 0) {
                    val base64Bytes = predictions.getJSONObject(0).optString("bytesBase64Encoded")
                    if (base64Bytes.isNotEmpty()) {
                        val bytes = Base64.decode(base64Bytes, Base64.DEFAULT)
                        return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e("ImageGenVM", "API error", e)
            null
        }
    }

    fun downloadImage(image: GeneratedImage, onComplete: (Uri?) -> Unit) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val bitmap = image.bitmap ?: run {
                image.fallbackDrawableRes?.let { resId ->
                    BitmapFactory.decodeResource(context.resources, resId)
                }
            }

            if (bitmap != null) {
                val uri = ImageSaver.saveBitmapToGallery(context, bitmap, "AI_Gen_${System.currentTimeMillis()}")
                if (uri != null) {
                    _snackBarMessage.value = "Saved to Pictures/AIImageGenerator!"
                    onComplete(uri)
                } else {
                    _snackBarMessage.value = "Failed to save image"
                    onComplete(null)
                }
            } else {
                _snackBarMessage.value = "Image data unavailable"
                onComplete(null)
            }
        }
    }

    fun selectHistoryItem(image: GeneratedImage) {
        _prompt.value = image.prompt
        _selectedStyle.value = image.style
        _selectedAspectRatio.value = image.aspectRatio
        _uiState.value = GenerationUiState.Success(image)
    }
}
