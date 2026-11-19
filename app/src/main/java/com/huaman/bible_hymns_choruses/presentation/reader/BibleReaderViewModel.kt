package com.huaman.bible_hymns_choruses.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferencesRepository
import com.huaman.bible_hymns_choruses.domain.model.BibleBook
import com.huaman.bible_hymns_choruses.domain.model.BibleVerse
import com.huaman.bible_hymns_choruses.domain.model.BookmarkType
import com.huaman.bible_hymns_choruses.domain.usecase.GetBibleBooksUseCase
import com.huaman.bible_hymns_choruses.domain.usecase.GetBibleChapterUseCase
import com.huaman.bible_hymns_choruses.domain.usecase.ManageBookmarksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BibleReaderViewModel @Inject constructor(
    private val getBibleBooksUseCase: GetBibleBooksUseCase,
    private val getBibleChapterUseCase: GetBibleChapterUseCase,
    private val manageBookmarksUseCase: ManageBookmarksUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BibleReaderUiState())
    val uiState: StateFlow<BibleReaderUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<BibleReaderEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<BibleReaderEffect> = _effects.asSharedFlow()

    // In-memory chapter cache preloading current, previous, and next chapters for 0 ms switching
    private val chapterCache = ConcurrentHashMap<Pair<Int, Int>, List<BibleVerse>>()
    private val locationFlow = MutableStateFlow<Pair<Int, Int>?>(null)

    init {
        observeInitialPreferences()
        observeBooksAndBookmarks()
        observeCurrentChapter()
    }

    private fun observeInitialPreferences() {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            _uiState.update { it.copy(preferences = prefs) }
            if (locationFlow.value == null) {
                locationFlow.value = prefs.lastBookId to prefs.lastChapter
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow
                .distinctUntilChanged { old, new ->
                    old.fontSizeSp == new.fontSizeSp &&
                        old.fontWeight == new.fontWeight &&
                        old.fontGrade == new.fontGrade &&
                        old.themeMode == new.themeMode
                }
                .collect { prefs ->
                    _uiState.update { it.copy(preferences = prefs) }
                }
        }
    }

    private fun observeBooksAndBookmarks() {
        viewModelScope.launch {
            combine(
                getBibleBooksUseCase(),
                manageBookmarksUseCase.observeAll()
            ) { books, bookmarks ->
                val bookmarkedVerseIds = bookmarks
                    .filter { it.itemType == BookmarkType.VERSE }
                    .map { it.referenceId }
                    .toSet()
                books to bookmarkedVerseIds
            }.collect { (books, bookmarkedIds) ->
                val loc = locationFlow.value
                val currentBook = loc?.let { (bookId, _) -> books.firstOrNull { it.id == bookId } }
                _uiState.update { state ->
                    state.copy(
                        books = books,
                        currentBook = currentBook ?: state.currentBook,
                        bookmarkedVerseIds = bookmarkedIds,
                        hasPreviousChapter = computeHasPrevious(books, currentBook, state.currentChapter),
                        hasNextChapter = computeHasNext(books, currentBook, state.currentChapter)
                    )
                }
                if (loc != null && books.isNotEmpty()) {
                    preloadAdjacentChapters(books, loc.first, loc.second)
                }
            }
        }
    }

    private fun observeCurrentChapter() {
        viewModelScope.launch {
            locationFlow
                .map { it }
                .distinctUntilChanged()
                .flatMapLatest { loc ->
                    if (loc == null) {
                        kotlinx.coroutines.flow.flowOf(emptyList())
                    } else {
                        val cached = chapterCache[loc]
                        if (cached != null) {
                            _uiState.update { state ->
                                val book = state.books.firstOrNull { it.id == loc.first }
                                state.copy(
                                    isLoading = false,
                                    currentBook = book ?: state.currentBook,
                                    currentChapter = loc.second,
                                    verses = cached,
                                    selectedVerseIds = emptySet(),
                                    hasPreviousChapter = computeHasPrevious(state.books, book, loc.second),
                                    hasNextChapter = computeHasNext(state.books, book, loc.second)
                                )
                            }
                        }
                        getBibleChapterUseCase(loc.first, loc.second)
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { verses ->
                    val loc = locationFlow.value ?: return@collect
                    if (verses.isNotEmpty()) {
                        chapterCache[loc] = verses
                    }
                    _uiState.update { state ->
                        val book = state.books.firstOrNull { it.id == loc.first }
                        state.copy(
                            isLoading = false,
                            currentBook = book ?: state.currentBook,
                            currentChapter = loc.second,
                            verses = verses,
                            hasPreviousChapter = computeHasPrevious(state.books, book, loc.second),
                            hasNextChapter = computeHasNext(state.books, book, loc.second)
                        )
                    }
                    val books = _uiState.value.books
                    if (books.isNotEmpty()) {
                        preloadAdjacentChapters(books, loc.first, loc.second)
                    }
                }
        }
    }

    private fun preloadAdjacentChapters(books: List<BibleBook>, bookId: Int, chapter: Int) {
        val prev = resolvePreviousLocation(books, bookId, chapter)
        val next = resolveNextLocation(books, bookId, chapter)
        listOfNotNull(prev, next).forEach { target ->
            if (!chapterCache.containsKey(target)) {
                viewModelScope.launch(Dispatchers.IO) {
                    val verses = getBibleChapterUseCase(target.first, target.second).first()
                    if (verses.isNotEmpty()) {
                        chapterCache[target] = verses
                    }
                }
            }
        }
    }

    fun onIntent(intent: BibleReaderIntent) {
        when (intent) {
            is BibleReaderIntent.SelectLocation -> {
                val safeChapter = intent.chapter.coerceAtLeast(1)
                _uiState.update {
                    it.copy(
                        highlightedVerseNumber = intent.highlightVerse,
                        isBookPickerVisible = false,
                        selectedVerseIds = emptySet()
                    )
                }
                locationFlow.value = intent.bookId to safeChapter
                viewModelScope.launch {
                    userPreferencesRepository.updateLastReadingPosition(intent.bookId, safeChapter)
                }
                intent.highlightVerse?.let { verseNum ->
                    _effects.tryEmit(BibleReaderEffect.ScrollToVerse(verseNum))
                }
            }

            is BibleReaderIntent.PreviousChapter -> {
                val state = _uiState.value
                val book = state.currentBook ?: return
                val prev = resolvePreviousLocation(state.books, book.id, state.currentChapter) ?: return
                onIntent(BibleReaderIntent.SelectLocation(prev.first, prev.second))
            }

            is BibleReaderIntent.NextChapter -> {
                val state = _uiState.value
                val book = state.currentBook ?: return
                val next = resolveNextLocation(state.books, book.id, state.currentChapter) ?: return
                onIntent(BibleReaderIntent.SelectLocation(next.first, next.second))
            }

            is BibleReaderIntent.ToggleVerseSelection -> {
                _uiState.update { state ->
                    val updated = if (intent.verseId in state.selectedVerseIds) {
                        state.selectedVerseIds - intent.verseId
                    } else {
                        state.selectedVerseIds + intent.verseId
                    }
                    state.copy(selectedVerseIds = updated)
                }
            }

            is BibleReaderIntent.ClearSelection -> {
                _uiState.update { it.copy(selectedVerseIds = emptySet()) }
            }

            is BibleReaderIntent.CopySelectedVerses -> {
                val state = _uiState.value
                val book = state.currentBook ?: return
                val selected = state.verses.filter { it.id in state.selectedVerseIds }
                if (selected.isEmpty()) return
                val header = "${book.name} ${state.currentChapter}:${formatVerseRange(selected)}"
                val body = selected.joinToString("\n") { "${it.verse}. ${it.text}" }
                _effects.tryEmit(BibleReaderEffect.CopyToClipboard("$header (RVR1960)\n$body", header))
                _effects.tryEmit(BibleReaderEffect.ShowMessage("Pasaje copiado: $header"))
                _uiState.update { it.copy(selectedVerseIds = emptySet()) }
            }

            is BibleReaderIntent.BookmarkSelectedVerses -> {
                val state = _uiState.value
                val book = state.currentBook ?: return
                val selected = state.verses.filter { it.id in state.selectedVerseIds }
                if (selected.isEmpty()) return
                viewModelScope.launch {
                    selected.forEach { verse ->
                        manageBookmarksUseCase.save(
                            type = BookmarkType.VERSE,
                            referenceId = verse.id,
                            titlePreview = "${book.name} ${verse.chapter}:${verse.verse}",
                            contentPreview = verse.text,
                            colorHex = intent.colorHex
                        )
                    }
                    _uiState.update { it.copy(selectedVerseIds = emptySet()) }
                    _effects.tryEmit(
                        BibleReaderEffect.ShowMessage(
                            if (selected.size == 1) "Versículo guardado" else "${selected.size} versículos guardados"
                        )
                    )
                }
            }

            is BibleReaderIntent.ToggleBookPicker -> {
                _uiState.update { it.copy(isBookPickerVisible = intent.visible) }
            }

            is BibleReaderIntent.SelectPickerTestament -> {
                _uiState.update { it.copy(pickerTestament = intent.testament) }
            }

            is BibleReaderIntent.ToggleTypographySheet -> {
                _uiState.update { it.copy(isTypographySheetVisible = intent.visible) }
            }

            is BibleReaderIntent.UpdateFontSize -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateTypographySettings(fontSizeSp = intent.sizeSp)
                }
            }

            is BibleReaderIntent.UpdateFontWeight -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateTypographySettings(fontWeight = intent.weight)
                }
            }

            is BibleReaderIntent.UpdateFontGrade -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateTypographySettings(fontGrade = intent.grade)
                }
            }
        }
    }

    private fun resolvePreviousLocation(
        books: List<BibleBook>,
        bookId: Int,
        chapter: Int
    ): Pair<Int, Int>? {
        if (chapter > 1) return bookId to (chapter - 1)
        val idx = books.indexOfFirst { it.id == bookId }
        if (idx > 0) {
            val prevBook = books[idx - 1]
            return prevBook.id to prevBook.chapterCount
        }
        return null
    }

    private fun resolveNextLocation(
        books: List<BibleBook>,
        bookId: Int,
        chapter: Int
    ): Pair<Int, Int>? {
        val idx = books.indexOfFirst { it.id == bookId }
        if (idx < 0) return null
        val currentBook = books[idx]
        if (chapter < currentBook.chapterCount) return bookId to (chapter + 1)
        if (idx + 1 < books.size) {
            val nextBook = books[idx + 1]
            return nextBook.id to 1
        }
        return null
    }

    private fun computeHasPrevious(
        books: List<BibleBook>,
        currentBook: BibleBook?,
        chapter: Int
    ): Boolean {
        if (currentBook == null || books.isEmpty()) return false
        return chapter > 1 || books.first().id != currentBook.id
    }

    private fun computeHasNext(
        books: List<BibleBook>,
        currentBook: BibleBook?,
        chapter: Int
    ): Boolean {
        if (currentBook == null || books.isEmpty()) return false
        return chapter < currentBook.chapterCount || books.last().id != currentBook.id
    }

    private fun formatVerseRange(verses: List<BibleVerse>): String {
        val nums = verses.map { it.verse }.sorted()
        return if (nums.size == 1) nums.first().toString() else "${nums.first()}-${nums.last()}"
    }
}
