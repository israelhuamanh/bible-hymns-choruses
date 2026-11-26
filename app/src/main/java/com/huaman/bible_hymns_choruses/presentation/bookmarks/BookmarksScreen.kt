@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.huaman.bible_hymns_choruses.presentation.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huaman.bible_hymns_choruses.core.util.formatShortSpanishDate
import com.huaman.bible_hymns_choruses.core.util.sharePlainText
import com.huaman.bible_hymns_choruses.core.util.spanishMonthAbbrev
import com.huaman.bible_hymns_choruses.domain.model.BookmarkItem
import com.huaman.bible_hymns_choruses.domain.model.BookmarkType

@Composable
fun BookmarksScreen(
    onOpenBookmarkDetail: (bookmarkId: Int, startInEditMode: Boolean) -> Unit,
    onOpenSongDetail: (Int) -> Unit,
    onOpenBiblePassage: (bookId: Int, chapter: Int, verse: Int) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onShowUndoSnackbar: (message: String, onUndo: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BookmarksEffect.ShowMessage -> onShowSnackbar(effect.message)
                is BookmarksEffect.ShowUndoDelete -> {
                    onShowUndoSnackbar(effect.message) {
                        viewModel.onIntent(BookmarksIntent.UndoDeleteBookmark)
                    }
                }
                is BookmarksEffect.NavigateToBiblePassage -> {
                    onOpenBiblePassage(effect.bookId, effect.chapter, effect.verse)
                }
                is BookmarksEffect.NoteUpdated -> Unit
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Guardados",
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Category Filter Row (Todos / Versículos / Himnos / Coros)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BookmarkFilter.entries, key = { it.name }) { filter ->
                    val checked = uiState.selectedFilter == filter
                    ToggleButton(
                        checked = checked,
                        onCheckedChange = { viewModel.onIntent(BookmarksIntent.SelectFilter(filter)) },
                        shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight)
                    ) {
                        Text(
                            text = filter.label,
                            style = if (checked) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Date Sort Order + Year & Month Filter Row
            if (uiState.allBookmarks.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item(key = "sort_order_chip") {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onIntent(BookmarksIntent.ToggleSortOrder) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.SwapVert,
                                    contentDescription = "Ordenar por fecha",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = uiState.sortOrder.label,
                                    style = MaterialTheme.typography.labelLargeEmphasized
                                )
                            },
                            shape = CircleShape
                        )
                    }

                    if (uiState.availableYears.isNotEmpty()) {
                        item(key = "year_all") {
                            val selected = uiState.selectedYear == null
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onIntent(BookmarksIntent.SelectYear(null)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Todos los años",
                                        style = if (selected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                                    )
                                },
                                shape = CircleShape
                            )
                        }

                        items(uiState.availableYears, key = { "year_$it" }) { year ->
                            val selected = uiState.selectedYear == year
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    viewModel.onIntent(
                                        BookmarksIntent.SelectYear(if (selected) null else year)
                                    )
                                },
                                label = {
                                    Text(
                                        text = year.toString(),
                                        style = if (selected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                                    )
                                },
                                shape = CircleShape
                            )
                        }
                    }

                    if (uiState.availableMonths.size > 1 || uiState.selectedMonth != null) {
                        items(uiState.availableMonths, key = { "month_$it" }) { month ->
                            val selected = uiState.selectedMonth == month
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    viewModel.onIntent(
                                        BookmarksIntent.SelectMonth(if (selected) null else month)
                                    )
                                },
                                label = {
                                    Text(
                                        text = spanishMonthAbbrev(month),
                                        style = if (selected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                                    )
                                },
                                shape = CircleShape
                            )
                        }
                    }
                }
            }
        }

        if (uiState.filteredBookmarks.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No hay elementos guardados en este filtro",
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    Text(
                        text = "Toca cualquier versículo, himno o coro para guardarlo aquí sin conexión.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.filteredBookmarks,
                    key = { it.id }
                ) { item ->
                    BookmarkCard(
                        item = item,
                        onClick = { onOpenBookmarkDetail(item.id, false) },
                        onEdit = { onOpenBookmarkDetail(item.id, true) },
                        onShare = {
                            sharePlainText(
                                context = context,
                                text = buildBookmarkShareText(item),
                                chooserTitle = "Compartir guardado"
                            )
                        },
                        onDelete = { viewModel.onIntent(BookmarksIntent.DeleteBookmark(item.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    item: BookmarkItem,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    // From M3 (references/tokens.md & SKILL.md):
    // Outer card radius = 20.dp, padding = 16.dp -> inner note box uses 12.dp corner step.
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category capsule + Date capsule ("25dic 2025")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = item.itemType.label,
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = formatShortSpanishDate(item.createdAt),
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Edit, Share, and Delete action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar nota personal",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Compartir guardado",
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Eliminar guardado",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = item.titlePreview,
                style = MaterialTheme.typography.titleMediumEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.contentPreview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!item.note.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Nota personal",
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = item.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated full-screen window for viewing and editing a Saved Item (`BookmarkItem`) and its
 * potentially long personal note (`AppRoute.BookmarkDetailRoute`).
 */
@Composable
fun BookmarkDetailScreen(
    bookmarkId: Int,
    startInEditMode: Boolean,
    onNavigateBack: () -> Unit,
    onOpenSongDetail: (Int) -> Unit,
    onOpenBiblePassage: (bookId: Int, chapter: Int, verse: Int) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onShowUndoSnackbar: (message: String, onUndo: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val item = remember(uiState.allBookmarks, bookmarkId) {
        uiState.allBookmarks.firstOrNull { it.id == bookmarkId }
    }

    var isEditingNote by rememberSaveable(bookmarkId, startInEditMode) {
        mutableStateOf(startInEditMode)
    }
    var draftNote by rememberSaveable(bookmarkId, item?.note) {
        mutableStateOf(item?.note.orEmpty())
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BookmarksEffect.ShowMessage -> onShowSnackbar(effect.message)
                is BookmarksEffect.ShowUndoDelete -> {
                    onShowUndoSnackbar(effect.message) {
                        viewModel.onIntent(BookmarksIntent.UndoDeleteBookmark)
                    }
                }
                is BookmarksEffect.NavigateToBiblePassage -> {
                    onOpenBiblePassage(effect.bookId, effect.chapter, effect.verse)
                }
                is BookmarksEffect.NoteUpdated -> {
                    isEditingNote = false
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = item?.titlePreview ?: "Detalle de guardado",
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
                    if (item != null) {
                        FilledTonalIconButton(
                            onClick = { isEditingNote = !isEditingNote },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Editar nota personal"
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = {
                                sharePlainText(
                                    context = context,
                                    text = buildBookmarkShareText(item),
                                    chooserTitle = "Compartir guardado"
                                )
                            },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Compartir guardado"
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = {
                                viewModel.onIntent(BookmarksIntent.DeleteBookmark(item.id))
                                onNavigateBack()
                            },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                                pressedShape = IconButtonDefaults.smallPressedShape
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Eliminar guardado"
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
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Guardado no encontrado",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card with Type Capsule, Date Capsule ("25dic 2025"), Full Content & Jump Action
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(
                                    text = item.itemType.label,
                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(
                                    text = formatShortSpanishDate(item.createdAt),
                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Text(
                            text = item.titlePreview,
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = item.contentPreview,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Context jump button: "Ir al capítulo completo" for Verses or "Abrir canto completo" for Songs
                        if (item.itemType == BookmarkType.VERSE) {
                            FilledTonalButton(
                                onClick = {
                                    viewModel.onIntent(BookmarksIntent.OpenVerseInBible(item.referenceId))
                                },
                                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Ir al capítulo completo",
                                    style = MaterialTheme.typography.labelLargeEmphasized
                                )
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { onOpenSongDetail(item.referenceId) },
                                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LibraryMusic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Abrir canto completo",
                                    style = MaterialTheme.typography.labelLargeEmphasized
                                )
                            }
                        }
                    }
                }

                // Personal Note Section (Read or Edit full-length personal notes)
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nota personal",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            if (!isEditingNote) {
                                FilledTonalButton(
                                    onClick = {
                                        draftNote = item.note.orEmpty()
                                        isEditingNote = true
                                    },
                                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                                    contentPadding = PaddingValues(horizontal = 14.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (item.note.isNullOrBlank()) "Agregar nota" else "Editar nota",
                                        style = MaterialTheme.typography.labelLargeEmphasized
                                    )
                                }
                            }
                        }

                        if (isEditingNote) {
                            OutlinedTextField(
                                value = draftNote,
                                onValueChange = { draftNote = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                label = { Text("Tu reflexión o nota personal") },
                                placeholder = { Text("Escribe tu nota completa sobre este guardado...") },
                                minLines = 5,
                                maxLines = 18,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        draftNote = item.note.orEmpty()
                                        isEditingNote = false
                                    },
                                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text(
                                        text = "Cancelar",
                                        style = MaterialTheme.typography.labelLargeEmphasized
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.onIntent(
                                            BookmarksIntent.UpdateBookmarkNote(
                                                id = item.id,
                                                note = draftNote
                                            )
                                        )
                                    },
                                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Guardar nota",
                                        style = MaterialTheme.typography.labelLargeEmphasized
                                    )
                                }
                            }
                        } else {
                            if (item.note.isNullOrBlank()) {
                                Text(
                                    text = "Aún no has escrito una nota personal para este elemento. Pulsa \"Agregar nota\" para añadir una reflexión.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            } else {
                                Text(
                                    text = item.note,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildBookmarkShareText(item: BookmarkItem): String {
    return buildString {
        append(item.titlePreview)
        append("\n")
        append(item.contentPreview)
        if (!item.note.isNullOrBlank()) {
            append("\n\nNota personal:\n")
            append(item.note)
        }
    }
}
