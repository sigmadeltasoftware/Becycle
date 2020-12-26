package be.sigmadelta.becycle.common.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext

@Composable fun Int.str(): String = AmbientContext.current.getString(this)