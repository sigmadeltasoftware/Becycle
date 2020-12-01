package be.sigmadelta.becycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.VerticalGradient
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.accesstoken.AccessTokenViewModel
import be.sigmadelta.becycle.baseheaders.BaseHeadersViewModel
import be.sigmadelta.becycle.baseheaders.BaseHeadersViewState
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.widgets.BecycleProgressIndicator
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.util.SessionStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalFocus
@ExperimentalCoroutinesApi
class SplashScreenActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val baseHeadersViewModel: BaseHeadersViewModel by viewModel()
    private val accessTokenViewModel: AccessTokenViewModel by viewModel()
    private val sessionStorage: SessionStorage by inject()
    private val prefs: Preferences by inject()

    private var error by mutableStateOf<String?>(null)
    private var showSplashScreen by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BecycleTheme {
                remember { error }
                Crossfade(current = showSplashScreen, animation = tween(durationMillis = 800) ) {
                    SplashScreenLayout(it)
                }
                if (error != null) {
                    error?.let { ErrorLayout(msg = it) }
                }
            }
        }
        showSplashScreen = true
        launch {
            baseHeadersViewModel.baseHeadersViewState.collect { viewState ->
                when (viewState) {
                    is BaseHeadersViewState.Empty -> {
                    }
                    is BaseHeadersViewState.Error -> error = viewState.msg ?: "An error occurred!"
                    is BaseHeadersViewState.Headers -> {
                        sessionStorage.baseHeaders = viewState.headers
                        accessTokenViewModel.getAccessToken()
                    }
                }
            }
        }

        launch {
            accessTokenViewModel.accessTokenViewState.collect { result ->
                when (result) {
                    is ViewState.Success -> {
                        Log.d(TAG, "Received Access token: ${result.payload}")
                        sessionStorage.accessToken = result.payload.accessToken
                        launch(Dispatchers.IO) {
                            delay(1400)
                            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                    is ViewState.Error -> error = result.error?.localizedMessage
                }
            }
        }

        baseHeadersViewModel.getBaseHeaders()

        if (prefs.androidNotificationIconRef == 0) {
            prefs.androidNotificationIconRef = R.drawable.ic_becycle
        }
    }

    companion object {
        const val TAG = "SplashScreenActivity"
    }
}


@Composable
fun SplashScreenLayout(show: Boolean) {
    if (show) {
        Column(
            modifier = Modifier.background(
                VerticalGradient(
                    colors = listOf(
                        Color.White,
                        secondaryAccent
                    ), startY = 0f, endY = 500f
                )
            ).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                style = TextStyle(fontFamily = montserrat),
                text = ContextAmbient.current.getString(R.string.app_name),
                fontSize = splashScreenLogoFontSize,
                fontWeight = FontWeight.Bold,
                color = primaryAccent,
                modifier = Modifier.padding(bottom = 16.dp).padding(horizontal = 16.dp)
            )
            BecycleProgressIndicator(modifier = Modifier.height(180.dp).padding(16.dp))
        }
    }
}

@Composable
fun ErrorLayout(msg: String) {
//    TODO("Make a more aesthetic/informative error")
    Text(
        msg,
        color = errorColor
    )
}
