@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.huaman.bible_hymns_choruses.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huaman.bible_hymns_choruses.data.repository.SmartQueryRouter
import com.huaman.bible_hymns_choruses.domain.model.DirectShortcut
import com.huaman.bible_hymns_choruses.domain.model.SongSummary
import com.huaman.bible_hymns_choruses.domain.model.SongType
import com.huaman.bible_hymns_choruses.domain.model.VerseSearchMatch

@Composable
fun GlobalSearchScreen(
    initialQuery: String = "",
    onNavigateBack: () -> Unit,
    onOpenBiblePassage: (bookId: Int, chapter: Int, highlightVerse: Int?) -> Unit,
    onOpenSongDetail: (songId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank() && uiState.query.isBlank()) {
            viewModel.onIntent(SearchIntent.UpdateQuery(initialQuery))
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Top Search Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onNavigateBack,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallRoundShape,
                        pressedShape = IconButtonDefaults.smallPressedShape
                    )
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }

                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onIntent(SearchIntent.UpdateQuery(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text("Cita (Juan 3:16), #104 o palabra clave...")
                    },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onIntent(SearchIntent.ClearQuery) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }

            if (uiState.isSearching) {
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

            // Tab Selector (Todo, Biblia RVR1960, Himnos, Coros)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchTab.entries, key = { it.name }) { tab ->
                    val checked = uiState.selectedTab == tab
                    val count = when (tab) {
                        SearchTab.ALL -> uiState.result.bibleMatches.size + uiState.result.hymnMatches.size + uiState.result.chorusMatches.size
                        SearchTab.BIBLE -> uiState.result.bibleMatches.size
                        SearchTab.HYMNS -> uiState.result.hymnMatches.size
                        SearchTab.CHORUSES -> uiState.result.chorusMatches.size
                    }
                    ToggleButton(
                        checked = checked,
                        onCheckedChange = { viewModel.onIntent(SearchIntent.SelectTab(tab)) },
                        shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight)
                    ) {
                        Text(
                            text = if (uiState.query.isNotBlank() && count > 0) "${tab.label} ($count)" else tab.label,
                            style = if (checked) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick suggestion chips when query is empty
                if (uiState.query.isBlank()) {
                    item(key = "suggestions_header") {
                        Text(
                            text = "Búsquedas sugeridas",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item(key = "suggestions_chips") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.recentQueries, key = { it }) { suggestion ->
                                SuggestionChip(
                                    onClick = { viewModel.onIntent(SearchIntent.UpdateQuery(suggestion)) },
                                    label = { Text(suggestion) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }

                // 1. Direct Shortcut Cards FIRST (Bible Reference or Song #)
                if (uiState.result.shortcuts.isNotEmpty()) {
                    item(key = "shortcuts_header") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Acceso Directo",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(
                        items = uiState.result.shortcuts,
                        key = { sc ->
                            when (sc) {
                                is DirectShortcut.BiblePassage -> "sc_bible_${sc.bookId}_${sc.chapter}_${sc.startVerse}"
                                is DirectShortcut.SongMatch -> "sc_song_${sc.song.id}"
                            }
                        }
                    ) { shortcut ->
                        DirectShortcutCard(
                            shortcut = shortcut,
                            onOpenBiblePassage = onOpenBiblePassage,
                            onOpenSongDetail = onOpenSongDetail
                        )
                    }
                }

                // 2. Tabbed FTS4 Results with Highlighted Terms
                val showBible = uiState.selectedTab == SearchTab.ALL || uiState.selectedTab == SearchTab.BIBLE
                val showHymns = uiState.selectedTab == SearchTab.ALL || uiState.selectedTab == SearchTab.HYMNS
                val showChoruses = uiState.selectedTab == SearchTab.ALL || uiState.selectedTab == SearchTab.CHORUSES

                if (showBible && uiState.result.bibleMatches.isNotEmpty()) {
                    item(key = "header_bible") {
                        Text(
                            text = "Biblia ${uiState.activeVersionId} (${uiState.result.bibleMatches.size})",
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    }
                    items(
                        items = uiState.result.bibleMatches,
                        key = { "v_${it.id}" }
                    ) { match ->
                        BibleSearchResultCard(
                            match = match,
                            query = uiState.query,
                            onClick = { onOpenBiblePassage(match.bookId, match.chapter, match.verse) }
                        )
                    }
                }

                if (showHymns && uiState.result.hymnMatches.isNotEmpty()) {
                    item(key = "header_hymns") {
                        Text(
                            text = "Himnos (${uiState.result.hymnMatches.size})",
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    }
                    items(
                        items = uiState.result.hymnMatches,
                        key = { "h_${it.id}" }
                    ) { song ->
                        SongSearchResultCard(
                            song = song,
                            query = uiState.query,
                            onClick = { onOpenSongDetail(song.id) }
                        )
                    }
                }

                if (showChoruses && uiState.result.chorusMatches.isNotEmpty()) {
                    item(key = "header_choruses") {
                        Text(
                            text = "Coros (${uiState.result.chorusMatches.size})",
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    }
                    items(
                        items = uiState.result.chorusMatches,
                        key = { "c_${it.id}" }
                    ) { song ->
                        SongSearchResultCard(
                            song = song,
                            query = uiState.query,
                            onClick = { onOpenSongDetail(song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectShortcutCard(
    shortcut: DirectShortcut,
    onOpenBiblePassage: (Int, Int, Int?) -> Unit,
    onOpenSongDetail: (Int) -> Unit
) {
    when (shortcut) {
        is DirectShortcut.BiblePassage -> {
            Card(
                onClick = { onOpenBiblePassage(shortcut.bookId, shortcut.chapter, shortcut.startVerse) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null)
                    Column(modifier = Modifier.weight(1f)) {
                        val refLabel = if (shortcut.startVerse != null) {
                            "${shortcut.bookName} ${shortcut.chapter}:${shortcut.startVerse}"
                        } else {
                            "${shortcut.bookName} ${shortcut.chapter}"
                        }
                        Text(text = refLabel, style = MaterialTheme.typography.titleMediumEmphasized)
                        if (shortcut.previewText.isNotBlank()) {
                            Text(
                                text = shortcut.previewText,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        is DirectShortcut.SongMatch -> {
            val song = shortcut.song
            Card(
                onClick = { onOpenSongDetail(song.id) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (song.type == SongType.HYMN) {
                            Icons.Rounded.LibraryMusic
                        } else {
                            Icons.Rounded.Lyrics
                        },
                        contentDescription = null
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${song.type.label.dropLast(1)} #${song.number} · ${song.title}",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!song.tonality.isNullOrBlank()) {
                            Text(
                                text = song.tonality,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BibleSearchResultCard(
    match: VerseSearchMatch,
    query: String,
    onClick: () -> Unit
) {
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val highlightTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                val verseDisplay = match.verseLabel.ifEmpty { match.verse.toString() }
                Text(
                    text = "${match.bookName} ${match.chapter}:$verseDisplay",
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = highlightTerms(match.text, query, highlightColor, highlightTextColor),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SongSearchResultCard(
    song: SongSummary,
    query: String,
    onClick: () -> Unit
) {
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val highlightTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Text(
                    text = "#${song.number}",
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightTerms(song.title, query, highlightColor, highlightTextColor),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!song.tonality.isNullOrBlank()) {
                    Text(
                        text = song.tonality,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun highlightTerms(
    text: String,
    rawQuery: String,
    bgColor: androidx.compose.ui.graphics.Color,
    fgColor: androidx.compose.ui.graphics.Color
): AnnotatedString {
    val tokens = SmartQueryRouter.normalizeText(rawQuery)
        .split(" ")
        .filter { it.length >= 2 }
    if (tokens.isEmpty()) return AnnotatedString(text)

    val normText = SmartQueryRouter.normalizeText(text)
    return buildAnnotatedString {
        append(text)
        tokens.forEach { token ->
            var startIdx = 0
            while (startIdx < normText.length) {
                val found = normText.indexOf(token, startIdx)
                if (found < 0) break
                val end = (found + token.length).coerceAtMost(text.length)
                if (found < end) {
                    addStyle(
                        SpanStyle(
                            background = bgColor,
                            color = fgColor,
                            fontWeight = FontWeight.Bold
                        ),
                        start = found,
                        end = end
                    )
                }
                startIdx = found + token.length
            }
        }
    }
}
