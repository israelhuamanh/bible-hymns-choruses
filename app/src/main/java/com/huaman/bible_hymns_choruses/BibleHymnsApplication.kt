package com.huaman.bible_hymns_choruses

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point configured with [@HiltAndroidApp] for compile-time
 * dependency injection across DataStore, Room/FTS4, UseCases, and MVI ViewModels.
 */
@HiltAndroidApp
class BibleHymnsApplication : Application()
