@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.huaman.bible_hymns_choruses.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huaman.bible_hymns_choruses.core.datastore.ThemeMode
import com.huaman.bible_hymns_choruses.domain.model.BibleBook
import com.huaman.bible_hymns_choruses.domain.model.BibleVerse
import com.huaman.bible_hymns_choruses.domain.model.BibleVersion
import com.huaman.bible_hymns_choruses.domain.model.Testament
import com.huaman.bible_hymns_choruses.ui.theme.readerBodyTextStyle
import kotlin.math.roundToInt

@Composable
fun BibleReaderScreen(
    initialBookId: Int? = null,
    initialChapter: Int? = null,
    highlightVerse: Int? = null,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    viewModel: BibleReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(initialBookId, initialChapter, highlightVerse) {
        if (initialBookId != null && initialChapter != null) {
            viewModel.onIntent(
                BibleReaderIntent.SelectLocation(
                    bookId = initialBookId,
                    chapter = initialChapter,
                    highlightVerse = highlightVerse
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BibleReaderEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                }
                is BibleReaderEffect.ShowMessage -> onShowSnackbar(effect.message)
                is BibleReaderEffect.ScrollToVerse -> {
                    val idx = uiState.verses.indexOfFirst {
                        it.verse <= effect.verseNumber && it.verseEnd >= effect.verseNumber
                    }
                    if (idx >= 0) {
                        listState.animateScrollToItem(idx)
                    }
                }
            }
        }
    }

    val readingProgress by remember(uiState.verses) {
        derivedStateOf {
            val total = uiState.verses.size
            if (total <= 1) 1f
            else {
                val visible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                ((visible + 1).toFloat() / total.toFloat()).coerceIn(0.05f, 1f)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Expressive Header with SplitButton + Version Pill + Typography trigger + WavyProgressIndicator
            BibleReaderTopHeader(
                bookName = uiState.currentBook?.name ?: "Biblia",
                chapter = uiState.currentChapter,
                versionId = uiState.currentVersionId,
                readingProgress = readingProgress,
                onOpenBookPicker = { viewModel.onIntent(BibleReaderIntent.ToggleBookPicker(true)) },
                onOpenVersionPicker = { viewModel.onIntent(BibleReaderIntent.ToggleVersionPicker(true)) },
                onOpenTypography = { viewModel.onIntent(BibleReaderIntent.ToggleTypographySheet(true)) }
            )

            // Verses LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 160.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.verses,
                    key = { it.id },
                    contentType = { "verse" }
                ) { verse ->
                    val isSelected = verse.id in uiState.selectedVerseIds
                    val isBookmarked = verse.id in uiState.bookmarkedVerseIds
                    val isHighlighted = uiState.highlightedVerseNumber?.let { target ->
                        verse.verse <= target && verse.verseEnd >= target
                    } ?: false

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!verse.heading.isNullOrBlank()) {
                            Text(
                                text = verse.heading,
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    end = 4.dp,
                                    top = if (verse.verse == 1) 2.dp else 10.dp,
                                    bottom = 2.dp
                                )
                            )
                        }

                        if (!verse.label.isNullOrBlank()) {
                            Text(
                                text = verse.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    end = 4.dp,
                                    bottom = 2.dp
                                )
                            )
                        }

                        BibleVerseRow(
                            verse = verse,
                            isSelected = isSelected,
                            isBookmarked = isBookmarked,
                            isHighlighted = isHighlighted,
                            fontSizeSp = uiState.preferences.fontSizeSp,
                            fontWeight = uiState.preferences.fontWeight,
                            fontGrade = uiState.preferences.fontGrade,
                            onClick = { viewModel.onIntent(BibleReaderIntent.ToggleVerseSelection(verse.id)) }
                        )
                    }
                }

                // Bottom inline chapter navigation card
                if (uiState.verses.isNotEmpty()) {
                    item(key = "chapter_nav_footer", contentType = "footer") {
                        Spacer(Modifier.height(12.dp))
                        ChapterNavigationButtonGroup(
                            hasPrevious = uiState.hasPreviousChapter,
                            hasNext = uiState.hasNextChapter,
                            onPrevious = { viewModel.onIntent(BibleReaderIntent.PreviousChapter) },
                            onNext = { viewModel.onIntent(BibleReaderIntent.NextChapter) },
                            onOpenTypography = {
                                viewModel.onIntent(BibleReaderIntent.ToggleTypographySheet(true))
                            }
                        )
                    }
                }
            }
        }

        // Contextual Verse Selection Action Bar (Copy / Bookmark)
        AnimatedVisibility(
            visible = uiState.selectedVerseIds.isNotEmpty(),
            enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
                slideInVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { it / 2 },
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {
            VerseSelectionActionBar(
                selectedCount = uiState.selectedVerseIds.size,
                onCopy = { viewModel.onIntent(BibleReaderIntent.CopySelectedVerses) },
                onBookmark = { viewModel.onIntent(BibleReaderIntent.BookmarkSelectedVerses()) },
                onClear = { viewModel.onIntent(BibleReaderIntent.ClearSelection) }
            )
        }
    }

    // Book & Chapter Picker ModalBottomSheet
    if (uiState.isBookPickerVisible) {
        BookChapterPickerSheet(
            books = uiState.books,
            currentBook = uiState.currentBook,
            currentChapter = uiState.currentChapter,
            selectedTestament = uiState.pickerTestament,
            onSelectTestament = { viewModel.onIntent(BibleReaderIntent.SelectPickerTestament(it)) },
            onSelectLocation = { bookId, ch ->
                viewModel.onIntent(BibleReaderIntent.SelectLocation(bookId, ch))
            },
            onDismiss = { viewModel.onIntent(BibleReaderIntent.ToggleBookPicker(false)) }
        )
    }

    // Bible Version Picker ModalBottomSheet (RVR1960, RVC, TLA)
    if (uiState.isVersionPickerVisible) {
        VersionPickerSheet(
            versions = uiState.versions,
            selectedVersionId = uiState.currentVersionId,
            onSelectVersion = { versionId ->
                viewModel.onIntent(BibleReaderIntent.SelectVersion(versionId))
            },
            onDismiss = { viewModel.onIntent(BibleReaderIntent.ToggleVersionPicker(false)) }
        )
    }

    // Personalization Sheet (Font Size & Light/Dark Theme)
    if (uiState.isTypographySheetVisible) {
        PersonalizationSettingsSheet(
            fontSizeSp = uiState.preferences.fontSizeSp,
            themeMode = uiState.preferences.themeMode,
            onUpdateFontSize = { viewModel.onIntent(BibleReaderIntent.UpdateFontSize(it)) },
            onUpdateThemeMode = { viewModel.onIntent(BibleReaderIntent.UpdateThemeMode(it)) },
            onDismiss = { viewModel.onIntent(BibleReaderIntent.ToggleTypographySheet(false)) }
        )
    }
}

@Composable
private fun BibleReaderTopHeader(
    bookName: String,
    chapter: Int,
    versionId: String,
    readingProgress: Float,
    onOpenBookPicker: () -> Unit,
    onOpenVersionPicker: () -> Unit,
    onOpenTypography: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        onClick = onOpenBookPicker
                    ) {
                        Text(
                            text = "$bookName $chapter",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(
                        checked = false,
                        onCheckedChange = { onOpenBookPicker() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = "Elegir libro y capítulo"
                        )
                    }
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onOpenVersionPicker,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    contentPadding = PaddingValues(start = 16.dp, end = 10.dp),
                    modifier = Modifier.height(ButtonDefaults.MinHeight)
                ) {
                    Text(
                        text = versionId,
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "Cambiar versión de la Biblia",
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = onOpenTypography,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallRoundShape,
                        pressedShape = IconButtonDefaults.smallPressedShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Personalización"
                    )
                }
            }
        }

        LinearWavyProgressIndicator(
            progress = { readingProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Composable
private fun BibleVerseRow(
    verse: BibleVerse,
    isSelected: Boolean,
    isBookmarked: Boolean,
    isHighlighted: Boolean,
    fontSizeSp: Float,
    fontWeight: Int,
    fontGrade: Int,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            isHighlighted -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
            isBookmarked -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "VerseContainerColor"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 12.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "VerseCornerMorph"
    )

    val bodyStyle = readerBodyTextStyle(
        fontSizeSp = fontSizeSp,
        fontWeightValue = fontWeight,
        fontGradeValue = fontGrade
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = if (isSelected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = buildAnnotatedString {
                    if (verse.verseLabel.isNotEmpty()) {
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = (fontSizeSp * 0.78f).sp
                            )
                        ) {
                            append("${verse.verseLabel}  ")
                        }
                    }
                    append(verse.text)
                },
                style = bodyStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isBookmarked) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = "Versículo guardado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ChapterNavigationButtonGroup(
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenTypography: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onPrevious,
            enabled = hasPrevious,
            shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = null
            )
            Spacer(Modifier.width(4.dp))
            Text("Anterior", style = MaterialTheme.typography.labelLargeEmphasized)
        }

        FilledTonalIconButton(
            onClick = onOpenTypography,
            shapes = IconButtonDefaults.shapes(
                shape = IconButtonDefaults.smallRoundShape,
                pressedShape = IconButtonDefaults.smallPressedShape
            )
        ) {
            Icon(Icons.Rounded.Tune, contentDescription = "Personalización")
        }

        Button(
            onClick = onNext,
            enabled = hasNext,
            shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
            modifier = Modifier.weight(1f)
        ) {
            Text("Siguiente", style = MaterialTheme.typography.labelLargeEmphasized)
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun VerseSelectionActionBar(
    selectedCount: Int,
    onCopy: () -> Unit,
    onBookmark: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$selectedCount sel.",
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            FilledTonalButton(
                onClick = onCopy,
                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copiar")
            }
            Button(
                onClick = onBookmark,
                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
            ) {
                Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Guardar")
            }
            FilledTonalIconButton(onClick = onClear) {
                Icon(Icons.Rounded.Close, contentDescription = "Cancelar selección")
            }
        }
    }
}

@Composable
private fun BookChapterPickerSheet(
    books: List<BibleBook>,
    currentBook: BibleBook?,
    currentChapter: Int,
    selectedTestament: Testament,
    onSelectTestament: (Testament) -> Unit,
    onSelectLocation: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pickedBook by remember(currentBook) { mutableStateOf(currentBook) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (pickedBook == null) "Seleccionar Libro" else "${pickedBook?.name} · Elige Capítulo",
                style = MaterialTheme.typography.headlineSmallEmphasized
            )

            // Testament ButtonGroup (AT / NT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Testament.entries.forEach { testament ->
                    val checked = selectedTestament == testament
                    ToggleButton(
                        checked = checked,
                        onCheckedChange = {
                            onSelectTestament(testament)
                            pickedBook = null
                        },
                        shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (testament == Testament.AT) "Antiguo Testamento" else "Nuevo Testamento",
                            style = if (checked) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (pickedBook != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pickedBook?.name} (${pickedBook?.chapterCount} capítulos)",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FilledTonalButton(
                        onClick = { pickedBook = null },
                        shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
                    ) {
                        Text("Cambiar libro")
                    }
                }

                val chapters = (1..(pickedBook?.chapterCount ?: 1)).toList()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 56.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chapters, key = { it }) { ch ->
                        val isCurrent = pickedBook?.id == currentBook?.id && ch == currentChapter
                        Surface(
                            onClick = { onSelectLocation(pickedBook!!.id, ch) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (isCurrent) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.height(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ch.toString(),
                                    style = MaterialTheme.typography.titleMediumEmphasized
                                )
                            }
                        }
                    }
                }
            } else {
                val filteredBooks = books.filter { it.testament == selectedTestament }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBooks, key = { it.id }) { book ->
                        val isCurrent = book.id == currentBook?.id
                        Surface(
                            onClick = {
                                if (book.chapterCount == 1) {
                                    onSelectLocation(book.id, 1)
                                } else {
                                    pickedBook = book
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (isCurrent) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = book.name,
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${book.chapterCount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalizationSettingsSheet(
    fontSizeSp: Float,
    themeMode: ThemeMode,
    onUpdateFontSize: (Float) -> Unit,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Personalización",
                style = MaterialTheme.typography.headlineSmallEmphasized
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Lámpara es a mis pies tu palabra, y lumbrera a mi camino.",
                    style = readerBodyTextStyle(fontSizeSp = fontSizeSp),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tamaño de letra: ${fontSizeSp.roundToInt()} sp",
                    style = MaterialTheme.typography.titleSmallEmphasized
                )
                Slider(
                    value = fontSizeSp,
                    onValueChange = onUpdateFontSize,
                    valueRange = 14f..28f
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tema",
                    style = MaterialTheme.typography.titleSmallEmphasized
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        Triple(ThemeMode.SYSTEM, "Auto", Icons.Rounded.BrightnessAuto),
                        Triple(ThemeMode.LIGHT, "Claro", Icons.Rounded.LightMode),
                        Triple(ThemeMode.DARK, "Oscuro", Icons.Rounded.DarkMode)
                    )
                    themeOptions.forEach { (mode, label, icon) ->
                        val checked = themeMode == mode
                        ToggleButton(
                            checked = checked,
                            onCheckedChange = { onUpdateThemeMode(mode) },
                            shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = if (checked) {
                                    MaterialTheme.typography.labelLargeEmphasized
                                } else {
                                    MaterialTheme.typography.labelLarge
                                },
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionPickerSheet(
    versions: List<BibleVersion>,
    selectedVersionId: String,
    onSelectVersion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val displayVersions = remember(versions) {
        versions.ifEmpty {
            listOf(
                BibleVersion("RVR1960", "Reina Valera 1960", "Biblia Reina Valera 1960", "", 1),
                BibleVersion("RVC", "Reina Valera Contemporánea", "Reina Valera Contemporánea", "", 2),
                BibleVersion("TLA", "Traducción en Lenguaje Actual", "Traducción en Lenguaje Actual", "", 3)
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Versión de la Biblia",
                style = MaterialTheme.typography.headlineSmallEmphasized
            )

            displayVersions.forEach { version ->
                val isSelected = version.id == selectedVersionId
                Surface(
                    onClick = { onSelectVersion(version.id) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    border = if (isSelected) {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ) {
                            Text(
                                text = version.id,
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = version.name,
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = "Disponible sin conexión",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Versión seleccionada",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
