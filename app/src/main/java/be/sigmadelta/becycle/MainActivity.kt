package be.sigmadelta.becycle

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
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
import be.sigmadelta.becycle.accesstoken.AccessTokenViewModel
import be.sigmadelta.becycle.address.*
import be.sigmadelta.becycle.collections.CollectionsViewModel
import be.sigmadelta.becycle.common.*
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.widgets.BecycleProgressIndicator
import be.sigmadelta.becycle.common.util.PowerUtil
import be.sigmadelta.becycle.home.Home
import be.sigmadelta.becycle.notification.SettingsNotifications
import be.sigmadelta.becycle.settings.Settings
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.notifications.NotificationRepo
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import be.sigmadelta.common.util.SessionStorage
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
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

    if (preferences.isFirstRun && nav.current != Destination.Settings && nav.current != Destination.SettingsAddressCreation) {
        MaterialDialog(ContextAmbient.current).show {
            cornerRadius(16f)
            title(text = "Battery Optimisations")
            message(text = "Due to Android's aggressive battery optimisations, notification reminders might not work on your device.\n\nWould you like disable the battery optimisations for this app to make sure the reminders are allowed to trigger?")
            icon(R.drawable.ic_notifications_on)
            positiveButton(text = "Disable Battery Optimisations") {
                actions.goTo(Destination.Settings)
                preferences.isFirstRun = false
            }
            negativeButton(text = "Turn off notification reminders") {
                it.dismiss()
                preferences.notificationsEnabled = false
                preferences.isFirstRun = false
            }
            cancelable(false)
            cancelOnTouchOutside(false)
        }
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
                                MaterialDialog(ctx).show {
                                    cornerRadius(16f)
                                    title(text = ("Go to Recycle App website?"))
                                    message(text = "Would you like to visit the Recycle App website for additional information?")
                                    positiveButton(text = "OK") {
                                        actions.goToRecycleWebsite(ctx)
                                    }
                                    negativeButton(text = "Cancel") {
                                        it.dismiss()
                                    }
                                }
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
                onGetDisableBatteryOptimisationInfoClicked = {
                    MaterialDialog(ctx, BottomSheet()).show {
                        cornerRadius(16f)
                        title(text = "Disable Battery Optimisations")
                        message(text =
                        """
                            Android will try to extend its battery life by letting the system go to sleep. This mode is called 'Doze' and might prevent the apps from acting when necessary such as in the case of firing a reminder notification. 
                            
                            To disable this, Becycle needs to be whitelisted by disabling battery optimizations. This will allow the app to send reminders even when the system is in Doze mode.
                        """.trimIndent()
                        )
                        icon(R.drawable.ic_notifications_on)
                        positiveButton(text = "Disable Battery Optimisations") {
                            PowerUtil.checkBattery(ctx)
                            // Can't get proper callback from checkBattery to refresh optimisation warning state,
                            // by going back home, the warning will be refreshed and therefor gone
                            actions.pressOnBack()
                        }
                    }
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
            { actions.goTo(Destination.SettingsAddressCreation) },
            { actions.pressOnBack() }
        )

        Destination.SettingsAddressCreation -> SettingsAddressManipulation(
            zipCodeItemsViewState,
            streetsViewState,
            onSearchZipCode = addressViewModel::searchZipCode,
            onSearchStreet = addressViewModel::searchStreets,
            onValidateAddress = addressViewModel::validateAddress,
            onBackClicked = { actions.pressOnBack() }
        )

        is Destination.SettingsAddressEditRemoval -> {
            SettingsAddressEditRemoval(
                (nav.current as Destination.SettingsAddressEditRemoval).addressId,
                addresses,
                zipCodeItemsViewState,
                streetsViewState,
                addressViewModel::searchZipCode,
                addressViewModel::searchStreets,
                addressViewModel::validateExistingAddress,
                {
                    addressViewModel.removeAddress(it)
                    actions.pressOnBack()
                },
                { actions.pressOnBack() }
            )
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
            BecycleProgressIndicator()
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