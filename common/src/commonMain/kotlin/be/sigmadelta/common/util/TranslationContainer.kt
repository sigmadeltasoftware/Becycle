package be.sigmadelta.common.util

import kotlinx.serialization.Serializable

@Serializable
data class TranslationContainer (
    val nl: String,
    val fr: String,
    val de: String,
    val en: String,
)