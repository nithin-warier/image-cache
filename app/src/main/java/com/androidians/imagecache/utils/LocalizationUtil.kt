package com.androidians.imagecache.utils

import android.content.Context
import com.androidians.imagecache.data.local.LocalStorage
import java.util.*

class LocalizationUtil {

    fun applyLanguageContext(context: Context): Context {
        return try {
            val localStorage = LocalStorage(context)
            val locale = Locale(localStorage.getUserLanguage())
            val configuration = context.resources.configuration

            Locale.setDefault(locale)
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } catch (exception: Exception) {
            context
        }
    }
}
