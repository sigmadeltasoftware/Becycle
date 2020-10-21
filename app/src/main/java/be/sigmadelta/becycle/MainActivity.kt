package be.sigmadelta.becycle

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import be.sigmadelta.becycle.accesstoken.AccessTokenViewModel
import be.sigmadelta.becycle.address.*
import be.sigmadelta.becycle.collections.CollectionsViewModel
import be.sigmadelta.becycle.common.*
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.util.PowerUtil
import be.sigmadelta.becycle.home.Home
import be.sigmadelta.becycle.notification.SettingsNotifications
import be.sigmadelta.becycle.settings.Settings
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.notifications.NotificationRepo
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import be.sigmadelta.common.util.SessionStorage
import com.judemanutd.autostarter.AutoStartPermissionHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalFocus
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val accessTokenViewModel: AccessTokenViewModel by viewModel()
    private val sessionStorage: SessionStorage by inject()
    private val preferences: Preferences by inject()
    private val notificationRepo: NotificationRepo by inject()
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

        notificationRepo.scheduleWorker()
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

    if (preferences.isFirstRun && nav.current != Destination.Settings) {
        AlertDialog.Builder(ContextAmbient.current)
            .setTitle("Battery Optimisations")
            .setMessage("Due to aggressive battery optimisations, notifications might not work on your device. Would you like disable the optimisations for this app?")
            .setPositiveButton("Disable Battery Optimisations") { _, _ ->
                actions.goTo(Destination.Settings)
                preferences.isFirstRun = false
            }
            .setPositiveButtonIcon(ContextCompat.getDrawable(ContextAmbient.current, R.drawable.ic_notifications_on))
            .setNegativeButton("No, I don't need reminders") { p0, _ ->
                p0.dismiss()
                preferences.notificationsEnabled = false
                preferences.isFirstRun = false
            }
            .setNegativeButtonIcon(ContextCompat.getDrawable(ContextAmbient.current, R.drawable.ic_notifications_off))
            .show()
    }

    Providers(BackDispatcherAmbient provides backPressedDispatcher) {
        ProvideDisplayInsets {
            Scaffold(
                bodyContent = { _ ->
                    Main(
                        nav,
                        actions,
                        preferences,
                        addressViewModel,
                        collectionsViewModel
                    )
                },
                bottomBar = {
                    val ctx = ContextAmbient.current
                    BottomNavigation(
                        backgroundColor = primaryBackgroundColor,
                        elevation = 8.dp,
                    ) {
                        BottomNavigationItem(
                            icon = { Icon(asset = vectorResource(id = R.drawable.ic_home)) },
                            selectedContentColor = primaryAccent,
                            unselectedContentColor = unselectedColor,
                            selected = nav.current == Destination.Home,
                            onClick = { actions.goTo(Destination.Home) })
                        BottomNavigationItem(
                            icon = { Icon(asset = vectorResource(id = R.drawable.ic_settings)) },
                            selectedContentColor = primaryAccent,
                            unselectedContentColor = unselectedColor,
                            selected = nav.current.toString().contains("Settings"), // TODO?
                            onClick = { actions.goTo(Destination.Settings) })
                        BottomNavigationItem(
                            icon = { Icon(asset = vectorResource(id = R.drawable.ic_web)) },
                            selectedContentColor = primaryAccent,
                            unselectedContentColor = unselectedColor,
                            selected = false,
                            onClick = {
                                AlertDialog.Builder(ctx)
                                    .setTitle("Go to Recycle App website?")
                                    .setPositiveButton("OK") { _, _ ->
                                        actions.goToRecycleWebsite(
                                            ctx
                                        )
                                    }
                                    .setNegativeButton("Close") { p0, _ -> p0.dismiss() }
                                    .show()
                            }
                        )
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

    when (nav.current) {
        Destination.Home -> Home(
            addresses,
            collections,
            { actions.goTo(Destination.SettingsAddressCreation) },
            { address -> collectionsViewModel.searchCollections(address) }
        )

        Destination.Settings -> {
            val ctx = ContextAmbient.current
            val autoStarter = AutoStartPermissionHelper.getInstance()
            var notificationSwitchState by remember { mutableStateOf(preferences.notificationsEnabled) }

            Settings(
                actions.goTo,
                notificationSwitchState,
                PowerUtil.isIgnoringBatteryOptimizations(ctx).not() && notificationSwitchState,
                onDisableBatteryOptimisationClicked = {
                    PowerUtil.checkBattery(ctx)
                    // Can't get proper callback from checkBattery to refresh optimisation warning state,
                    // by going back home, the warning will be refreshed and therefor gone
                    actions.pressOnBack()
                },
                autoStarter.isAutoStartPermissionAvailable(ctx),
                { autoStarter.getAutoStartPermission(ctx) },
                onSigmaDeltaLogoClicked = {
                    actions.goToSigmaDeltaWebsite(ctx)
                }) {
                preferences.notificationsEnabled = it
                notificationSwitchState = it
            }
        }


        Destination.SettingsNotifications -> SettingsNotifications(addresses) {
            actions.goTo(Destination.SettingsAddressCreation)
        }


        Destination.SettingsAddresses -> SettingsAddressOverview(
            addresses,
            { actions.goTo(Destination.SettingsAddressEditRemoval(it.id)) },
            { actions.goTo(Destination.SettingsAddressCreation) }
        )

        Destination.SettingsAddressCreation -> AddressCreation(
            zipCodeItemsViewState,
            streetsViewState,
            onSearchZipCode = addressViewModel::searchZipCode,
            onSearchStreet = addressViewModel::searchStreets,
            onValidateAddress = addressViewModel::validateAddress
        )

        is Destination.SettingsAddressEditRemoval -> {
            SettingsAddressEditRemoval(
                (nav.current as Destination.SettingsAddressEditRemoval).addressId,
                addresses,
                zipCodeItemsViewState,
                streetsViewState,
                addressViewModel::searchZipCode,
                addressViewModel::searchStreets,
                addressViewModel::validateExistingAddress
            ) {
                addressViewModel.removeAddress(it)
                actions.pressOnBack()
            }
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
    when (validationViewState) {
        ValidationViewState.Empty -> Unit

        ValidationViewState.Loading -> Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }

        is ValidationViewState.Success -> {
            Snackbar(backgroundColor = primaryAccent) {
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
        ValidationViewState.InvalidCombination -> Snackbar(backgroundColor = errorColor) {
            Text(
                text = "Something went wrong, invalid Address combination. Please reselect your zipcode and street, and try again.",
                color = Color.White
            )
        }
        ValidationViewState.NetworkError -> Snackbar(backgroundColor = errorColor) {
            Text(
                text = "Something went wrong, bad network response. Please check your connection or try again later.",
                color = Color.White
            )
        }
        ValidationViewState.InvalidAddressSpecified -> Snackbar(backgroundColor = errorColor) {
            Text(
                text = "Invalid address specified. Please check your house number and try again",
                color = Color.White
            )
        }
    }

    MainScope().launch {
        delay(4000)
        addressViewModel.resetValidation()
    }
}

private fun resetViewStates(addressViewModel: AddressViewModel) = addressViewModel.resetAll()

@ExperimentalCoroutinesApi
suspend fun <T> StateFlow<ListViewState<T>>.observeForAutKeyErrors(getAccessToken: () -> Unit) =
    collect {
        if (it is ListViewState.Error && it.error is AuthorizationKeyExpiredException) {
            getAccessToken()
        }
    }