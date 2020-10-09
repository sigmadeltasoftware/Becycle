package be.sigmadelta.becycle

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.viewinterop.AndroidView
import be.sigmadelta.becycle.accesstoken.AccessTokenViewModel
import be.sigmadelta.becycle.common.*
import be.sigmadelta.becycle.common.ui.theme.BecycleTheme
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.address.AddressInput
import be.sigmadelta.becycle.address.AddressViewModel
import be.sigmadelta.becycle.address.ValidationViewState
import be.sigmadelta.becycle.collections.CollectionsViewModel
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.home.Home
import be.sigmadelta.becycle.notification.Notifications
import be.sigmadelta.becycle.settings.Settings
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import be.sigmadelta.common.util.SessionStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalFocus
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val accessTokenViewModel: AccessTokenViewModel by viewModel()
    private val sessionStorage: SessionStorage by inject()
    private val preferences: Preferences by inject()
    private val addressViewModel: AddressViewModel by viewModel()
    private val collectionsViewModel: CollectionsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add observers for access token expiration
        launch {
            accessTokenViewModel.accessTokenViewState.collect {
                when (it) {
                    is ViewState.Success -> {
                        sessionStorage.accessToken = it.payload.accessToken
                        Toast.makeText(
                            this@MainActivity,
                            "Access token refreshed, please try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is ViewState.Error -> TODO("Failed to get new access token, try again later")
                }
            }
            addressViewModel.addressesViewState.observeForAutKeyErrors { accessTokenViewModel.getAccessToken() }
            collectionsViewModel.collectionsViewState.observeForAutKeyErrors { accessTokenViewModel.getAccessToken() }
        }

        setContent {
            BecycleTheme {
                MainLayout(
                    addressViewModel,
                    collectionsViewModel,
                    preferences,
                    onBackPressedDispatcher
                )
            }
        }
    }
}

@ExperimentalFocus
@Composable
fun MainLayout(
    addressViewModel: AddressViewModel,
    collectionsViewModel: CollectionsViewModel,
    preferences: Preferences,
    backPressedDispatcher: OnBackPressedDispatcher
) {
    val nav: Navigator<Destination> =
        rememberSavedInstanceState(saver = Navigator.saver(backPressedDispatcher)) {
            Navigator(Destination.Home, backPressedDispatcher)
        }

    val actions = remember(nav) { Actions(nav) }

    Providers(BackDispatcherAmbient provides backPressedDispatcher) {
        ProvideDisplayInsets {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Becycle") })
                },
                bodyContent = { _ -> Main(nav, actions, preferences, addressViewModel, collectionsViewModel) },
                bottomBar = {
                    val ctx = ContextAmbient.current
                    BottomNavigation() {
                        BottomNavigationItem(
                            icon = { Icon(asset = vectorResource(id = R.drawable.ic_home)) },
                            selected = nav.current == Destination.Home,
                            onClick = { actions.goTo(Destination.Home) })
                        BottomNavigationItem(
                            icon = { Icon(asset = vectorResource(id = R.drawable.ic_settings)) },
                            selected = nav.current.toString().contains("Settings"),
                            onClick = { actions.goTo(Destination.Settings) })
                        BottomNavigationItem(
                            icon = { Icon(asset = vectorResource(id = R.drawable.ic_web)) },
                            selected = false,
                            onClick = { actions.goToRecycleWebsite(ctx) })
                    }
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFocus
@Composable
fun Main(
    nav: Navigator<Destination>,
    actions: Actions,
    preferences: Preferences,
    addressViewModel: AddressViewModel,
    collectionsViewModel: CollectionsViewModel
) {

    val addresses by addressViewModel.addressesViewState.collectAsState()
    val collections by collectionsViewModel.collectionsViewState.collectAsState()
    val zipCodeItemsViewState by addressViewModel.zipCodeItemsViewState.collectAsState()
    val streetsViewState by addressViewModel.streetsViewState.collectAsState()
    val validation by addressViewModel.validationViewState.collectAsState()

    Crossfade(nav.current, animation = tween(300)) { dest ->
        when (dest) {
            Destination.Home -> Home(
                addresses,
                collections,
                actions.goTo,
                { addressViewModel.clearAddresses() },
                { address -> collectionsViewModel.searchCollections(address,) }
            )

            Destination.AddressInput -> AddressInput(
                zipCodeItemsViewState,
                streetsViewState,
                onSearchZipCode = addressViewModel::searchZipCode,
                onSearchStreet = addressViewModel::searchStreets,
                onValidateAddress = addressViewModel::validateAddress
            )

            Destination.Settings -> {
                val notificationSwitchState = remember { mutableStateOf(preferences.notificationsEnabled) }
                Settings(actions.goTo, notificationSwitchState) {
                    preferences.notificationsEnabled = it
                    notificationSwitchState.value = it
                }
            }

            Destination.SettingsNotifications -> Notifications(addresses)
        }
    }

    ValidationSnackbar(validation, addressViewModel, actions)
}

@Composable
fun ValidationSnackbar(
    validationViewState: ValidationViewState,
    addressViewModel: AddressViewModel,
    actions: Actions
) {
    Crossfade(validationViewState, animation = tween(300)) {
        when (it) {
            ValidationViewState.Empty -> Unit

            ValidationViewState.Loading -> CircularProgressIndicator()

            is ValidationViewState.Success -> {
                Snackbar(backgroundColor = Color(0xFF43A047)) {
                    Text(text = "Address Validated!", color = Color.White)
                }
                MainScope().launch {
                    (validationViewState as? ValidationViewState.Success)?.let { success ->
                        addressViewModel.saveAddress(success.address)
                        actions.goTo(Destination.Home)
                        delay(2000)
                        resetViewStates(addressViewModel)
                    }
                }
            }
            ValidationViewState.InvalidCombination -> Snackbar(backgroundColor = Color.Red) {
                Text(
                    text = "Something went wrong, invalid Address combination. Please reselect your zipcode and street, and try again.",
                    color = Color.White
                )
            }
            ValidationViewState.NetworkError -> Snackbar(backgroundColor = Color.Red) {
                Text(
                    text = "Something went wrong, bad network response. Please check your connection or try again later.",
                    color = Color.White
                )
            }
            ValidationViewState.InvalidAddressSpecified -> Snackbar(backgroundColor = Color.Red) {
                Text(
                    text = "Invalid address specified. Please check your house number and try again",
                    color = Color.White
                )
            }
        }
        MainScope().launch {
            delay(4000)
            addressViewModel.validationViewState.value = ValidationViewState.Empty
        }
    }
}

private fun resetViewStates(addressViewModel: AddressViewModel) = addressViewModel.apply {
    validationViewState.value = ValidationViewState.Empty
    zipCodeItemsViewState.value = ListViewState.Empty()
    streetsViewState.value = ListViewState.Empty()
}

@ExperimentalCoroutinesApi
suspend fun <T> MutableStateFlow<ListViewState<T>>.observeForAutKeyErrors(getAccessToken: () -> Unit) =
    collect {
        if (it is ListViewState.Error && it.error is AuthorizationKeyExpiredException) {
            getAccessToken()
        }
    }