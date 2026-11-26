@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.huaman.bible_hymns_choruses.presentation.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextDecrease
import androidx.compose.material.icons.rounded.TextIncrease
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huaman.bible_hymns_choruses.core.util.sharePlainText
import com.huaman.bible_hymns_choruses.domain.model.SongStanzaBlock
import com.huaman.bible_hymns_choruses.domain.model.SongType
import com.huaman.bible_hymns_choruses.ui.theme.readerBodyTextStyle

@Composable
fun SongDetailScreen(
    songId: Int,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(songId) {
        viewModel.onIntent(SongDetailIntent.LoadSong(songId))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SongDetailEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                }
                is SongDetailEffect.ShowMessage -> onShowSnackbar(effect.message)
            }
        }
    }

    val song = uiState.song

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (song != null) "${song.type.label.dropLast(1)} #${song.number}" else "Cargando...",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onNavigateBack,
                        shapes = IconButtonDefaults.shapes(
                            shape = IconButtonDefaults.smallRoundShape,
                            pressedShape = IconButtonDefaults.smallPressedShape
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (song != null) {
                        FilledTonalIconButton(
                            onClick = {
                                val next = (uiState.preferences.fontSizeSp - 2f).coerceAtLeast(14f)
                                viewModel.onIntent(SongDetailIntent.UpdateFontSize(next))
                            },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            )
                        ) {
                            Icon(Icons.Rounded.TextDecrease, contentDescription = "Reducir letra")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = {
                                val next = (uiState.preferences.fontSizeSp + 2f).coerceAtMost(28f)
                                viewModel.onIntent(SongDetailIntent.UpdateFontSize(next))
                            },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            )
                        ) {
                            Icon(Icons.Rounded.TextIncrease, contentDescription = "Aumentar letra")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = { viewModel.onIntent(SongDetailIntent.CopyLyrics) },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            )
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copiar letra")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = {
                                val formatted = "${song.type.label.dropLast(1)} #${song.number} - ${song.title}\n\n${song.rawLyrics}"
                                sharePlainText(
                                    context = context,
                                    text = formatted,
                                    chooserTitle = "Compartir canto"
                                )
                            },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            )
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Compartir canto")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = { viewModel.onIntent(SongDetailIntent.ToggleBookmark) },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isBookmarked) {
                                    Icons.Rounded.Bookmark
                                } else {
                                    Icons.Rounded.BookmarkBorder
                                },
                                contentDescription = "Guardar en favoritos"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        }
    ) { innerPadding ->
        if (song == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando canto...", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val badgeShape = if (song.type == SongType.HYMN) {
                MaterialShapes.Clover4Leaf.toShape()
            } else {
                MaterialShapes.Cookie9Sided.toShape()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Song Hero Header
                item(key = "song_header") {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = badgeShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${song.number}",
                                            style = MaterialTheme.typography.titleLargeEmphasized
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.headlineSmallEmphasized
                                    )
                                    if (!song.subtitleEn.isNullOrBlank()) {
                                        Text(
                                            text = "(${song.subtitleEn})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!song.tonality.isNullOrBlank()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MusicNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = song.tonality,
                                                style = MaterialTheme.typography.labelLargeEmphasized
                                            )
                                        }
                                    }
                                }

                                if (!song.category.isNullOrBlank()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) {
                                        Text(
                                            text = song.category,
                                            style = MaterialTheme.typography.labelMediumEmphasized,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Stanzas and Expressive CORO Card (SecondaryContainer, RoundedCornerShape(24.dp))
                itemsIndexed(
                    items = song.blocks,
                    key = { idx, block -> "${idx}_${block.label}" }
                ) { _, block ->
                    SongStanzaView(
                        block = block,
                        fontSizeSp = uiState.preferences.fontSizeSp,
                        fontWeight = uiState.preferences.fontWeight,
                        fontGrade = uiState.preferences.fontGrade
                    )
                }
            }
        }
    }
}

@Composable
private fun SongStanzaView(
    block: SongStanzaBlock,
    fontSizeSp: Float,
    fontWeight: Int,
    fontGrade: Int
) {
    val bodyStyle = readerBodyTextStyle(
        fontSizeSp = fontSizeSp,
        fontWeightValue = fontWeight,
        fontGradeValue = fontGrade
    )

    if (block.isChorus) {
        // CORO framed inside an expressive SecondaryContainer card with RoundedCornerShape(24.dp)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text(
                        text = block.label.ifBlank { "CORO" },
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = block.lines.joinToString("\n"),
                    style = bodyStyle
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (block.label.isNotBlank()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = block.label,
                                style = MaterialTheme.typography.labelLargeEmphasized
                            )
                        }
                    }
                }
                Text(
                    text = block.lines.joinToString("\n"),
                    style = bodyStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
