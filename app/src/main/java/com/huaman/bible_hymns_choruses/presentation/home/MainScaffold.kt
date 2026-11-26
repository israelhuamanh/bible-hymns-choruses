@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.huaman.bible_hymns_choruses.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.huaman.bible_hymns_choruses.core.datastore.ActiveAppTab
import com.huaman.bible_hymns_choruses.core.datastore.ThemeMode
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferences
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferencesRepository
import com.huaman.bible_hymns_choruses.core.navigation.AppRoute
import com.huaman.bible_hymns_choruses.presentation.bookmarks.BookmarkDetailScreen
import com.huaman.bible_hymns_choruses.presentation.bookmarks.BookmarksScreen
import com.huaman.bible_hymns_choruses.presentation.bookmarks.BookmarksViewModel
import com.huaman.bible_hymns_choruses.presentation.notes.NoteEditorScreen
import com.huaman.bible_hymns_choruses.presentation.notes.NotesScreen
import com.huaman.bible_hymns_choruses.presentation.notes.NotesViewModel
import com.huaman.bible_hymns_choruses.presentation.onboarding.FirstLaunchOnboardingSheet
import com.huaman.bible_hymns_choruses.presentation.reader.BibleReaderIntent
import com.huaman.bible_hymns_choruses.presentation.reader.BibleReaderScreen
import com.huaman.bible_hymns_choruses.presentation.reader.BibleReaderViewModel
import com.huaman.bible_hymns_choruses.presentation.search.GlobalSearchScreen
import com.huaman.bible_hymns_choruses.presentation.songs.SongDetailScreen
import com.huaman.bible_hymns_choruses.presentation.songs.SongsScreen
import com.huaman.bible_hymns_choruses.presentation.songs.SongsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainAppViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences(hasCompletedOnboarding = true)
        )

    val showOnboarding: StateFlow<Boolean> = userPreferencesRepository.userPreferencesFlow
        .map { !it.hasCompletedOnboarding }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.userPreferencesFlow
        .map { it.themeMode }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    fun setActiveTab(tab: ActiveAppTab) {
        viewModelScope.launch {
            userPreferencesRepository.updateLastActiveTab(tab)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding()
        }
    }
}

private enum class MainToolbarTab(
    val tab: ActiveAppTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    BIBLE(
        tab = ActiveAppTab.BIBLE,
        label = "Biblia",
        selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook
    ),
    SONGS(
        tab = ActiveAppTab.SONGS,
        label = "Cantos",
        selectedIcon = Icons.Rounded.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    ),
    BOOKMARKS(
        tab = ActiveAppTab.BOOKMARKS,
        label = "Guardados",
        selectedIcon = Icons.Rounded.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    ),
    NOTES(
        tab = ActiveAppTab.NOTES,
        label = "Apuntes",
        selectedIcon = Icons.Rounded.EditNote,
        unselectedIcon = Icons.Outlined.EditNote
    )
}

@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    appViewModel: MainAppViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val showOnboarding by appViewModel.showOnboarding.collectAsStateWithLifecycle()

    val bibleViewModel: BibleReaderViewModel = hiltViewModel()
    val songsViewModel: SongsViewModel = hiltViewModel()
    val bookmarksViewModel: BookmarksViewModel = hiltViewModel()
    val notesViewModel: NotesViewModel = hiltViewModel()
    val bibleListState = rememberLazyListState()
    val songsListState = rememberLazyListState()
    val bookmarksListState = rememberLazyListState()
    val notesListState = rememberLazyListState()

    var activeTab by rememberSaveable { mutableStateOf(ActiveAppTab.BIBLE) }
    var visitedTabs by remember { mutableStateOf(setOf(ActiveAppTab.BIBLE)) }

    LaunchedEffect(activeTab) {
        if (activeTab !in visitedTabs) {
            visitedTabs = visitedTabs + activeTab
        }
        appViewModel.setActiveTab(activeTab)
    }

    val showSnackbar: (String) -> Unit = remember(snackbarHostState) {
        { msg ->
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(msg)
            }
        }
    }

    val showUndoSnackbar: (String, () -> Unit) -> Unit = remember(snackbarHostState) {
        { msg, onUndo ->
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "Deshacer",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onUndo()
                }
            }
        }
    }

    val isMainTabRoute = currentDestination?.hasRoute<AppRoute.BibleReaderRoute>() ?: true

    if (showOnboarding) {
        FirstLaunchOnboardingSheet(
            onComplete = { appViewModel.completeOnboarding() }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = if (isMainTabRoute) 88.dp else 16.dp)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoute.BibleReaderRoute(),
                enterTransition = { fadeIn(animationSpec = tween(140)) },
                exitTransition = { fadeOut(animationSpec = tween(100)) },
                popEnterTransition = { fadeIn(animationSpec = tween(140)) },
                popExitTransition = { fadeOut(animationSpec = tween(100)) }
            ) {
                composable<AppRoute.BibleReaderRoute> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Keep each visited tab mounted and switch via GPU alpha + zIndex
                        // so tab switches do zero screen recomposition during the navbar spring.
                        if (ActiveAppTab.BIBLE in visitedTabs) {
                            KeepAliveTabContainer(isActive = activeTab == ActiveAppTab.BIBLE) {
                                BibleReaderScreen(
                                    onShowSnackbar = showSnackbar,
                                    listState = bibleListState,
                                    viewModel = bibleViewModel
                                )
                            }
                        }

                        if (ActiveAppTab.SONGS in visitedTabs) {
                            KeepAliveTabContainer(isActive = activeTab == ActiveAppTab.SONGS) {
                                SongsScreen(
                                    onNavigateToDetail = { songId ->
                                        navController.navigate(AppRoute.SongDetailRoute(songId))
                                    },
                                    onShowSnackbar = showSnackbar,
                                    listState = songsListState,
                                    viewModel = songsViewModel
                                )
                            }
                        }

                        if (ActiveAppTab.BOOKMARKS in visitedTabs) {
                            KeepAliveTabContainer(isActive = activeTab == ActiveAppTab.BOOKMARKS) {
                                BookmarksScreen(
                                    onOpenBookmarkDetail = { bookmarkId, startInEditMode ->
                                        navController.navigate(
                                            AppRoute.BookmarkDetailRoute(
                                                bookmarkId = bookmarkId,
                                                startInEditMode = startInEditMode
                                            )
                                        )
                                    },
                                    onOpenSongDetail = { songId ->
                                        navController.navigate(AppRoute.SongDetailRoute(songId))
                                    },
                                    onOpenBiblePassage = { bookId, chapter, verse ->
                                        bibleViewModel.onIntent(
                                            BibleReaderIntent.SelectLocation(
                                                bookId = bookId,
                                                chapter = chapter,
                                                highlightVerse = verse
                                            )
                                        )
                                        activeTab = ActiveAppTab.BIBLE
                                    },
                                    onShowSnackbar = showSnackbar,
                                    onShowUndoSnackbar = showUndoSnackbar,
                                    listState = bookmarksListState,
                                    viewModel = bookmarksViewModel
                                )
                            }
                        }

                        if (ActiveAppTab.NOTES in visitedTabs) {
                            KeepAliveTabContainer(isActive = activeTab == ActiveAppTab.NOTES) {
                                NotesScreen(
                                    onOpenNoteEditor = { noteId ->
                                        navController.navigate(AppRoute.NoteEditorRoute(noteId))
                                    },
                                    onShowSnackbar = showSnackbar,
                                    onShowUndoSnackbar = showUndoSnackbar,
                                    listState = notesListState,
                                    viewModel = notesViewModel
                                )
                            }
                        }
                    }
                }

                composable<AppRoute.BookmarkDetailRoute> { entry ->
                    val route = entry.toRoute<AppRoute.BookmarkDetailRoute>()
                    BookmarkDetailScreen(
                        bookmarkId = route.bookmarkId,
                        startInEditMode = route.startInEditMode,
                        onNavigateBack = { navController.popBackStack() },
                        onOpenSongDetail = { songId ->
                            navController.navigate(AppRoute.SongDetailRoute(songId))
                        },
                        onOpenBiblePassage = { bookId, chapter, verse ->
                            bibleViewModel.onIntent(
                                BibleReaderIntent.SelectLocation(
                                    bookId = bookId,
                                    chapter = chapter,
                                    highlightVerse = verse
                                )
                            )
                            activeTab = ActiveAppTab.BIBLE
                            navController.popBackStack()
                        },
                        onShowSnackbar = showSnackbar,
                        onShowUndoSnackbar = showUndoSnackbar,
                        viewModel = bookmarksViewModel
                    )
                }

                composable<AppRoute.NoteEditorRoute> { entry ->
                    val route = entry.toRoute<AppRoute.NoteEditorRoute>()
                    NoteEditorScreen(
                        noteId = route.noteId,
                        onNavigateBack = { navController.popBackStack() },
                        onShowSnackbar = showSnackbar,
                        viewModel = notesViewModel
                    )
                }

                composable<AppRoute.SongDetailRoute> { entry ->
                    val route = entry.toRoute<AppRoute.SongDetailRoute>()
                    SongDetailScreen(
                        songId = route.songId,
                        onNavigateBack = { navController.popBackStack() },
                        onShowSnackbar = showSnackbar
                    )
                }

                composable<AppRoute.GlobalSearchRoute> { entry ->
                    val route = entry.toRoute<AppRoute.GlobalSearchRoute>()
                    GlobalSearchScreen(
                        initialQuery = route.initialQuery,
                        onNavigateBack = { navController.popBackStack() },
                        onOpenBiblePassage = { bookId, chapter, verse ->
                            bibleViewModel.onIntent(
                                BibleReaderIntent.SelectLocation(
                                    bookId = bookId,
                                    chapter = chapter,
                                    highlightVerse = verse
                                )
                            )
                            activeTab = ActiveAppTab.BIBLE
                            navController.popBackStack()
                        },
                        onOpenSongDetail = { songId ->
                            navController.navigate(AppRoute.SongDetailRoute(songId))
                        }
                    )
                }
            }

            if (isMainTabRoute) {
                val hapticFeedback = LocalHapticFeedback.current
                val selectTab: (ActiveAppTab) -> Unit = remember(hapticFeedback) {
                    { targetTab ->
                        if (targetTab != activeTab) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            activeTab = targetTab
                        }
                    }
                }
                val navScrub = rememberNavBarScrubState(
                    tabIds = remember { MainToolbarTab.entries.map { it.tab } },
                    onHoverChange = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    },
                    onCommit = selectTab
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = FloatingToolbarDefaults.ScreenOffset),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From M3 (references/components/bars.md, references/tokens.md, SKILL.md):
                        // - Floating toolbars have elevation by default (Level 2 = 3.dp).
                        // - Multi-element container borders use outlineVariant at 1.dp (OutlineWidth = 1.0.dp)
                        //   so the floating dock has a subtle edge and soft shadow in both light and dark themes.
                        HorizontalFloatingToolbar(
                            expanded = true,
                            modifier = Modifier
                                .shadow(
                                    elevation = 3.dp,
                                    shape = FloatingToolbarDefaults.ContainerShape,
                                    clip = false
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                                    shape = FloatingToolbarDefaults.ContainerShape
                                ),
                            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = FloatingToolbarDefaults.ContentPadding,
                            shape = FloatingToolbarDefaults.ContainerShape
                        ) {
                            Row(
                                modifier = Modifier.navBarScrub(navScrub),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MainToolbarTab.entries.forEach { item ->
                                    val selected = item.tab == (navScrub.hoveredTab ?: activeTab)
                                    MainToolbarTabPill(
                                        label = item.label,
                                        selectedIcon = item.selectedIcon,
                                        unselectedIcon = item.unselectedIcon,
                                        selected = selected,
                                        onClick = { selectTab(item.tab) },
                                        modifier = Modifier.navBarScrubItem(navScrub, item.tab)
                                    )
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                navController.navigate(AppRoute.GlobalSearchRoute())
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                    shape = FloatingActionButtonDefaults.shape
                                ),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 6.dp
                            ),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Buscador Universal"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeepAliveTabContainer(
    isActive: Boolean,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "KeepAliveTabAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (isActive) 1f else 0f)
            .graphicsLayer { this.alpha = alpha }
            .then(
                if (!isActive) {
                    Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

@Composable
private fun MainToolbarTabPill(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 14.dp else 24.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "MainToolbarTabCornerMorph"
    )

    val animatedPadding by animateDpAsState(
        targetValue = if (selected) 16.dp else 12.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "MainToolbarTabPadding"
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "MainToolbarTabContainerColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "MainToolbarTabContentColor"
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics { role = Role.Tab },
        shape = RoundedCornerShape(cornerRadius),
        color = animatedContainerColor,
        contentColor = animatedContentColor,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = animatedPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = if (selected) null else label,
                modifier = Modifier.size(22.dp)
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)) + expandHorizontally(
                    animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) + shrinkHorizontally(
                    animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
