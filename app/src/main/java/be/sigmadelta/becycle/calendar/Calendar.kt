package be.sigmadelta.becycle.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.address.AddressSwitcher
import be.sigmadelta.becycle.collections.NoCollectionsSubtitle
import be.sigmadelta.becycle.collections.UpcomingCollectionItem
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.calpose.Calpose
import be.sigmadelta.calpose.WEIGHT_7DAY_WEEK
import be.sigmadelta.calpose.model.CalposeActions
import be.sigmadelta.calpose.model.CalposeDate
import be.sigmadelta.calpose.model.CalposeWidgets
import be.sigmadelta.calpose.widgets.DefaultDay
import be.sigmadelta.calpose.widgets.DefaultHeader
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.CollectionException
import be.sigmadelta.common.collections.CollectionOverview
import com.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.*
import java.time.YearMonth

@ExperimentalCoroutinesApi
@Composable
fun CalendarView(
    collectionOverview: ViewState<CollectionOverview>,
    actions: CalendarViewActions
) {

    val monthState = remember { mutableStateOf(YearMonth.now()) }
    val collectionSet = remember { MutableStateFlow<Set<CalposeDate>>(setOf()) }
    var collections: List<Collection> = mutableListOf()
    val collectionDates = collectionSet.collectAsState().value
        .filter { it.month == monthState.value }
        .toSet()
    val selection =  remember { mutableStateOf(Clock.System.now().toCalposeDate().copy(month = monthState.value)) }

    when (collectionOverview) {
        is ViewState.Success -> {
            collections = collectionOverview.payload.allCollections()
                .filter { it.date.toCalposeDate().month == monthState.value }
            collectionSet.value = collectionOverview.payload.toSelectionSet()
        }
        is ViewState.Error -> Napier.e("Error during retrieval of collection: ${collectionOverview.error}")
        else -> Unit
    }

    Column {
        AddressSwitcher(
            onGoToAddressInput = actions.onGoToAddressInput,
            onTabSelected = { ix -> actions.onTabSelected(ix) }
        )

        Calendar(
            month =  monthState,
            selection = selection,
            actions = CalposeActions(
                onClickedPreviousMonth = { monthState.value = monthState.value.minusMonths(1) },
                onClickedNextMonth = { monthState.value = monthState.value.plusMonths(1) },
            ),
            collections = collectionDates
        ) { selection.value = it }

        EventColumn(collections, selection) {
            actions.onExceptionInfoClicked(it)
        }
    }
}

@Composable
fun Calendar(
    month: State<YearMonth>,
    selection: State<CalposeDate>,
    actions: CalposeActions,
    collections: Set<CalposeDate>,
    onSelectedDate: (CalposeDate) -> Unit
) {

    Calpose(
        month = month.value,

        actions = actions,

        widgets = CalposeWidgets(
            header = { month, todayMonth, actions ->
                DefaultHeader(
                    month,
                    todayMonth,
                    actions
                )
            },
            headerDayRow = { headerDayList ->
                Row(
                    modifier = Modifier.fillMaxWidth(1f)
                        .padding(vertical = 8.dp),
                ) {
                    headerDayList.forEach {
                        DefaultDay(
                            text = it.name.first().toString(),
                            modifier = Modifier.weight(WEIGHT_7DAY_WEEK).alpha(.6f),
                            style = TextStyle(color = Color.Gray, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },

            day = { day, today ->
                val isSelected = selection.value == day
                val hasCollection = collections.contains(day)
                val weight = if (hasCollection || isSelected) 1f else WEIGHT_7DAY_WEEK
                val bgColor = when {
                    isSelected -> primaryAccent
                    hasCollection -> secondaryAccent
                    else -> Color.Transparent
                }

                val widget: @Composable () -> Unit = {
                    DefaultDay(
                        text = day.day.toString(),
                        modifier = Modifier.padding(4.dp).weight(weight).fillMaxWidth(),
                        style = TextStyle(
                            color = when {
                                isSelected -> Color.White
                                else -> Color.Black
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }

                Column(
                    modifier = Modifier.weight(WEIGHT_7DAY_WEEK),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Crossfade(current = bgColor) {
                        Box(
                            modifier = Modifier.preferredSize(28.dp).clip(CircleShape)
                                .clickable(onClick = { onSelectedDate(day) }, indication = null)
                                .background(it)
                                .border(BorderStroke(2.dp, if (day == today) primaryAccent else Color.Transparent), CircleShape)
                        ) {
                            widget()
                        }
                    }
                }
            },
            priorMonthDay = { day ->
                DefaultDay(
                    text = day.day.toString(),
                    style = TextStyle(color = unselectedColor),
                    modifier = Modifier.padding(4.dp).fillMaxWidth().weight(WEIGHT_7DAY_WEEK)
                )
            },
            headerContainer = {
                Card {
                    it()
                }
            }
        )
    )
}

@Composable
fun EventColumn(
    collections: List<Collection>,
    selection: State<CalposeDate>,
    onExceptionInfoClicked: (CollectionException) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            "${selection.value.day} ${selection.value.month.month.name} ${selection.value.month.year}",
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = textPrimary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        val currentCollections =
            collections.filter { it.date.toCalposeDate() == selection.value }
        LazyColumn(Modifier.fillMaxWidth()) {
            items(currentCollections) {
                UpcomingCollectionItem(collection = it) {
                    onExceptionInfoClicked(it)
                }
            }
        }
        if (currentCollections.isEmpty()) {
            NoCollectionsSubtitle()
        }
    }
}

data class CalendarViewActions(
    val onGoToAddressInput: () -> Unit,
    val onSearchCollectionsForAddress: (Address) -> Unit,
    val onTabSelected: (Int) -> Unit,
    val onExceptionInfoClicked: (CollectionException) -> Unit
)

private fun LocalDateTime.toCalposeDate(): CalposeDate {
    return CalposeDate(date.dayOfMonth, date.dayOfWeek, YearMonth.of(date.year, date.month))
}

private fun Instant.toCalposeDate(): CalposeDate = toLocalDateTime(TimeZone.currentSystemDefault()).toCalposeDate()

private fun CollectionOverview.allCollections() = mutableListOf<Collection>().apply {
    addAll(today ?: listOf())
    addAll(tomorrow ?: listOf())
    addAll(upcoming ?: listOf())
}

private fun CollectionOverview.toSelectionSet(): Set<CalposeDate> =
    allCollections().map { it.date.toCalposeDate() }.toSet()