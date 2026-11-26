@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.huaman.bible_hymns_choruses.presentation.notes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huaman.bible_hymns_choruses.domain.model.NoteItem
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NotesScreen(
    onOpenNoteEditor: (noteId: Int) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotesEffect.ShowMessage -> onShowSnackbar(effect.message)
                is NotesEffect.NoteSaved -> Unit
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

            // From M3 (component-tokens.md -> ButtonSmall):
            // ContainerHeight = 40.0.dp, LeadingSpace = 16.0.dp, TrailingSpace = 16.0.dp,
            // IconSize = 20.0.dp, IconLabelSpace = 8.0.dp
            Button(
                onClick = { onOpenNoteEditor(0) },
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
                        text = "Crea apuntes con título, texto, grabaciones de voz e imágenes sin conexión.",
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
                        playingElapsedSeconds = uiState.playingElapsedSeconds,
                        playingDurationSeconds = uiState.playingDurationSeconds,
                        onClick = { onOpenNoteEditor(note.id) },
                        onToggleAudio = { path -> viewModel.onIntent(NotesIntent.TogglePlayAudio(path)) },
                        onPreviewImage = { path -> viewModel.onIntent(NotesIntent.OpenImagePreview(path)) },
                        onDelete = { viewModel.onIntent(NotesIntent.DeleteNote(note.id)) }
                    )
                }
            }
        }
    }

    val previewPath = uiState.previewImagePath
    if (previewPath != null) {
        ProportionalImagePreviewDialog(
            filePath = previewPath,
            onDismiss = { viewModel.onIntent(NotesIntent.OpenImagePreview(null)) }
        )
    }
}

/**
 * Dedicated full-screen window for creating or editing an Apunte (navigated via [AppRoute.NoteEditorRoute]),
 * supporting multiple in-app voice recordings (AAC-LC 16kHz/24kbps) and responsive image attachments.
 */
@Composable
fun NoteEditorScreen(
    noteId: Int,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(noteId) {
        if (noteId == 0) {
            viewModel.onIntent(NotesIntent.PrepareNewNote)
        } else {
            viewModel.onIntent(NotesIntent.LoadNoteForEdit(noteId))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotesEffect.ShowMessage -> onShowSnackbar(effect.message)
                is NotesEffect.NoteSaved -> onNavigateBack()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onIntent(NotesIntent.AttachImageUri(uri.toString()))
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onIntent(NotesIntent.StartAudioRecording)
        } else {
            onShowSnackbar("Permiso de micrófono requerido para grabar notas de voz")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar following M3 Expressive Small App Bar tokens
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (uiState.isRecordingAudio) {
                                viewModel.onIntent(NotesIntent.CancelAudioRecording)
                            }
                            onNavigateBack()
                        },
                        shapes = IconButtonDefaults.shapes(
                            shape = IconButtonDefaults.smallRoundShape,
                            pressedShape = IconButtonDefaults.smallPressedShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }

                    Text(
                        text = if (noteId == 0) "Nuevo apunte" else "Editar apunte",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = { viewModel.onIntent(NotesIntent.SaveNote) },
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Guardar",
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Visual hierarchy: Title field uses titleLargeEmphasized
                OutlinedTextField(
                    value = uiState.draftTitle,
                    onValueChange = { viewModel.onIntent(NotesIntent.UpdateTitle(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLargeEmphasized,
                    label = { Text("Título del apunte") },
                    placeholder = { Text("Ej. Predicación dominical, Estudio bíblico...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                // Body content field uses bodyLarge
                OutlinedTextField(
                    value = uiState.draftContent,
                    onValueChange = { viewModel.onIntent(NotesIntent.UpdateContent(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    label = { Text("Contenido del apunte") },
                    placeholder = { Text("Escribe aquí tus ideas, bosquejos o reflexiones...") },
                    minLines = 6,
                    maxLines = 16,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                // Action Row: In-app Voice Recorder + Image Picker (M3 ButtonSmall tokens: 40dp height, 20dp icon, 8dp space)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isRecordingAudio) {
                        Button(
                            onClick = { viewModel.onIntent(NotesIntent.StopAudioRecording) },
                            shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Detener (${formatDurationSeconds(uiState.recordingElapsedSeconds)})",
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                maxLines = 1
                            )
                        }
                    } else {
                        FilledTonalButton(
                            onClick = {
                                val hasMicPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasMicPerm) {
                                    viewModel.onIntent(NotesIntent.StartAudioRecording)
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Grabar audio",
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                maxLines = 1
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
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
                            text = "Agregar imagen",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            maxLines = 1
                        )
                    }
                }

                // Responsive Images Section
                if (uiState.draftImagePaths.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Imágenes (${uiState.draftImagePaths.size})",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.draftImagePaths, key = { it }) { path ->
                                ResponsiveImageThumbnail(
                                    filePath = path,
                                    onClick = { viewModel.onIntent(NotesIntent.OpenImagePreview(path)) },
                                    onRemove = { viewModel.onIntent(NotesIntent.RemoveImagePath(path)) }
                                )
                            }
                        }
                    }
                }

                // Multiple Voice Recordings Section
                if (uiState.draftAudioPaths.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Grabaciones de voz (${uiState.draftAudioPaths.size})",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uiState.draftAudioPaths.forEachIndexed { index, audioPath ->
                            AudioAttachmentRow(
                                index = index + 1,
                                filePath = audioPath,
                                isPlaying = uiState.playingAudioPath == audioPath,
                                playingElapsedSeconds = uiState.playingElapsedSeconds,
                                playingDurationSeconds = uiState.playingDurationSeconds,
                                onTogglePlay = { viewModel.onIntent(NotesIntent.TogglePlayAudio(audioPath)) },
                                onRemove = { viewModel.onIntent(NotesIntent.RemoveAudioPath(audioPath)) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    val previewPath = uiState.previewImagePath
    if (previewPath != null) {
        ProportionalImagePreviewDialog(
            filePath = previewPath,
            onDismiss = { viewModel.onIntent(NotesIntent.OpenImagePreview(null)) }
        )
    }
}

@Composable
private fun NoteCard(
    note: NoteItem,
    playingAudioPath: String?,
    playingElapsedSeconds: Int,
    playingDurationSeconds: Int,
    onClick: () -> Unit,
    onToggleAudio: (String) -> Unit,
    onPreviewImage: (String) -> Unit,
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
                        ResponsiveImageThumbnail(
                            filePath = path,
                            onClick = { onPreviewImage(path) },
                            onRemove = null
                        )
                    }
                }
            }

            if (note.audioPaths.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    note.audioPaths.forEachIndexed { index, audioPath ->
                        AudioAttachmentRow(
                            index = index + 1,
                            filePath = audioPath,
                            isPlaying = playingAudioPath == audioPath,
                            playingElapsedSeconds = playingElapsedSeconds,
                            playingDurationSeconds = playingDurationSeconds,
                            onTogglePlay = { onToggleAudio(audioPath) },
                            onRemove = null
                        )
                    }
                }
            }
        }
    }
}

/**
 * Responsive image thumbnail proportional to screen width so images never take over the screen,
 * adapting across compact phones, foldables, and tablets.
 */
@Composable
private fun ResponsiveImageThumbnail(
    filePath: String,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?
) {
    val configuration = LocalConfiguration.current
    val thumbWidth: Dp = (configuration.screenWidthDp.dp * 0.34f).coerceIn(112.dp, 180.dp)
    val thumbHeight: Dp = (thumbWidth * 0.78f).coerceIn(88.dp, 140.dp)

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
            .width(thumbWidth)
            .height(thumbHeight)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        val bmp = imageBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = "Ver imagen",
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

/**
 * Proportional responsive image viewer dialog (max 86% screen width, 62% screen height, 28dp M3 dialog radius)
 * so the user can inspect any attached image clearly without occupying the entire window.
 */
@Composable
private fun ProportionalImagePreviewDialog(
    filePath: String,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val maxDialogWidth = (configuration.screenWidthDp.dp * 0.86f).coerceIn(260.dp, 560.dp)
    val maxDialogHeight = (configuration.screenHeightDp.dp * 0.62f).coerceIn(240.dp, 620.dp)

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = filePath) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                BitmapFactory.decodeFile(filePath)?.asImageBitmap()
            }.getOrNull()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = maxDialogWidth)
                .heightIn(max = maxDialogHeight)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                val bmp = imageBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = "Vista previa de imagen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                FilledTonalIconButton(
                    onClick = onDismiss,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallRoundShape,
                        pressedShape = IconButtonDefaults.smallPressedShape
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar vista previa"
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioAttachmentRow(
    index: Int,
    filePath: String,
    isPlaying: Boolean,
    playingElapsedSeconds: Int,
    playingDurationSeconds: Int,
    onTogglePlay: () -> Unit,
    onRemove: (() -> Unit)?
) {
    val file = File(filePath)
    val sizeKb = (file.length() / 1024L).coerceAtLeast(1L)

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
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Detener audio" else "Reproducir audio",
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nota de voz #$index",
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPlaying) {
                        "Reproduciendo ${formatDurationSeconds(playingElapsedSeconds)} / ${formatDurationSeconds(playingDurationSeconds)}"
                    } else {
                        "Toca ▶ para escuchar · $sizeKb KB"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Eliminar grabación",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatDurationSeconds(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
