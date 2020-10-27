package be.sigmadelta.becycle

import android.content.Intent
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.theme.BecycleTheme
import be.sigmadelta.becycle.accesstoken.AccessTokenViewModel
import be.sigmadelta.becycle.baseheaders.BaseHeadersViewModel
import be.sigmadelta.becycle.baseheaders.BaseHeadersViewState
import be.sigmadelta.becycle.common.ui.theme.errorColor
import be.sigmadelta.becycle.common.ui.widgets.BecycleProgressIndicator
import be.sigmadelta.common.util.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalFocus
@ExperimentalCoroutinesApi
class SplashScreenActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val baseHeadersViewModel: BaseHeadersViewModel by viewModel()
    private val accessTokenViewModel: AccessTokenViewModel by viewModel()
    private val sessionStorage: SessionStorage by inject()

    private var error by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        throw RuntimeException("Test Crash")
        launch {
            baseHeadersViewModel.baseHeadersViewState.collect { viewState ->
                when (viewState) {
                    is BaseHeadersViewState.Empty -> {
                        setContent {
                            BecycleTheme {
                                remember { error }

                                if (error == null) {
                                    SplashScreenLayout()
                                } else {
                                    error?.let { ErrorLayout(msg = it) }
                                }
                            }
                        }
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
                        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                        finish()
                    }
                    is ViewState.Error -> error = result.error?.localizedMessage
                }
            }
        }

        baseHeadersViewModel.getBaseHeaders()
    }

    companion object {
        const val TAG = "SplashScreenActivity"
    }
}


@Composable
fun SplashScreenLayout() {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Becycle")
        BecycleProgressIndicator(modifier = Modifier.height(180.dp).padding(16.dp))
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
