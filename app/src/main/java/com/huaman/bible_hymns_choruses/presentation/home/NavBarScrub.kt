package com.huaman.bible_hymns_choruses.presentation.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.abs

/**
 * Drag along a navigation bar to scrub across destinations.
 *
 * The pill highlight follows the finger ([hoveredTab]) while dragging, and
 * navigation only commits on release so crossing multiple tabs composes the
 * final destination once. Normal taps remain untouched because the drag
 * detector only claims the pointer past touch slop.
 */
class NavBarScrubState<T : Any> {
    private var bar: LayoutCoordinates? = null
    private val items = mutableMapOf<T, LayoutCoordinates>()

    internal var tabIds: List<T> = emptyList()

    // Updated on composition rather than used as pointerInput keys so hovering
    // (which recomposes the bar) never cancels an active drag gesture.
    internal var onHoverChange: () -> Unit = {}
    internal var onCommit: (T) -> Unit = {}

    /** The tab currently under the finger during a drag, or null when idle. */
    var hoveredTab by mutableStateOf<T?>(null)
        private set

    internal fun registerBar(coordinates: LayoutCoordinates) {
        bar = coordinates
    }

    internal fun registerItem(tabId: T, coordinates: LayoutCoordinates) {
        items[tabId] = coordinates
    }

    internal fun tabAt(position: Float, vertical: Boolean = false): T? {
        val barCoords = bar?.takeIf { it.isAttached } ?: return null
        return tabIds.mapNotNull { id ->
            val itemCoords = items[id]?.takeIf { it.isAttached } ?: return@mapNotNull null
            val center = barCoords.localBoundingBoxOf(itemCoords).center
            id to abs((if (vertical) center.y else center.x) - position)
        }.minByOrNull { it.second }?.first
    }

    internal fun hover(tabId: T?) {
        hoveredTab = tabId
    }
}

@Composable
fun <T : Any> rememberNavBarScrubState(
    tabIds: List<T>,
    onHoverChange: () -> Unit,
    onCommit: (T) -> Unit
): NavBarScrubState<T> = remember { NavBarScrubState<T>() }.also {
    it.tabIds = tabIds
    it.onHoverChange = onHoverChange
    it.onCommit = onCommit
}

fun <T : Any> Modifier.navBarScrubItem(state: NavBarScrubState<T>, tabId: T): Modifier =
    onGloballyPositioned { state.registerItem(tabId, it) }

fun <T : Any> Modifier.navBarScrub(
    state: NavBarScrubState<T>,
    vertical: Boolean = false
): Modifier = this
    .onGloballyPositioned { state.registerBar(it) }
    .pointerInput(state, vertical) {
        if (vertical) {
            detectVerticalDragGestures(
                onDragStart = { offset -> state.hover(state.tabAt(offset.y, vertical = true)) },
                onDragEnd = {
                    val target = state.hoveredTab
                    state.hover(null)
                    if (target != null) state.onCommit(target)
                },
                onDragCancel = { state.hover(null) },
                onVerticalDrag = { change, _ ->
                    change.consume()
                    val target = state.tabAt(change.position.y, vertical = true)
                    if (target != null && target != state.hoveredTab) {
                        state.hover(target)
                        state.onHoverChange()
                    }
                }
            )
            return@pointerInput
        }
        detectHorizontalDragGestures(
            onDragStart = { offset -> state.hover(state.tabAt(offset.x)) },
            onDragEnd = {
                val target = state.hoveredTab
                state.hover(null)
                if (target != null) state.onCommit(target)
            },
            onDragCancel = { state.hover(null) },
            onHorizontalDrag = { change, _ ->
                change.consume()
                val target = state.tabAt(change.position.x)
                if (target != null && target != state.hoveredTab) {
                    state.hover(target)
                    state.onHoverChange()
                }
            }
        )
    }
