@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.huaman.bible_hymns_choruses.presentation.notes

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huaman.bible_hymns_choruses.domain.model.NoteItem
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NotesScreen(
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onIntent(NotesIntent.AttachImageUri(uri.toString()))
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onIntent(NotesIntent.AttachAudioUri(uri.toString()))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotesEffect.ShowMessage -> onShowSnackbar(effect.message)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Apuntes",
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = { viewModel.onIntent(NotesIntent.OpenNewNote) },
                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Nuevo apunte",
                    style = MaterialTheme.typography.labelLargeEmphasized
                )
            }
        }

        if (uiState.notes.isEmpty() && !uiState.isLoading) {
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
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Aún no tienes apuntes",
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    Text(
                        text = "Crea apuntes de estudio o predicaciones con título, texto, imágenes y audios sin conexión.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.notes,
                    key = { it.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        playingAudioPath = uiState.playingAudioPath,
                        onClick = { viewModel.onIntent(NotesIntent.OpenEditNote(note)) },
                        onToggleAudio = { path -> viewModel.onIntent(NotesIntent.TogglePlayAudio(path)) },
                        onDelete = { viewModel.onIntent(NotesIntent.DeleteNote(note.id)) }
                    )
                }
            }
        }
    }

    if (uiState.isEditorVisible) {
        NoteEditorSheet(
            isEditing = uiState.editingNoteId != 0,
            title = uiState.draftTitle,
            content = uiState.draftContent,
            imagePaths = uiState.draftImagePaths,
            audioPaths = uiState.draftAudioPaths,
            playingAudioPath = uiState.playingAudioPath,
            onUpdateTitle = { viewModel.onIntent(NotesIntent.UpdateTitle(it)) },
            onUpdateContent = { viewModel.onIntent(NotesIntent.UpdateContent(it)) },
            onPickImage = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveImage = { viewModel.onIntent(NotesIntent.RemoveImagePath(it)) },
            onPickAudio = {
                audioPickerLauncher.launch("audio/*")
            },
            onRemoveAudio = { viewModel.onIntent(NotesIntent.RemoveAudioPath(it)) },
            onToggleAudio = { viewModel.onIntent(NotesIntent.TogglePlayAudio(it)) },
            onSave = { viewModel.onIntent(NotesIntent.SaveNote) },
            onDismiss = { viewModel.onIntent(NotesIntent.DismissEditor) }
        )
    }
}

@Composable
private fun NoteCard(
    note: NoteItem,
    playingAudioPath: String?,
    onClick: () -> Unit,
    onToggleAudio: (String) -> Unit,
    onDelete: () -> Unit
) {
    // From M3 (references/tokens.md & SKILL.md):
    // Outer card radius = 20.dp, inner padding = 16.dp -> inner media elements use 12.dp corner step.
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Strong visual hierarchy between Title (titleLargeEmphasized) and Body content (bodyMedium)
                    Text(
                        text = note.title.ifBlank { "Apunte sin título" },
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = if (note.title.isNotBlank()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Eliminar apunte"
                    )
                }
            }

            if (note.imagePaths.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(note.imagePaths, key = { it }) { path ->
                        LocalImageThumbnail(
                            filePath = path,
                            onRemove = null
                        )
                    }
                }
            }

            if (note.audioPaths.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    note.audioPaths.forEach { audioPath ->
                        AudioAttachmentRow(
                            filePath = audioPath,
                            isPlaying = playingAudioPath == audioPath,
                            onTogglePlay = { onToggleAudio(audioPath) },
                            onRemove = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditorSheet(
    isEditing: Boolean,
    title: String,
    content: String,
    imagePaths: List<String>,
    audioPaths: List<String>,
    playingAudioPath: String?,
    onUpdateTitle: (String) -> Unit,
    onUpdateContent: (String) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onPickAudio: () -> Unit,
    onRemoveAudio: (String) -> Unit,
    onToggleAudio: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) "Editar apunte" else "Nuevo apunte",
                style = MaterialTheme.typography.headlineSmallEmphasized
            )

            // Hierarchy: Title field uses titleLargeEmphasized so it stands out from content
            OutlinedTextField(
                value = title,
                onValueChange = onUpdateTitle,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLargeEmphasized,
                label = { Text("Título del apunte") },
                placeholder = { Text("Ej. Predicación dominical, Estudio de Romanos...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )

            // Body content field uses bodyLarge
            OutlinedTextField(
                value = content,
                onValueChange = onUpdateContent,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                label = { Text("Contenido del apunte") },
                placeholder = { Text("Escribe aquí tus ideas, bosquejos o reflexiones...") },
                minLines = 5,
                maxLines = 12,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )

            // Media attachment triggers (Images & Audio) using M3 ButtonSmall tokens (40dp height, 20dp icon, 8dp gap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onPickImage,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Subir imagen",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        maxLines = 1
                    )
                }

                FilledTonalButton(
                    onClick = onPickAudio,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AudioFile,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Subir audio",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        maxLines = 1
                    )
                }
            }

            if (imagePaths.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(imagePaths, key = { it }) { path ->
                        LocalImageThumbnail(
                            filePath = path,
                            onRemove = { onRemoveImage(path) }
                        )
                    }
                }
            }

            if (audioPaths.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    audioPaths.forEach { audioPath ->
                        AudioAttachmentRow(
                            filePath = audioPath,
                            isPlaying = playingAudioPath == audioPath,
                            onTogglePlay = { onToggleAudio(audioPath) },
                            onRemove = { onRemoveAudio(audioPath) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }

                Button(
                    onClick = onSave,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text(
                        text = "Guardar apunte",
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalImageThumbnail(
    filePath: String,
    onRemove: (() -> Unit)?
) {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = filePath) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeFile(filePath, opts)?.asImageBitmap()
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        val bmp = imageBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = "Imagen adjunta",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (onRemove != null) {
            FilledTonalIconButton(
                onClick = onRemove,
                shapes = IconButtonDefaults.shapes(
                    shape = IconButtonDefaults.smallRoundShape,
                    pressedShape = IconButtonDefaults.smallPressedShape
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Quitar imagen",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioAttachmentRow(
    filePath: String,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onRemove: (() -> Unit)?
) {
    val displayName = File(filePath).name.substringAfter("_", File(filePath).name)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalIconButton(
                onClick = onTogglePlay,
                shapes = IconButtonDefaults.shapes(
                    shape = IconButtonDefaults.smallRoundShape,
                    pressedShape = IconButtonDefaults.smallPressedShape
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Detener audio" else "Reproducir audio",
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPlaying) "Reproduciendo..." else "Audio guardado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Quitar audio",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
