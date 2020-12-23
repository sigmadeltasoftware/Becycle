package be.sigmadelta.becycle

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import be.sigmadelta.becycle.address.*
import be.sigmadelta.becycle.calendar.CalendarView
import be.sigmadelta.becycle.calendar.CalendarViewActions
import be.sigmadelta.becycle.collections.CollectionsViewModel
import be.sigmadelta.becycle.common.*
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.widgets.BecycleProgressIndicator
import be.sigmadelta.becycle.common.util.AmbientAddress
import be.sigmadelta.becycle.common.util.AmbientTabIndex
import be.sigmadelta.becycle.common.util.PowerUtil
import be.sigmadelta.becycle.home.Home
import be.sigmadelta.becycle.home.HomeActions
import be.sigmadelta.becycle.notification.NotificationViewModel
import be.sigmadelta.becycle.notification.SettingsNotifications
import be.sigmadelta.becycle.notification.SettingsNotificationsActions
import be.sigmadelta.becycle.settings.Settings
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.github.aakira.napier.Napier
import com.judemanutd.autostarter.AutoStartPermissionHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private var selectedTabIx by mutableStateOf(0)

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val preferences: Preferences by inject()
    private val addressViewModel: AddressViewModel by viewModel()
    private val collectionsViewModel: CollectionsViewModel by viewModel()
    private val notificationViewModel: NotificationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add observers for access token expiration
        launch {
            // TODO: Handle this better through a separate viewState in AddressViewModel
            addressViewModel.zipCodeItemsViewState.observeForAutKeyErrors { restartAppForTokenRefresh() }
            addressViewModel.streetsViewState.observeForAutKeyErrors { restartAppForTokenRefresh() }
            addressViewModel.addressesViewState.observeForAutKeyErrors { restartAppForTokenRefresh() }
            collectionsViewModel.collectionsViewState.observeForAuthKeyErrors { restartAppForTokenRefresh() }
        }

        setContent {
            BecycleTheme {
                MainLayout(
                    addressViewModel,
                    collectionsViewModel,
                    notificationViewModel,
                    preferences,
                    onBackPressedDispatcher
                )
            }
        }

        notificationViewModel.loadNotificationProps()
        notificationViewModel.scheduleWorker()
    }

    private fun restartAppForTokenRefresh() {
        startActivity(Intent(this@MainActivity, SplashScreenActivity::class.java))
        finish()
    }
}

@ExperimentalMaterialApi
@Composable
fun MainLayout(
    addressViewModel: AddressViewModel,
    collectionsViewModel: CollectionsViewModel,
    notificationViewModel: NotificationViewModel,
    preferences: Preferences,
    backPressedDispatcher: OnBackPressedDispatcher
) {
    val nav: Navigator<Destination> =
        rememberSavedInstanceState(saver = Navigator.saver(backPressedDispatcher)) {
            Navigator(Destination.Home, backPressedDispatcher)
        }

    val actions = remember(nav) { Actions(nav) }

    if (
        (addressViewModel.addressesViewState.value as? ListViewState.Success)?.payload?.isNotEmpty() == true
        && nav.current != Destination.Settings
        && nav.current != Destination.SettingsAddressManipulation
        && preferences.isFirstRun
    ) {
        MaterialDialog(AmbientContext.current).show {
            cornerRadius(16f)
            title(text = "Battery Optimisations")
            message(text = "Due to Android's aggressive battery optimisations, notification reminders might not work on your device.\n\nWould you like disable the battery optimisations for this app to make sure the reminders are allowed to trigger?")
            icon(R.drawable.ic_notifications_on)
            positiveButton(text = "Go to Settings") {
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

    Providers(
        AmbientBackDispatcher provides backPressedDispatcher,
        AmbientAddress provides addressViewModel.addressesViewState.value,
        AmbientTabIndex provides selectedTabIx
    ) {

        ProvideDisplayInsets {
            Scaffold(
                bodyContent = { _ ->
                    Main(
                        nav,
                        actions,
                        preferences,
                        addressViewModel,
                        collectionsViewModel,
                        notificationViewModel
                    )
                },
                bottomBar = {
                    val ctx = AmbientContext.current
                    BottomNavigation(
                        backgroundColor = primaryBackgroundColor,
                        elevation = 8.dp,
                    ) {
                        BottomNavigationItem(
                            icon = { Icon(vectorResource(id = R.drawable.ic_home)) },
                            selectedContentColor = primaryAccent,
                            unselectedContentColor = unselectedColor,
                            selected = nav.current == Destination.Home,
                            onClick = { actions.goTo(Destination.Home) })
                        BottomNavigationItem(
                            icon = { Icon(vectorResource(id = R.drawable.ic_calendar)) },
                            selectedContentColor = primaryAccent,
                            unselectedContentColor = unselectedColor,
                            selected = nav.current == Destination.Calendar,
                            onClick = { actions.goTo(Destination.Calendar) })
                        BottomNavigationItem(
                            icon = { Icon(vectorResource(id = R.drawable.ic_settings)) },
                            selectedContentColor = primaryAccent,
                            unselectedContentColor = unselectedColor,
                            selected = nav.current.toString().contains("Settings"), // TODO?
                            onClick = { actions.goTo(Destination.Settings) })
                        BottomNavigationItem(
                            icon = { Icon(vectorResource(id = R.drawable.ic_web)) },
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

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Main(
    nav: Navigator<Destination>,
    actions: Actions,
    preferences: Preferences,
    addressViewModel: AddressViewModel,
    collectionsViewModel: CollectionsViewModel,
    notificationViewModel: NotificationViewModel
) {

    val collectionOverview by collectionsViewModel.collectionsViewState.collectAsState()
    val zipCodeItemsViewState by addressViewModel.zipCodeItemsViewState.collectAsState()
    val streetsViewState by addressViewModel.streetsViewState.collectAsState()
    val validation by addressViewModel.validationViewState.collectAsState()
    val notificationProps by notificationViewModel.notificationPropsViewState.collectAsState()

    val addresses = (AmbientAddress.current as? ListViewState.Success)?.payload

    when (nav.current) {
        Destination.Home -> Home(
            collectionOverview,
            HomeActions(
                onGoToAddressInput = { actions.goTo(Destination.SettingsAddressManipulation) },
                onLoadCollections = { address -> collectionsViewModel.searchCollections(address) },
                onTabSelected = { ix ->
                    addresses?.get(ix)?.let {
                        collectionsViewModel.searchCollections(it, true)
                    }
                    selectedTabIx = ix
                }
            )
        )

        Destination.Calendar -> {
            CalendarView(
                collectionOverview, AmbientAddress.current, CalendarViewActions(
                    onGoToAddressInput = {
                        actions.goTo(Destination.SettingsAddressManipulation)
                    },
                    onSearchCollectionsForAddress = collectionsViewModel::searchCollections,
                    onTabSelected = { ix ->
                        addresses?.get(ix)?.let {
                            collectionsViewModel.searchCollections(it, true)
                        }
                        selectedTabIx = ix
                    }
                )
            )
        }

        Destination.Settings -> {
            val ctx = AmbientContext.current
            val autoStarter = AutoStartPermissionHelper.getInstance()
            var notificationSwitchState by remember { mutableStateOf(preferences.notificationsEnabled) }

            Napier.e("triggeredNotifications: ${notificationViewModel.getTriggeredNotificationIds()}")
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
                        message(
                            text =
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
                },
                onSendFeedbackClicked = {
                    val mailIntent = Intent(Intent.ACTION_VIEW)
                    val data =
                        Uri.parse("mailto:?subject=Becycle-Feedback&to=info@sigmadelta.be")
                    mailIntent.data = data
                    ContextCompat.startActivity(
                        ctx,
                        Intent.createChooser(mailIntent, "Send Feedback"),
                        null
                    )
                }
            ) {
                preferences.notificationsEnabled = it
                notificationSwitchState = it
            }
        }

        Destination.SettingsNotifications -> {
            val ctx = AmbientContext.current
            SettingsNotifications(
                notificationProps,
                SettingsNotificationsActions(
                    onGoToAddressInput = { actions.goTo(Destination.SettingsAddressManipulation) },
                    onTomorrowAlarmTimeSelected = notificationViewModel::setTomorrowAlarmTime,
                    onNotificationsInfoClicked = {
                        MaterialDialog(ctx, BottomSheet()).show {
                            cornerRadius(16f)
                            title(text = "Notification Info")
                            message(
                                text =
                                """
                            Due to constraints set by the Android operating system for the power-saving 'Doze' mode,
                            we are only able to check whether a notification should be triggered every 15 minutes.
                            This also implies that there is a 15 minute error margin window for the notification time
                            you have selected. 
                            
                            Should your notification reminder be set to 09:00, the latest you can expect your notification
                            to be triggered is 09:15 (worst case).
                        """.trimIndent()
                            )
                            positiveButton(text = "More information") {
                                actions.goToNotificationsDocumentation(ctx)
                            }
                            negativeButton(text = "OK") {
                                dismiss()
                            }
                        }
                    },
                    onReloadNotificationPropsWhenEmpty = {
                        notificationViewModel.loadNotificationProps()
                    },
                    onTabSelected = { ix ->
                        addresses?.get(ix)?.let {
                            collectionsViewModel.searchCollections(it, true)
                        }
                        selectedTabIx = ix
                    }
                )
            )
        }


        Destination.SettingsAddresses -> SettingsAddressOverview(
            AddressOverviewActions(
                { actions.goTo(Destination.SettingsAddressEditRemoval(it.id)) },
                { actions.goTo(Destination.SettingsAddressManipulation) },
                { actions.pressOnBack() }
            )
        )

        Destination.SettingsAddressManipulation -> SettingsAddressManipulation(
            zipCodeItemsViewState,
            streetsViewState,
            SettingsAddressManipulationActions(
                onSearchZipCode = addressViewModel::searchZipCode,
                onSearchStreet = addressViewModel::searchStreets,
                onValidateAddress = addressViewModel::validateAddress,
                onAddressRemove = null,
                onBackClicked = { actions.pressOnBack() }
            )
        )

        is Destination.SettingsAddressEditRemoval -> {
            SettingsAddressEditRemoval(
                (nav.current as Destination.SettingsAddressEditRemoval).addressId,
                zipCodeItemsViewState,
                streetsViewState,
                SettingsAddressEditRemovalActions(
                    addressViewModel::searchZipCode,
                    addressViewModel::searchStreets,
                    {
                        addressViewModel.validateExistingAddress(it)
                        collectionsViewModel.removeCollections(it)
                        collectionsViewModel.searchCollections(it)
                    },
                    {
                        addressViewModel.removeAddress(it)
                        collectionsViewModel.removeCollections(it)
                        collectionsViewModel.searchCollections(it)
                        actions.pressOnBack()
                    },
                    { actions.pressOnBack() }
                )
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

@ExperimentalCoroutinesApi
suspend fun <T> StateFlow<T>.observeForAuthKeyErrors(getAccessToken: () -> Unit) =
    collect {
        if (it is ViewState.Error<*> && it.error is AuthorizationKeyExpiredException) {
            getAccessToken()
        }
    }