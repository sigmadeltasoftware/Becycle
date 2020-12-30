package be.sigmadelta.becycle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.becycle.home.Home
import be.sigmadelta.becycle.home.HomeActions
import be.sigmadelta.becycle.notification.NotificationViewModel
import be.sigmadelta.becycle.notification.SettingsNotificationsActions
import be.sigmadelta.becycle.notification.SettingsNotificationsOverview
import be.sigmadelta.becycle.settings.*
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.Faction
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
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
                    onBackPressedDispatcher,
                    this
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
    backPressedDispatcher: OnBackPressedDispatcher,
    act: Activity
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
            title(R.string.battery_optimisations__title)
            message(R.string.battery_optimisations__text)
            icon(R.drawable.ic_notifications_on)
            positiveButton(R.string.battery_optimisations__go_to_settings) {
                actions.goTo(Destination.Settings)
                preferences.isFirstRun = false
            }
            negativeButton(R.string.battery_optimisations__turn_off_reminders) {
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
                        notificationViewModel,
                        act
                    )
                },
                bottomBar = {
                    val ctx = AmbientContext.current
                    if ((AmbientAddress.current as ListViewState.Success).payload.isNotEmpty()) {
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
                                selected = nav.current.toString().contains("Settings"),
                                onClick = { actions.goTo(Destination.Settings) })
                            BottomNavigationItem(
                                icon = { Icon(vectorResource(id = R.drawable.ic_web)) },
                                selectedContentColor = primaryAccent,
                                unselectedContentColor = unselectedColor,
                                selected = false,
                                onClick = {
                                    MaterialDialog(ctx).show {
                                        cornerRadius(16f)
                                        title(R.string.recycle_website__go_to)
                                        message(R.string.recycle_website__more_info)
                                        positiveButton(android.R.string.ok) {
                                            actions.goToRecycleWebsite(ctx)
                                        }
                                        negativeButton(android.R.string.cancel) {
                                            it.dismiss()
                                        }
                                    }
                                }
                            )
                        }
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
    notificationViewModel: NotificationViewModel,
    act: Activity
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
                collectionOverview,
                CalendarViewActions(
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
                        title(R.string.battery_optimisations_disable__title)
                        message(R.string.battery_optimisations_disable__text)
                        icon(R.drawable.ic_notifications_on)
                        positiveButton(R.string.battery_optimisations_disable__title) {
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
                        Intent.createChooser(mailIntent, ctx.getString(R.string.send_feedback)),
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
            SettingsNotificationsOverview(
                notificationProps,
                SettingsNotificationsActions(
                    onGoToAddressInput = { actions.goTo(Destination.SettingsAddressManipulation) },
                    onTomorrowAlarmTimeSelected = notificationViewModel::setTomorrowAlarmTime,
                    onNotificationsInfoClicked = {
                        MaterialDialog(ctx, BottomSheet()).show {
                            cornerRadius(16f)
                            title(R.string.notifications_info__title)
                            message(R.string.notifications_info__text)
                            positiveButton(R.string.more_information) {
                                actions.goToNotificationsDocumentation(ctx)
                            }
                            negativeButton(android.R.string.ok) {
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
                    },
                    onBackClicked = actions.pressOnBack
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
                onHouseNumberValueChanged = addressViewModel::resetValidation,
                onValidateAddress = addressViewModel::validateAddress,
                onAddressRemove = null,
                onBackClicked = { actions.pressOnBack() }
            )
        )

        is Destination.SettingsAddressEditRemoval -> {
            val addr = addresses?.firstOrNull { it.id == (nav.current as Destination.SettingsAddressEditRemoval).addressId }

            when (addr?.faction) {
                Faction.RECAPP -> {
                    SettingsRecAppAddressEditRemoval(
                        addr.id,
                        zipCodeItemsViewState,
                        streetsViewState,
                        SettingsAddressEditRemovalActions(
                            onSearchZipCode = addressViewModel::searchZipCode,
                            onSearchStreet = addressViewModel::searchStreets,
                            onAddressChanged = {
                                val generic = it.asGeneric()
                                addressViewModel.validateExistingRecAppAddress(it)
                                collectionsViewModel.removeCollections(generic)
                                collectionsViewModel.searchCollections(generic)
                            },
                            onAddressRemove = {
                                addressViewModel.removeAddress(it)
                                selectedTabIx = 0
                                collectionsViewModel.removeCollections(it)
                                collectionsViewModel.searchCollections(it)
                                actions.pressOnBack()
                            },
                            onHouseNumberValueChanged = addressViewModel::resetValidation,
                            onBackClicked = { actions.pressOnBack() }
                        )
                    )
                }
                Faction.LIMNET -> Unit // TODO
                null -> Unit
            }

        }
    }

    ValidationSnackbar(validation, addressViewModel) {
        if (preferences.isFirstRun) {
            act.startActivity(Intent(act, SplashScreenActivity::class.java))
            act.finish()
        } else {
            actions.goTo(Destination.Home)
            resetViewStates(addressViewModel)
        }
    }
}

@Composable
fun ValidationSnackbar(
    validationViewState: ValidationViewState,
    addressViewModel: AddressViewModel,
    onValidationSuccesful: () -> Unit
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
                Text(text = R.string.validation__address_validated.str(), color = Color.White)
            }
            MainScope().launch {
                (validationViewState as? ValidationViewState.Success)?.let { success ->
                    addressViewModel.saveAddress(success.address)
                    onValidationSuccesful()
                    delay(1000)
                    resetViewStates(addressViewModel)
                }
            }
        }
        ValidationViewState.InvalidCombination -> ErrorSnackbar(
            R.string.validation__error_invalid_address.str()
        ) {
            addressViewModel.resetValidation()
        }
        ValidationViewState.NetworkError -> ErrorSnackbar(
            R.string.validation__error_bad_network_response.str()
        ) {
            addressViewModel.resetValidation()
        }
        ValidationViewState.InvalidAddressSpecified -> ErrorSnackbar(
            R.string.validation__error_invalid_address_specified.str()
        ) {
            addressViewModel.resetValidation()
        }
        ValidationViewState.DuplicateAddressEntry -> ErrorSnackbar(
            R.string.validation__error_duplicate_address_entry.str()
        ) {
            addressViewModel.resetValidation()
        }
    }
}

@Composable
fun ErrorSnackbar(
    text: String,
    onClose: () -> Unit
) {
    Snackbar(backgroundColor = errorColor) {
        Row {
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier.weight(0.9f)
            )
            IconButton(
                onClick = { onClose() },
                modifier = Modifier.weight(0.1f)
            ) { Icon(imageVector = vectorResource(id = R.drawable.ic_close)) }
        }
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