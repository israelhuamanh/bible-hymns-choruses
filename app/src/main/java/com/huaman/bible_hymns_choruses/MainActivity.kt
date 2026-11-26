package com.huaman.bible_hymns_choruses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huaman.bible_hymns_choruses.core.designsystem.ExpressiveAppTheme
import com.huaman.bible_hymns_choruses.presentation.home.MainAppViewModel
import com.huaman.bible_hymns_choruses.presentation.home.MainScaffold
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: MainAppViewModel = hiltViewModel()
            val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()
            ExpressiveAppTheme(themeMode = themeMode) {
                MainScaffold(appViewModel = appViewModel)
            }
        }
    }
}