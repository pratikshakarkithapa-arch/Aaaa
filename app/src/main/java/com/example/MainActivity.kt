package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GenerationUiState
import com.example.ui.components.ApiKeyInfoDialog
import com.example.ui.components.AspectRatioSelector
import com.example.ui.components.FullscreenImageDialog
import com.example.ui.components.GenerationLoadingAnimation
import com.example.ui.components.HistoryGallery
import com.example.ui.components.ImagePreviewCard
import com.example.ui.components.PromptInputBox
import com.example.ui.components.StyleSelector
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ImageGeneratorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ImageGeneratorApp()
            }
        }
    }
}

@Composable
fun ImageGeneratorApp(
    viewModel: ImageGeneratorViewModel = viewModel()
) {
    val context = LocalContext.current
    val prompt by viewModel.prompt.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedRatio by viewModel.selectedAspectRatio.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val activeFullscreenImage by viewModel.activeFullscreenImage.collectAsState()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsState()
    val snackBarMessage by viewModel.snackBarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackBarMessage) {
        snackBarMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSnackBar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg),
        containerColor = StudioDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            HeaderBar(
                isApiKeyConfigured = viewModel.isApiKeyConfigured,
                onInfoClick = { viewModel.openApiKeyDialog() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Box
            PromptInputBox(
                prompt = prompt,
                onPromptChange = viewModel::onPromptChange,
                onInspireClick = viewModel::inspireMe,
                onClearClick = viewModel::clearPrompt,
                onSubmit = viewModel::generate
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Style Selector (Realistic, Anime, 3D, Dreamcore)
            StyleSelector(
                selectedStyle = selectedStyle,
                onStyleSelected = viewModel::onStyleSelect
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Aspect Ratio Selector (1:1, 9:16, 16:9, 3:4)
            AspectRatioSelector(
                selectedRatio = selectedRatio,
                onRatioSelected = viewModel::onAspectRatioSelect
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Big Generate Button
            val isGenerating = uiState is GenerationUiState.Loading
            GenerateButton(
                isGenerating = isGenerating,
                onClick = viewModel::generate
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Section / State Display
            when (val state = uiState) {
                is GenerationUiState.Loading -> {
                    GenerationLoadingAnimation(
                        stepMessage = state.stepMessage,
                        progress = state.progress,
                        elapsedSeconds = state.elapsedSeconds,
                        style = selectedStyle
                    )
                }

                is GenerationUiState.Success -> {
                    ImagePreviewCard(
                        image = state.image,
                        onDownloadClick = viewModel::saveToGallery,
                        onShareClick = viewModel::shareImage,
                        onFullscreenClick = viewModel::openFullscreen
                    )
                }

                is GenerationUiState.Error -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = NeonPink,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Generation Notice",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = viewModel::generate,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry Generation")
                            }
                        }
                    }
                }

                GenerationUiState.Idle -> {
                    ImagePreviewCard(
                        image = null,
                        onDownloadClick = {},
                        onShareClick = {},
                        onFullscreenClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // History Gallery
            HistoryGallery(
                history = history,
                onSelectImage = viewModel::selectHistoryItem
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Fullscreen Zoom Dialog
    activeFullscreenImage?.let { image ->
        FullscreenImageDialog(
            image = image,
            onDismiss = viewModel::closeFullscreen,
            onDownload = viewModel::saveToGallery,
            onShare = viewModel::shareImage,
            onPromptCopied = {
                Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // API Key / Engine Info Dialog
    if (showApiKeyDialog) {
        ApiKeyInfoDialog(
            isConfigured = viewModel.isApiKeyConfigured,
            onDismiss = viewModel::closeApiKeyDialog
        )
    }
}

@Composable
private fun HeaderBar(
    isApiKeyConfigured: Boolean,
    onInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonViolet, NeonPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "IMAGINE AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Fast AI Image Studio",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Engine / API status pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onInfoClick)
                .testTag("api_status_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isApiKeyConfigured) NeonEmerald else NeonCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isApiKeyConfigured) "Gemini Live" else "Studio Mode",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isApiKeyConfigured) NeonEmerald else NeonCyan
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "API Info",
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun GenerateButton(
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    val gradientBrush = Brush.horizontalGradient(
        listOf(
            NeonViolet,
            NeonPurple,
            NeonPink
        )
    )

    Button(
        onClick = onClick,
        enabled = !isGenerating,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = StudioCardElevated
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (!isGenerating) 12.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = NeonViolet.copy(alpha = 0.5f),
                spotColor = NeonPink
            )
            .clip(RoundedCornerShape(18.dp))
            .background(if (!isGenerating) gradientBrush else Brush.linearGradient(listOf(StudioCardElevated, StudioCardElevated)))
            .border(
                1.dp,
                if (!isGenerating) NeonViolet.copy(alpha = 0.7f) else StudioBorder,
                RoundedCornerShape(18.dp)
            )
            .testTag("generate_button")
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = NeonCyan,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Dreaming Pixels...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        } else {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Generate Artwork",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }
    }
}
