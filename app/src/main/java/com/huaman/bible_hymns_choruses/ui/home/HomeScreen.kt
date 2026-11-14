package com.huaman.bible_hymns_choruses.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huaman.bible_hymns_choruses.ui.theme.BiblehymnschorusesTheme

/**
 * Primary sections displayed as tabs inside the bottom [HorizontalFloatingToolbar].
 * Per M3 Expressive (`bars.md`), a floating toolbar can act as tabs switching the body content
 * underneath, and must NEVER be paired simultaneously with a bottom `NavigationBar`.
 */
enum class SacredSection(
    val title: String,
    val headerTitle: String,
    val headerOverline: String,
    val headerSubtitle: String,
    val icon: ImageVector,
    val filterCategories: List<String>
) {
    BIBLE(
        title = "Biblia",
        headerTitle = "Santa Biblia",
        headerOverline = "REINA-VALERA 1960 • LECTURA DIARIA",
        headerSubtitle = "Explora las Escrituras con tipografía expresiva y lectura enfocada.",
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        filterCategories = listOf("Todos", "Pentateuco", "Salmos", "Evangelios", "Epístolas")
    ),
    HYMNS(
        title = "Himnos",
        headerTitle = "Himnario",
        headerOverline = "400+ HIMNOS CLÁSICOS • TONALIDADES Y ACORDES",
        headerSubtitle = "Cantos solemnes de adoración, gratitud y fortaleza espiritual.",
        icon = Icons.Rounded.LibraryMusic,
        filterCategories = listOf("Todos", "Adoración", "Gracia", "Consagración", "Santa Cena")
    ),
    CHORUSES(
        title = "Coros",
        headerTitle = "Coros Vivos",
        headerOverline = "ALABANZA CONGREGACIONAL • RITMO Y GOZO",
        headerSubtitle = "Cánticos breves de júbilo, avivamiento y comunión íntima.",
        icon = Icons.Rounded.Lyrics,
        filterCategories = listOf("Todos", "Júbilo", "Espíritu Santo", "Gratitud", "Adoración")
    )
}

data class FeaturedSacredContent(
    val badgeLabel: String,
    val title: String,
    val referenceOrNumber: String,
    val excerpt: String,
    val metadataTag: String,
    val primaryActionLabel: String,
    val secondaryActionLabel: String
)

data class ContrastingBentoItem(
    val id: String,
    val overline: String,
    val title: String,
    val subtitle: String,
    val tag: String
)

data class SacredCatalogItem(
    val id: String,
    val code: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val keyOrChapterCount: String
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BibleHymnsChorusesHomeScreen(
    modifier: Modifier = Modifier
) {
    var selectedSection by rememberSaveable { mutableStateOf(SacredSection.BIBLE) }
    var selectedCategory by rememberSaveable(selectedSection) {
        mutableStateOf(selectedSection.filterCategories.first())
    }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isPlayingFeaturedAudio by rememberSaveable(selectedSection) { mutableStateOf(false) }
    val savedItemIds = remember { mutableStateListOf("sal-23", "himno-001", "coro-104") }

    // M3 Expressive FloatingToolbar scroll behavior (From M3: exits downward on scroll)
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main scrollable feed
            val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
            val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
            AnimatedContent(
                targetState = selectedSection,
                transitionSpec = {
                    (fadeIn(effectsSpec) + scaleIn(spatialSpec, initialScale = 0.96f))
                        .togetherWith(fadeOut(effectsSpec) + scaleOut(spatialSpec, targetScale = 0.98f))
                },
                label = "SacredSectionContentTransition"
            ) { section ->
                val featured = remember(section) { getFeaturedContentFor(section) }
                val bentoPair = remember(section) { getBentoPairFor(section) }
                val catalogItems = remember(section, selectedCategory, searchQuery) {
                    getCatalogItemsFor(section).filter { item ->
                        val matchesCategory =
                            selectedCategory == "Todos" || item.category.equals(selectedCategory, ignoreCase = true)
                        val matchesQuery = searchQuery.isBlank() ||
                            item.title.contains(searchQuery, ignoreCase = true) ||
                            item.subtitle.contains(searchQuery, ignoreCase = true) ||
                            item.code.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesQuery
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, // My design decision: 20.dp screen margin on mobile
                        end = 20.dp,
                        top = 16.dp,
                        // Extra bottom padding so content clears the 64.dp FloatingToolbar + 16.dp margin
                        bottom = 128.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Expressive Header with Google Sans Flex Emphasized Typography
                    item(key = "expressive_header") {
                        ExpressiveSectionHeader(
                            section = section,
                            isSearchActive = isSearchExpanded,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onToggleSearch = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) searchQuery = ""
                            }
                        )
                    }

                    // 2. Interactive Category Filter Row with Shape-Morphing ToggleButtons
                    item(key = "category_toggles") {
                        ExpressiveCategoryToggleRow(
                            categories = section.filterCategories,
                            selectedCategory = selectedCategory,
                            onSelectCategory = { selectedCategory = it }
                        )
                    }

                    // 3. Featured Hero Card with 32.dp outer radius, 16.dp nested radius (32 - 16 = 16),
                    //    MaterialShapes decorative badge, and Medium + Small Shape-Morphing Buttons
                    item(key = "featured_hero_card") {
                        FeaturedExpressiveHeroCard(
                            section = section,
                            content = featured,
                            isPlaying = isPlayingFeaturedAudio,
                            isBookmarked = savedItemIds.contains(section.name),
                            onPrimaryActionClick = { isPlayingFeaturedAudio = !isPlayingFeaturedAudio },
                            onSecondaryActionClick = {
                                if (savedItemIds.contains(section.name)) {
                                    savedItemIds.remove(section.name)
                                } else {
                                    savedItemIds.add(section.name)
                                }
                            }
                        )
                    }

                    // 4. Contrasting Shape Bento Pair (Asymmetric 28dp/8dp vs 20dp tension)
                    item(key = "contrasting_bento_pair") {
                        ContrastingShapeBentoRow(
                            items = bentoPair,
                            onActionClick = { /* Quick jump action */ }
                        )
                    }

                    // 5. Section Subheader + Grouped Expressive Catalog Cards
                    item(key = "catalog_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (section) {
                                    SacredSection.BIBLE -> "Libros y pasajes frecuentes"
                                    SacredSection.HYMNS -> "Selección del himnario"
                                    SacredSection.CHORUSES -> "Coros para hoy"
                                },
                                style = MaterialTheme.typography.headlineSmallEmphasized,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${catalogItems.size} disponibles",
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(
                        items = catalogItems,
                        key = { it.id }
                    ) { item ->
                        ExpressiveCatalogCard(
                            item = item,
                            isSaved = savedItemIds.contains(item.id),
                            onToggleSave = {
                                if (savedItemIds.contains(item.id)) {
                                    savedItemIds.remove(item.id)
                                } else {
                                    savedItemIds.add(item.id)
                                }
                            },
                            onOpenClick = { /* Open chapter / hymn / chorus */ }
                        )
                    }
                }
            }

            // Bottom HorizontalFloatingToolbar coupled with a Quick Search FAB
            // From M3 (`bars.md` & `component-tokens.md` `FloatingToolbar`):
            // - Height: 64.dp, Shape: CornerFull, Outside margin: 16.dp (`FloatingToolbarDefaults.ScreenOffset`)
            // - Leading/Trailing space: 8.dp, Between space: 4.dp
            // - Coupled with `FloatingToolbarDefaults.VibrantFloatingActionButton`
            val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            HorizontalFloatingToolbar(
                expanded = true,
                floatingActionButton = {
                    FloatingToolbarDefaults.VibrantFloatingActionButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                        }
                    ) {
                        AnimatedContent(
                            targetState = isSearchExpanded,
                            label = "QuickSearchFabIcon"
                        ) { expanded ->
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = if (expanded) {
                                    "Cerrar búsqueda rápida"
                                } else {
                                    "Búsqueda rápida en ${selectedSection.title}"
                                }
                            )
                        }
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp), // From M3: FloatingToolbar.ContainerBetweenSpace = 4.0.dp
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SacredSection.entries.forEach { section ->
                        val isSelected = section == selectedSection
                        FloatingToolbarTabPill(
                            section = section,
                            selected = isSelected,
                            onClick = { selectedSection = section }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab item inside [HorizontalFloatingToolbar].
 *
 * Follows M3 Expressive (`bars.md` & `expressive-app-bars-toolbars-and-buttons.png`):
 * - Uses a fully-rounded pill container (`CircleShape` / `CornerFull`) when idle and morphs
 *   subtle corner tension on press (`8.dp` -> `50%`) using `MaterialTheme.motionScheme.fastSpatialSpec()`,
 *   while staying round when selected so it never places static square buttons inside the round toolbar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FloatingToolbarTabPill(
    section: SacredSection,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // From M3 (`buttons.md`): Small button height = 48.dp inside 64.dp toolbar (64 - 2*8 = 48.dp),
    // morphs corner radius when pressed using M3 Expressive fastSpatialSpec.
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 14.dp else 24.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ToolbarTabCornerMorph"
    )

    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics { role = Role.Tab },
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp), // From M3: ButtonSmall LeadingSpace/TrailingSpace = 16.0.dp
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp) // From M3: ButtonSmall IconLabelSpace = 8.0.dp
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp) // From M3: ButtonSmall IconSize = 20.0.dp
            )
            Text(
                text = section.title,
                style = if (selected) {
                    MaterialTheme.typography.labelLargeEmphasized
                } else {
                    MaterialTheme.typography.labelLarge
                }
            )
        }
    }
}

/**
 * Expressive editorial header using `Google Sans Flex` and M3 Expressive `*Emphasized` styles.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveSectionHeader(
    section: SacredSection,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Overline pill badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = section.headerOverline,
                        style = MaterialTheme.typography.labelSmallEmphasized
                    )
                }
            }

            // Shape-morphing FilledTonalIconButton (From M3: SmallIconButton 40.dp, Round -> 8.dp on press)
            FilledTonalIconButton(
                onClick = onToggleSearch,
                shapes = IconButtonDefaults.shapes(
                    shape = IconButtonDefaults.smallRoundShape,
                    pressedShape = IconButtonDefaults.smallPressedShape
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = "Historial de lectura y cantos"
                )
            }
        }

        // Expressive Display Header (From M3: displaySmallEmphasized 36sp/44sp Medium in Google Sans Flex)
        Text(
            text = section.headerTitle,
            style = MaterialTheme.typography.displaySmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = section.headerSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Expandable Quick Search bar triggered from the coupled FAB or header
        AnimatedVisibility(visible = isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = {
                    Text(
                        text = when (section) {
                            SacredSection.BIBLE -> "Buscar libro, capítulo o palabra clave (ej. Salmo 23)..."
                            SacredSection.HYMNS -> "Buscar himno por número, título o tema (ej. #001)..."
                            SacredSection.CHORUSES -> "Buscar coro por letra, tono o ritmo..."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        FilledTonalIconButton(
                            onClick = { onSearchQueryChange("") },
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.extraSmallRoundShape,
                                pressedShape = IconButtonDefaults.extraSmallPressedShape
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Limpiar búsqueda",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp), // From M3: CornerExtraLarge = 28.0.dp
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    }
}

/**
 * Row of M3 Expressive [ToggleButton]s that morph shape between:
 * - Unchecked: `CornerFull` (`50%` pill)
 * - Pressed: `CornerSmall` (`8.dp`)
 * - Checked/Selected: `CornerMedium` (`12.dp` square)
 * per `component-tokens.md` (`ButtonSmall` tokens).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveCategoryToggleRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it }) { category ->
            val isChecked = category == selectedCategory
            ToggleButton(
                checked = isChecked,
                onCheckedChange = { onSelectCategory(category) },
                shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight),
                colors = ToggleButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = category,
                    style = if (isChecked) {
                        MaterialTheme.typography.labelLargeEmphasized
                    } else {
                        MaterialTheme.typography.labelLarge
                    }
                )
            }
        }
    }
}

/**
 * Hero card showcasing M3 Expressive shape contrast, nested corner radius math, decorative
 * [MaterialShapes] badge, and Medium + Small shape-morphing buttons:
 * - Outer card corner radius: `32.dp` (From M3 Expressive 10-step scale: `0/4/8/12/16/20/28/32/48/full`)
 * - Outer card internal padding: `16.dp`
 * - Nested excerpt container corner radius: `32.dp - 16.dp = 16.dp` (`inner = outer - padding` rule from `shape.md`)
 * - Decorative icon badge: uses `MaterialShapes.Sunny.toShape()`, `MaterialShapes.Clover4Leaf.toShape()`,
 *   or `MaterialShapes.Arch.toShape()` strictly on the decorative badge (`shape.md`).
 * - Primary CTA: Medium `Button` (`56.dp` height, `ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight)`)
 *   that morphs from `CornerFull` to `CornerMedium` (`12.dp`) when pressed.
 * - Secondary CTA: `FilledTonalIconButton` with `IconButtonDefaults.shapes()` that morphs on press.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FeaturedExpressiveHeroCard(
    section: SacredSection,
    content: FeaturedSacredContent,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    onPrimaryActionClick: () -> Unit,
    onSecondaryActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Decorative MaterialShape per section (applied ONLY to decorative icon badge, per shape.md)
    val decorativeBadgeShape: Shape = when (section) {
        SacredSection.BIBLE -> MaterialShapes.Sunny.toShape()
        SacredSection.HYMNS -> MaterialShapes.Clover4Leaf.toShape()
        SacredSection.CHORUSES -> MaterialShapes.Cookie9Sided.toShape()
    }

    // Non-clickable Card container hosting interactive shape-morphing buttons (per containment.md)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp), // From M3 Expressive 10-step scale: 32.dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Outer padding = 16.dp -> Nested radius must be 32.dp - 16.dp = 16.dp
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Decorative MaterialShapes badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(decorativeBadgeShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (section) {
                                SacredSection.BIBLE -> Icons.AutoMirrored.Rounded.MenuBook
                                SacredSection.HYMNS -> Icons.Rounded.MusicNote
                                SacredSection.CHORUSES -> Icons.Rounded.GraphicEq
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = content.badgeLabel,
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = content.title,
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Metadata pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = content.referenceOrNumber,
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Nested excerpt box following mandatory M3 rule: inner radius (16.dp) = outer (32.dp) - padding (16.dp)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = content.excerpt,
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = content.metadataTag,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action row with M3 Expressive Shape-Morphing Buttons
            // From M3 (`component-tokens.md` `ButtonMedium`):
            // - ContainerHeight = 56.0.dp, Leading/TrailingSpace = 24.0.dp, IconLabelSpace = 8.0.dp,
            // - Morphs from CornerFull (round) to CornerMedium (12.dp) on press!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPrimaryActionClick,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = ButtonDefaults.MediumContainerHeight),
                    contentPadding = ButtonDefaults.MediumContentPadding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.MediumIconSize)
                    )
                    Spacer(Modifier.width(ButtonDefaults.MediumIconSpacing))
                    Text(
                        text = if (isPlaying) "Pausar lectura / audio" else content.primaryActionLabel,
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                }

                // Medium Shape-Morphing IconButton (56.dp height, Round -> 12.dp on press)
                FilledTonalIconButton(
                    onClick = onSecondaryActionClick,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.mediumRoundShape,
                        pressedShape = IconButtonDefaults.mediumPressedShape
                    ),
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = content.secondaryActionLabel,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Two side-by-side cards demonstrating M3 Expressive contrasting shapes (`shape.md` "Embrace tension"):
 * - Left Card: Asymmetric sculpted corners (`topStart = 28.dp, topEnd = 8.dp, bottomEnd = 28.dp, bottomStart = 8.dp`)
 *   in `tertiaryContainer` with a `MaterialShapes.Arch.toShape()` decorative badge.
 * - Right Card: Symmetrical `20.dp` (`RoundedCornerShape(20.dp)`, new in M3 Expressive scale) in `secondaryContainer`
 *   with a shape-morphing `FilledTonalButton` (`ButtonDefaults.shapes()`).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContrastingShapeBentoRow(
    items: Pair<ContrastingBentoItem, ContrastingBentoItem>,
    onActionClick: (ContrastingBentoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val (leftItem, rightItem) = items
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Card: Asymmetric corner tension (28.dp & 8.dp from the M3 10-step scale)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 8.dp,
                bottomEnd = 28.dp,
                bottomStart = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialShapes.Arch.toShape())
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = leftItem.overline,
                        style = MaterialTheme.typography.labelSmallEmphasized
                    )
                }

                Text(
                    text = leftItem.title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = leftItem.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Shape-morphing Small Button (40.dp height, Round -> 8.dp on press)
                Button(
                    onClick = { onActionClick(leftItem) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(
                        text = leftItem.tag,
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
            }
        }

        // Right Card: Contrasting 20.dp rounded rectangle (20.dp is new in M3 Expressive scale)
        ElevatedCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialShapes.Gem.toShape())
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LibraryMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = rightItem.overline,
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = rightItem.title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = rightItem.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Shape-morphing FilledTonalButton (40.dp height, Round -> 8.dp on press)
                FilledTonalButton(
                    onClick = { onActionClick(rightItem) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = rightItem.tag,
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
            }
        }
    }
}

/**
 * Catalog item card with contrasting `20.dp` outer radius, `12.dp` padding, and `8.dp` inner badge
 * (`20.dp - 12.dp = 8.dp` nested radius rule) plus shape-morphing `OutlinedButton` and `FilledIconButton`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveCatalogCard(
    item: SacredCatalogItem,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), // From M3 Expressive 10-step scale: 20.dp
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Outer 20.dp - padding 12.dp = 8.dp inner radius
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nested number/book badge with exact 8.dp radius (20.dp - 12.dp = 8.dp)
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.code,
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    Text(
                        text = item.keyOrChapterCount,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.category.uppercase(),
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Shape-morphing OutlinedButton ("Abrir") + Shape-morphing FilledTonalIconButton
            OutlinedButton(
                onClick = onOpenClick,
                shapes = ButtonDefaults.shapes(
                    shape = ButtonDefaults.shape,
                    pressedShape = ButtonDefaults.extraSmallPressedShape
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Abrir",
                    style = MaterialTheme.typography.labelMediumEmphasized
                )
            }

            FilledIconButton(
                onClick = onToggleSave,
                shapes = IconButtonDefaults.shapes(
                    shape = IconButtonDefaults.smallSquareShape,
                    pressedShape = IconButtonDefaults.smallPressedShape
                ),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isSaved) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor = if (isSaved) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isSaved) "Quitar de favoritos" else "Guardar en favoritos",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getFeaturedContentFor(section: SacredSection): FeaturedSacredContent =
    when (section) {
        SacredSection.BIBLE -> FeaturedSacredContent(
            badgeLabel = "VERSÍCULO DESTACADO DEL DÍA",
            title = "Salmos 23:1–4",
            referenceOrNumber = "RVR1960",
            excerpt = "«Jehová es mi pastor; nada me faltará. En lugares de delicados pastos me hará descansar; junto a aguas de reposo me pastoreará. Confortará mi alma...»",
            metadataTag = "Poéticos • Salmo de David • Audio narrado disponible",
            primaryActionLabel = "Leer capítulo",
            secondaryActionLabel = "Guardar pasaje"
        )

        SacredSection.HYMNS -> FeaturedSacredContent(
            badgeLabel = "HIMNO SOLEMNE DESTACADO",
            title = "¡Santo! ¡Santo! ¡Santo!",
            referenceOrNumber = "Himno #001",
            excerpt = "«¡Santo! ¡Santo! ¡Santo! Señor Omnipotente, siempre el labio mío loores te dará. ¡Santo! ¡Santo! ¡Santo! Te adoro reverente, Dios en tres personas, bendita Trinidad.»",
            metadataTag = "Tonalidad: Re Mayor (D) • Compás 4/4 • Reginald Heber",
            primaryActionLabel = "Cantar con acordes",
            secondaryActionLabel = "Guardar himno"
        )

        SacredSection.CHORUSES -> FeaturedSacredContent(
            badgeLabel = "CORO DE AVIVAMIENTO Y ADORACIÓN",
            title = "Hay una fuente en mí",
            referenceOrNumber = "Coro #104",
            excerpt = "«Hay una fuente en mí, que está brotando, que está fluyendo dentro de mí: es un río de alabanza y de adoración, dirigido hacia ti, Señor Jesús, recíbelo.»",
            metadataTag = "Tonalidad: Sol Mayor (G) • Ritmo: Adoración Suave • 74 BPM",
            primaryActionLabel = "Iniciar secuencia",
            secondaryActionLabel = "Guardar coro"
        )
    }

private fun getBentoPairFor(section: SacredSection): Pair<ContrastingBentoItem, ContrastingBentoItem> =
    when (section) {
        SacredSection.BIBLE -> Pair(
            ContrastingBentoItem(
                id = "plan-anual",
                overline = "PLAN ACTIVO",
                title = "Evangelio de Juan",
                subtitle = "Día 14 • Juan 15: La vid verdadera",
                tag = "Continuar"
            ),
            ContrastingBentoItem(
                id = "paralelos",
                overline = "ESTUDIO",
                title = "Referencias cruzadas",
                subtitle = "Compara pasajes del Antiguo y Nuevo Testamento",
                tag = "Explorar"
            )
        )

        SacredSection.HYMNS -> Pair(
            ContrastingBentoItem(
                id = "transponer",
                overline = "MÚSICOS",
                title = "Transpositor rápido",
                subtitle = "Ajusta acordes para piano, guitarra o voz",
                tag = "Cambiar tono"
            ),
            ContrastingBentoItem(
                id = "indice-tematico",
                overline = "LITURGIA",
                title = "Orden de culto",
                subtitle = "Lista preparada para el servicio dominical",
                tag = "Ver lista"
            )
        )

        SacredSection.CHORUSES -> Pair(
            ContrastingBentoItem(
                id = "cadena-coros",
                overline = "POPURRÍ",
                title = "Cadena de Júbilo",
                subtitle = "5 coros enlazados en Mi menor (Em)",
                tag = "Tocar cadena"
            ),
            ContrastingBentoItem(
                id = "cancionero-vivo",
                overline = "MODO PROYECCIÓN",
                title = "Letra en pantalla",
                subtitle = "Tipografía de alto contraste para congregación",
                tag = "Proyectar"
            )
        )
    }

private fun getCatalogItemsFor(section: SacredSection): List<SacredCatalogItem> =
    when (section) {
        SacredSection.BIBLE -> listOf(
            SacredCatalogItem(
                id = "gen-1",
                code = "GÉN",
                title = "Génesis",
                subtitle = "La creación, los patriarcas y las promesas del pacto",
                category = "Pentateuco",
                keyOrChapterCount = "50 cap."
            ),
            SacredCatalogItem(
                id = "sal-23",
                code = "SAL",
                title = "Salmos",
                subtitle = "Cánticos de alabanza, oración y confianza en Dios",
                category = "Salmos",
                keyOrChapterCount = "150 cap."
            ),
            SacredCatalogItem(
                id = "mat-5",
                code = "MAT",
                title = "San Mateo",
                subtitle = "El Sermón del Monte y el Reino de los Cielos",
                category = "Evangelios",
                keyOrChapterCount = "28 cap."
            ),
            SacredCatalogItem(
                id = "jua-1",
                code = "JUA",
                title = "San Juan",
                subtitle = "El Verbo hecho carne y los discursos de vida eterna",
                category = "Evangelios",
                keyOrChapterCount = "21 cap."
            ),
            SacredCatalogItem(
                id = "rom-8",
                code = "ROM",
                title = "Romanos",
                subtitle = "La justificación por la fe y la vida en el Espíritu",
                category = "Epístolas",
                keyOrChapterCount = "16 cap."
            )
        )

        SacredSection.HYMNS -> listOf(
            SacredCatalogItem(
                id = "himno-001",
                code = "#001",
                title = "¡Santo! ¡Santo! ¡Santo!",
                subtitle = "Señor Omnipotente, siempre el labio mío loores te dará",
                category = "Adoración",
                keyOrChapterCount = "Tono D"
            ),
            SacredCatalogItem(
                id = "himno-042",
                code = "#042",
                title = "Sublime Gracia",
                subtitle = "Sublime gracia del Señor que a un infeliz salvó",
                category = "Gracia",
                keyOrChapterCount = "Tono G"
            ),
            SacredCatalogItem(
                id = "himno-118",
                code = "#118",
                title = "Castillo Fuerte es Nuestro Dios",
                subtitle = "Defensa y buen escudo en medio de la tempestad",
                category = "Adoración",
                keyOrChapterCount = "Tono C"
            ),
            SacredCatalogItem(
                id = "himno-205",
                code = "#205",
                title = "Todo a Cristo Yo Me Rindo",
                subtitle = "Con el fin de serle fiel, para siempre quiero amarle",
                category = "Consagración",
                keyOrChapterCount = "Tono F"
            ),
            SacredCatalogItem(
                id = "himno-312",
                code = "#312",
                title = "En la Cruz",
                subtitle = "Me hirió el pecado, fui a Jesús, mostréle mi dolor",
                category = "Santa Cena",
                keyOrChapterCount = "Tono E"
            )
        )

        SacredSection.CHORUSES -> listOf(
            SacredCatalogItem(
                id = "coro-104",
                code = "C-104",
                title = "Hay una fuente en mí",
                subtitle = "Es un río de alabanza y de adoración dirigido hacia ti",
                category = "Adoración",
                keyOrChapterCount = "Tono G"
            ),
            SacredCatalogItem(
                id = "coro-112",
                code = "C-112",
                title = "Alabaré, alabaré a mi Señor",
                subtitle = "Juan vio el número de los redimidos y todos alababan",
                category = "Júbilo",
                keyOrChapterCount = "Tono E"
            ),
            SacredCatalogItem(
                id = "coro-128",
                code = "C-128",
                title = "Espíritu de Dios, llena mi vida",
                subtitle = "Llena mi alma, llena mi ser con tu dulce presencia",
                category = "Espíritu Santo",
                keyOrChapterCount = "Tono Am"
            ),
            SacredCatalogItem(
                id = "coro-145",
                code = "C-145",
                title = "Demos gracias al Señor",
                subtitle = "Demos gracias por su amor y su fidelidad cada mañana",
                category = "Gratitud",
                keyOrChapterCount = "Tono C"
            ),
            SacredCatalogItem(
                id = "coro-160",
                code = "C-160",
                title = "Yo navegaba en alta mar",
                subtitle = "Y Cristo vino a mi barca y calmó la tempestad",
                category = "Júbilo",
                keyOrChapterCount = "Tono Dm"
            )
        )
    }

@Preview(showBackground = true, name = "M3 Expressive Home - Light")
@Composable
private fun BibleHymnsChorusesHomeScreenLightPreview() {
    BiblehymnschorusesTheme(darkTheme = false, dynamicColor = false) {
        BibleHymnsChorusesHomeScreen()
    }
}

@Preview(showBackground = true, name = "M3 Expressive Home - Dark")
@Composable
private fun BibleHymnsChorusesHomeScreenDarkPreview() {
    BiblehymnschorusesTheme(darkTheme = true, dynamicColor = false) {
        BibleHymnsChorusesHomeScreen()
    }
}
