package com.huaman.bible_hymns_choruses.data.repository

import com.huaman.bible_hymns_choruses.data.local.dao.NoteDao
import com.huaman.bible_hymns_choruses.data.local.entity.NoteEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> {
        return noteDao.getAllNotes().flowOn(Dispatchers.IO)
    }

    suspend fun getNoteById(id: Int): NoteEntity? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)
    }

    suspend fun saveNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        noteDao.upsertNote(note)
    }

    suspend fun deleteNoteById(id: Int) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }
}
