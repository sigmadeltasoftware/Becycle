package be.sigmadelta.becycle.common.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext
import be.sigmadelta.becycle.common.R
import be.sigmadelta.common.collections.CollectionType

@Composable fun Int.str(): String = AmbientContext.current.getString(this)