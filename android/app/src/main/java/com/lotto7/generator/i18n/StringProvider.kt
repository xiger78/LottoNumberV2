package com.lotto7.generator.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalStrings = staticCompositionLocalOf { LocalizedStrings.get(AppLanguage.JA) }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.JA }

@Composable
fun ProvideStrings(language: AppLanguage, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalStrings provides LocalizedStrings.get(language),
        LocalAppLanguage provides language,
        content = content
    )
}

object S {
    val current: Strings
        @Composable
        get() = LocalStrings.current
}
