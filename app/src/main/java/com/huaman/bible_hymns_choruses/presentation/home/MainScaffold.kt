@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.huaman.bible_hymns_choruses.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.huaman.bible_hymns_choruses.core.datastore.ActiveAppTab
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferences
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferencesRepository
import com.huaman.bible_hymns_choruses.core.navigation.AppRoute
import com.huaman.bible_hymns_choruses.presentation.bookmarks.BookmarksScreen
import com.huaman.bible_hymns_choruses.presentation.bookmarks.BookmarksViewModel
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
            initialValue = UserPreferences()
        )

    fun setActiveTab(tab: ActiveAppTab) {
        viewModelScope.launch {
            userPreferencesRepository.updateLastActiveTab(tab)
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

    // Hoisted above AnimatedContent so switching tabs never recreates ViewModels,
    // re-queries Room, or drops scroll positions (matches Koda's zero-lag tab model).
    val bibleViewModel: BibleReaderViewModel = hiltViewModel()
    val songsViewModel: SongsViewModel = hiltViewModel()
    val bookmarksViewModel: BookmarksViewModel = hiltViewModel()
    val bibleListState = rememberLazyListState()
    val songsListState = rememberLazyListState()
    val bookmarksListState = rememberLazyListState()

    var activeTab by rememberSaveable { mutableStateOf(ActiveAppTab.BIBLE) }

    LaunchedEffect(activeTab) {
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

    val isMainTabRoute = currentDestination?.hasRoute<AppRoute.BibleReaderRoute>() ?: true

    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior),
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
                    val tabOrder = remember { MainToolbarTab.entries.map { it.tab } }
                    AnimatedContent(
                        targetState = activeTab,
                        label = "MainTabTransition",
                        transitionSpec = {
                            val initialIndex = tabOrder.indexOf(initialState)
                            val targetIndex = tabOrder.indexOf(targetState)
                            if (targetIndex > initialIndex) {
                                (slideInHorizontally(animationSpec = tween(220)) { width -> width / 4 } +
                                    fadeIn(animationSpec = tween(180))) togetherWith
                                    (slideOutHorizontally(animationSpec = tween(180)) { width -> -width / 6 } +
                                        fadeOut(animationSpec = tween(140)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(220)) { width -> -width / 4 } +
                                    fadeIn(animationSpec = tween(180))) togetherWith
                                    (slideOutHorizontally(animationSpec = tween(180)) { width -> width / 6 } +
                                        fadeOut(animationSpec = tween(140)))
                            }
                        }
                    ) { tab ->
                        when (tab) {
                            ActiveAppTab.BIBLE -> BibleReaderScreen(
                                onShowSnackbar = showSnackbar,
                                listState = bibleListState,
                                viewModel = bibleViewModel
                            )

                            ActiveAppTab.SONGS -> SongsScreen(
                                onNavigateToDetail = { songId ->
                                    navController.navigate(AppRoute.SongDetailRoute(songId))
                                },
                                onShowSnackbar = showSnackbar,
                                listState = songsListState,
                                viewModel = songsViewModel
                            )

                            ActiveAppTab.BOOKMARKS -> BookmarksScreen(
                                onOpenSongDetail = { songId ->
                                    navController.navigate(AppRoute.SongDetailRoute(songId))
                                },
                                onShowSnackbar = showSnackbar,
                                listState = bookmarksListState,
                                viewModel = bookmarksViewModel
                            )
                        }
                    }
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
                val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
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

                HorizontalFloatingToolbar(
                    expanded = true,
                    floatingActionButton = {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = {
                                navController.navigate(AppRoute.GlobalSearchRoute())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Buscador Universal"
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -FloatingToolbarDefaults.ScreenOffset - navBarBottomPadding),
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                        toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = FloatingToolbarDefaults.ContentPadding,
                    scrollBehavior = scrollBehavior,
                    shape = FloatingToolbarDefaults.ContainerShape,
                    floatingActionButtonPosition = FloatingToolbarHorizontalFabPosition.End
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
            }
        }
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
        targetValue = if (selected) 20.dp else 12.dp,
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
