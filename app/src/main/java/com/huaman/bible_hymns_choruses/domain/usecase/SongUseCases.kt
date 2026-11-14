package com.huaman.bible_hymns_choruses.domain.usecase

import com.huaman.bible_hymns_choruses.data.mapper.toDetailDomain
import com.huaman.bible_hymns_choruses.data.mapper.toDomain
import com.huaman.bible_hymns_choruses.data.mapper.toSummaryDomain
import com.huaman.bible_hymns_choruses.data.repository.SongRepository
import com.huaman.bible_hymns_choruses.domain.model.SongDetail
import com.huaman.bible_hymns_choruses.domain.model.SongSummary
import com.huaman.bible_hymns_choruses.domain.model.SongType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSongsCatalogUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    operator fun invoke(type: SongType): Flow<List<SongSummary>> =
        songRepository.getSongsByType(type.dbValue)
            .map { list -> list.map { it.toDomain() } }
}

class GetSongDetailUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    operator fun invoke(songId: Int): Flow<SongDetail?> =
        songRepository.getSongById(songId).map { it?.toDetailDomain() }

    suspend fun byNumber(type: SongType, number: Int): SongDetail? =
        songRepository.getSongByNumber(type.dbValue, number)?.toDetailDomain()

    suspend fun searchLyricsFts(query: String): List<SongSummary> =
        songRepository.searchSongsFts(query).map { it.toSummaryDomain() }
}
