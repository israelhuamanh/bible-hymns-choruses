package com.huaman.bible_hymns_choruses

import com.huaman.bible_hymns_choruses.data.local.entity.NoteEntity
import com.huaman.bible_hymns_choruses.data.mapper.toDomain
import com.huaman.bible_hymns_choruses.data.mapper.toEntity
import com.huaman.bible_hymns_choruses.domain.model.NoteItem
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying multiple recorded voice-note paths and image paths round-trip
 * cleanly through [NoteEntity] <-> [NoteItem] JSON persistence and file descriptors.
 */
class NotesAudioPersistenceTest {

    @Test
    fun multipleRecordedAudioAndImagePaths_roundTripThroughNoteEntityAndDomain() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "apuntes_audio_test")
        tempDir.mkdirs()

        val audio1 = File(tempDir, "Voz_20261126_120001_100.m4a").apply {
            writeBytes(ByteArray(4096) { 0x1A })
        }
        val audio2 = File(tempDir, "Voz_20261126_120015_250.m4a").apply {
            writeBytes(ByteArray(8192) { 0x2B })
        }
        val image1 = File(tempDir, "img_20261126_120030.jpg").apply {
            writeBytes(ByteArray(2048) { 0x3C })
        }

        val originalNote = NoteItem(
            id = 7,
            title = "Estudio de Salmos",
            content = "Apuntes con múltiples grabaciones de voz",
            imagePaths = listOf(image1.absolutePath),
            audioPaths = listOf(audio1.absolutePath, audio2.absolutePath),
            createdAt = 1700000000000L,
            updatedAt = 1700000050000L
        )

        val entity: NoteEntity = originalNote.toEntity()
        val restored: NoteItem = entity.toDomain()

        assertEquals(originalNote.id, restored.id)
        assertEquals(originalNote.title, restored.title)
        assertEquals(originalNote.content, restored.content)
        assertEquals(2, restored.audioPaths.size)
        assertEquals(1, restored.imagePaths.size)

        // Verify every persisted audio path exists on disk and can open a readable FileDescriptor
        restored.audioPaths.forEach { path ->
            val f = File(path)
            assertTrue("Recorded audio file must exist at $path", f.exists() && f.length() > 256L)
            f.inputStream().use { fis ->
                assertTrue("FileDescriptor must be valid for MediaPlayer", fis.fd.valid())
            }
        }

        audio1.delete()
        audio2.delete()
        image1.delete()
        tempDir.delete()
    }
}
