package com.huaman.bible_hymns_choruses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.huaman.bible_hymns_choruses.ui.home.BibleHymnsChorusesHomeScreen
import com.huaman.bible_hymns_choruses.ui.theme.BiblehymnschorusesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiblehymnschorusesTheme {
                BibleHymnsChorusesHomeScreen()
            }
        }
    }
}