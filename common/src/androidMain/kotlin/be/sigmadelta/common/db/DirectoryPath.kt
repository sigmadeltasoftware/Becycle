package be.sigmadelta.common.db

import android.content.Context

lateinit var appCtx: Context

actual fun getApplicationFilesDirectoryPath() = appCtx.filesDir.absolutePath
