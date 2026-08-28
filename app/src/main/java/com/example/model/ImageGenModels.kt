package com.example.model

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.AnimeAccent
import com.example.ui.theme.DreamcoreAccent
import com.example.ui.theme.RealisticAccent
import com.example.ui.theme.ThreeDAccent
import java.util.UUID

enum class ImageStyle(
    val title: String,
    val subtitle: String,
    val promptModifier: String,
    @DrawableRes val sampleResId: Int,
    val color: Color
) {
    REALISTIC(
        title = "Realistic",
        subtitle = "Photorealistic 8K, cinematic lighting",
        promptModifier = ", 8k photorealistic photography, sharp focus, hyperdetailed, dynamic studio lighting, octane render aesthetic, 35mm lens shot",
        sampleResId = R.drawable.style_realistic,
        color = RealisticAccent
    ),
    ANIME(
        title = "Anime",
        subtitle = "Vibrant Japanese manga aesthetic",
        promptModifier = ", anime masterpiece aesthetic, Makoto Shinkai style, crisp lineart, radiant vibrant colors, cinematic cel shading, beautiful detailed illustration",
        sampleResId = R.drawable.style_anime,
        color = AnimeAccent
    ),
    THREE_D(
        title = "3D",
        subtitle = "Pixar style, volumetric render",
        promptModifier = ", 3D animated character style, Pixar Disney render, Octane 3D volumetric soft lighting, clay and smooth textures, ultra cute and detailed",
        sampleResId = R.drawable.style_3d,
        color = ThreeDAccent
    ),
    DREAMCORE(
        title = "Dreamcore",
        subtitle = "Ethereal liminal dreamscape",
        promptModifier = ", dreamcore aesthetic, ethereal surrealism, nostalgic liminal space, floating pastel clouds, mystical iridescent glow, vaporwave fantasy",
        sampleResId = R.drawable.style_dreamcore,
        color = DreamcoreAccent
    )
}

enum class AspectRatioOption(
    val label: String,
    val ratioValue: Float,
    val apiRatio: String,
    val iconDescription: String
) {
    SQUARE("1:1", 1.0f, "1:1", "Square"),
    PORTRAIT_9_16("9:16", 9f / 16f, "9:16", "Story"),
    LANDSCAPE_16_9("16:9", 16f / 9f, "16:9", "Banner"),
    PORTRAIT_3_4("3:4", 3f / 4f, "3:4", "Portrait")
}

data class GeneratedImage(
    val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val enhancedPrompt: String,
    val style: ImageStyle,
    val aspectRatio: AspectRatioOption,
    val bitmap: Bitmap? = null,
    @DrawableRes val fallbackDrawableRes: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Double = 0.0,
    val isAiGenerated: Boolean = true
)

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class Loading(
        val stepMessage: String,
        val progress: Float,
        val elapsedSeconds: Int
    ) : GenerationUiState
    data class Success(val image: GeneratedImage) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}
