@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.huaman.bible_hymns_choruses.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.huaman.bible_hymns_choruses.R

@Immutable
private data class RealAppFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val REAL_APP_FEATURES = listOf(
    RealAppFeature(
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        title = "Biblia sin conexión (RVR1960, RVC y TLA)",
        description = "Los 66 libros con subtítulos de sección, cambio instantáneo de versión y ajuste de letra y tema."
    ),
    RealAppFeature(
        icon = Icons.Rounded.LibraryMusic,
        title = "Himnos y Coros «Melodías Celestiales»",
        description = "222 himnos y 240 coros con filtro por tonalidad, búsqueda por número o letra y vista de estrofas."
    ),
    RealAppFeature(
        icon = Icons.Rounded.Search,
        title = "Buscador universal y Guardados",
        description = "Busca citas bíblicas («Juan 3:16»), números («104») o palabras clave y guarda tus favoritos."
    )
)

/**
 * Brief, single-step first-run onboarding sheet reusing the project's existing
 * ModalBottomSheet + Surface + M3 Expressive token patterns.
 */
@Composable
fun FirstLaunchOnboardingSheet(
    onComplete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onComplete,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Logo Biblia, Himnos y Coros",
                    modifier = Modifier.size(56.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Biblia, Himnos y Coros",
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Todo disponible 100% sin conexión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                REAL_APP_FEATURES.forEach { feature ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(20.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = feature.title,
                                    style = MaterialTheme.typography.titleMediumEmphasized,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = feature.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onComplete,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Omitir",
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }

                Button(
                    onClick = onComplete,
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MinHeight),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Comenzar",
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
            }
        }
    }
}
