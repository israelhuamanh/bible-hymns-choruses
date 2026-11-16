@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.huaman.bible_hymns_choruses.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferences
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferencesRepository
import com.huaman.bible_hymns_choruses.core.navigation.AppRoute
import com.huaman.bible_hymns_choruses.presentation.bookmarks.BookmarksScreen
import com.huaman.bible_hymns_choruses.presentation.reader.BibleReaderScreen
import com.huaman.bible_hymns_choruses.presentation.search.GlobalSearchScreen
import com.huaman.bible_hymns_choruses.presentation.songs.SongDetailScreen
import com.huaman.bible_hymns_choruses.presentation.songs.SongsScreen
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

private enum class MainNavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val activeAppTab: ActiveAppTab?
) {
    BIBLE(
        label = "Biblia",
        selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        activeAppTab = ActiveAppTab.BIBLE
    ),
    SONGS(
        label = "Cantos",
        selectedIcon = Icons.Rounded.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic,
        activeAppTab = ActiveAppTab.SONGS
    ),
    BOOKMARKS(
        label = "Guardados",
        selectedIcon = Icons.Rounded.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        activeAppTab = ActiveAppTab.BOOKMARKS
    ),
    SEARCH(
        label = "Buscar",
        selectedIcon = Icons.Rounded.Search,
        unselectedIcon = Icons.Outlined.Search,
        activeAppTab = null
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

    val showSnackbar: (String) -> Unit = remember(snackbarHostState) {
        { msg ->
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(msg)
            }
        }
    }

    val isMainTabRoute = currentDestination?.let { dest ->
        dest.hasRoute<AppRoute.BibleReaderRoute>() ||
            dest.hasRoute<AppRoute.SongsRoute>() ||
            dest.hasRoute<AppRoute.BookmarksRoute>() ||
            dest.hasRoute<AppRoute.GlobalSearchRoute>()
    } ?: true

    val activeTab = when {
        currentDestination?.hasRoute<AppRoute.SongsRoute>() == true -> MainNavTab.SONGS
        currentDestination?.hasRoute<AppRoute.BookmarksRoute>() == true -> MainNavTab.BOOKMARKS
        currentDestination?.hasRoute<AppRoute.GlobalSearchRoute>() == true -> MainNavTab.SEARCH
        else -> MainNavTab.BIBLE
    }

    // Hide-on-scroll with 40dp threshold (matching SpringFloatingNavBar behavior)
    val density = LocalDensity.current
    val hideThresholdPx = with(density) { 40.dp.toPx() }
    var isNavBarVisible by remember { mutableStateOf(true) }
    var scrollAccumulatorPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(activeTab) {
        isNavBarVisible = true
        scrollAccumulatorPx = 0f
    }

    val nestedScrollConnection = remember(hideThresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                if (deltaY < 0f) {
                    // Scrolling down -> accumulate downward scroll
                    scrollAccumulatorPx = (scrollAccumulatorPx - deltaY).coerceAtLeast(0f)
                    if (scrollAccumulatorPx > hideThresholdPx && isNavBarVisible) {
                        isNavBarVisible = false
                    }
                } else if (deltaY > 0f) {
                    // Scrolling up -> reveal navbar
                    scrollAccumulatorPx = 0f
                    if (!isNavBarVisible) {
                        isNavBarVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = if (isMainTabRoute) 96.dp else 16.dp)
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
                composable<AppRoute.BibleReaderRoute> { entry ->
                    val route = entry.toRoute<AppRoute.BibleReaderRoute>()
                    BibleReaderScreen(
                        initialBookId = route.bookId.takeIf { it > 0 && route.highlightVerse != null },
                        initialChapter = route.chapter.takeIf { it > 0 && route.highlightVerse != null },
                        highlightVerse = route.highlightVerse,
                        onShowSnackbar = showSnackbar
                    )
                }

                composable<AppRoute.SongsRoute> {
                    SongsScreen(
                        onNavigateToDetail = { songId ->
                            navController.navigate(AppRoute.SongDetailRoute(songId))
                        },
                        onShowSnackbar = showSnackbar
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

                composable<AppRoute.BookmarksRoute> {
                    BookmarksScreen(
                        onOpenSongDetail = { songId ->
                            navController.navigate(AppRoute.SongDetailRoute(songId))
                        },
                        onShowSnackbar = showSnackbar
                    )
                }

                composable<AppRoute.GlobalSearchRoute> { entry ->
                    val route = entry.toRoute<AppRoute.GlobalSearchRoute>()
                    GlobalSearchScreen(
                        initialQuery = route.initialQuery,
                        onNavigateBack = { navController.popBackStack() },
                        onOpenBiblePassage = { bookId, chapter, verse ->
                            navController.navigate(
                                AppRoute.BibleReaderRoute(
                                    bookId = bookId,
                                    chapter = chapter,
                                    highlightVerse = verse
                                )
                            ) {
                                popUpTo<AppRoute.BibleReaderRoute> { inclusive = true }
                            }
                        },
                        onOpenSongDetail = { songId ->
                            navController.navigate(AppRoute.SongDetailRoute(songId))
                        }
                    )
                }
            }

            if (isMainTabRoute) {
                val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val hiddenOffsetPx = with(density) { 120.dp.toPx() }
                val verticalOffsetPx by animateFloatAsState(
                    targetValue = if (isNavBarVisible) 0f else hiddenOffsetPx,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "SpringNavBarVisibilityOffset"
                )

                SpringFloatingNavBar(
                    tabs = MainNavTab.entries,
                    selectedIndex = activeTab.ordinal,
                    onSelectTab = { item ->
                        item.activeAppTab?.let { appViewModel.setActiveTab(it) }
                        val targetRoute: AppRoute = when (item) {
                            MainNavTab.BIBLE -> AppRoute.BibleReaderRoute()
                            MainNavTab.SONGS -> AppRoute.SongsRoute()
                            MainNavTab.BOOKMARKS -> AppRoute.BookmarksRoute
                            MainNavTab.SEARCH -> AppRoute.GlobalSearchRoute()
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp + navBarBottomPadding)
                        .graphicsLayer { translationY = verticalOffsetPx }
                )
            }
        }
    }
}

@Composable
private fun SpringFloatingNavBar(
    tabs: List<MainNavTab>,
    selectedIndex: Int,
    onSelectTab: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val targetPillColor = when (tabs[selectedIndex]) {
        MainNavTab.BIBLE -> MaterialTheme.colorScheme.primaryContainer
        MainNavTab.SONGS -> MaterialTheme.colorScheme.secondaryContainer
        MainNavTab.BOOKMARKS -> MaterialTheme.colorScheme.tertiaryContainer
        MainNavTab.SEARCH -> MaterialTheme.colorScheme.primaryContainer
    }
    val targetContentColor = when (tabs[selectedIndex]) {
        MainNavTab.BIBLE -> MaterialTheme.colorScheme.onPrimaryContainer
        MainNavTab.SONGS -> MaterialTheme.colorScheme.onSecondaryContainer
        MainNavTab.BOOKMARKS -> MaterialTheme.colorScheme.onTertiaryContainer
        MainNavTab.SEARCH -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val animatedPillColor by animateColorAsState(
        targetValue = targetPillColor,
        animationSpec = spring(stiffness = 450f, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "SpringNavBarPillColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val density = LocalDensity.current
            val itemWidth = maxWidth / tabs.size
            val itemWidthPx = with(density) { itemWidth.toPx() }
            val pillMarginPx = with(density) { 6.dp.toPx() }
            val pillOffsetPx by animateFloatAsState(
                targetValue = itemWidthPx * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 450f
                ),
                label = "SpringNavBarPillSlide"
            )

            // Sliding spring-physics pill indicator (GPU-accelerated, zero relayout)
            Box(
                modifier = Modifier
                    .graphicsLayer { translationX = pillOffsetPx + pillMarginPx }
                    .padding(vertical = 6.dp)
                    .width(itemWidth - 12.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(22.dp))
                    .background(animatedPillColor)
            )

            // Touch & icon/label layer
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    val itemColor by animateColorAsState(
                        targetValue = if (selected) {
                            targetContentColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = spring(stiffness = 450f),
                        label = "SpringNavBarItemColor"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .semantics { role = Role.Tab }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectTab(item)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = itemColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = itemColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = if (selected) {
                                MaterialTheme.typography.labelMediumEmphasized
                            } else {
                                MaterialTheme.typography.labelMedium
                            }
                        )
                    }
                }
            }
        }
    }
}
