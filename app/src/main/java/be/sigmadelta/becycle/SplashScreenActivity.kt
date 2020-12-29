package be.sigmadelta.becycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.VerticalGradient
import androidx.compose.ui.platform.AmbientContext
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
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import be.sigmadelta.common.util.SessionStorage
import com.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.UnknownHostException

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
class SplashScreenActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val baseHeadersViewModel: BaseHeadersViewModel by viewModel()
    private val accessTokenViewModel: AccessTokenViewModel by viewModel()
    private val sessionStorage: SessionStorage by inject()
    private val prefs: Preferences by inject()

    private var error by mutableStateOf<Throwable?>(null)
    private var showSplashScreen by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        val errorActions = ErrorActions(
            onAuthKeyExpired = { restart() },
            onNoValidConnection = { restart() }
        )

        setContent {
            BecycleTheme {
                remember { error }
                Column {
                    Crossfade(current = showSplashScreen, animation = tween(durationMillis = 800)) {
                        SplashScreenLayout(it, error, errorActions)
                    }
                }
            }
        }

        showSplashScreen = true
        launch {
            baseHeadersViewModel.baseHeadersViewState.collect { viewState ->
                when (viewState) {
                    is BaseHeadersViewState.Empty -> Unit
                    is BaseHeadersViewState.Error -> error = viewState.error
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
                        Napier.d("Received Access token: ${result.payload}")
                        sessionStorage.accessToken = result.payload.accessToken
                        launch(Dispatchers.IO) {
                            delay(700)
                            startActivity(
                                Intent(
                                    this@SplashScreenActivity,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        }
                    }
                    is ViewState.Error -> error = result.error
                }
            }
        }

        baseHeadersViewModel.getBaseHeaders()

        if (prefs.androidNotificationIconRef == 0) {
            prefs.androidNotificationIconRef = R.drawable.ic_becycle
        }
    }

    private fun restart() {
        startActivity(Intent(this, SplashScreenActivity::class.java))
        finish()
    }
}


@Composable
fun SplashScreenLayout(show: Boolean, error: Throwable?, actions: ErrorActions) {
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
                text = AmbientContext.current.getString(R.string.app_name),
                fontSize = splashScreenLogoFontSize,
                fontWeight = FontWeight.Bold,
                color = primaryAccent,
                modifier = Modifier.padding(bottom = 16.dp).padding(horizontal = 16.dp)
            )
            BecycleProgressIndicator(modifier = Modifier.height(180.dp).padding(16.dp))
            error?.let {
                Napier.e( it.localizedMessage ?: "Error Occurred")
                ErrorLayout(it, actions)
            }
        }
    }
}

@Composable
fun ErrorLayout(error: Throwable, actions: ErrorActions) = when (error) {
    is AuthorizationKeyExpiredException -> actions.onAuthKeyExpired()
    is UnknownHostException -> {
        Text(
            text = R.string.splash_screen__error_check_connection.str(),
            modifier = Modifier.padding(16.dp),
            color = errorColor
        )
        Button(onClick = { actions.onNoValidConnection() }) {
            Text(text = R.string.retry_connecting.str())
        }
    }
    else -> Text(error.localizedMessage, color = errorColor)
}

data class ErrorActions(
    val onAuthKeyExpired: () -> Unit,
    val onNoValidConnection: () -> Unit
)

