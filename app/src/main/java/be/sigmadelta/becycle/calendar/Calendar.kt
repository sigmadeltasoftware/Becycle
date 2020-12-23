package be.sigmadelta.becycle.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.address.AddressSwitcher
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.util.AmbientTabIndex
import be.sigmadelta.calpose.Calpose
import be.sigmadelta.calpose.WEIGHT_7DAY_WEEK
import be.sigmadelta.calpose.model.CalposeActions
import be.sigmadelta.calpose.model.CalposeDate
import be.sigmadelta.calpose.model.CalposeWidgets
import be.sigmadelta.calpose.widgets.DefaultDay
import be.sigmadelta.calpose.widgets.DefaultHeader
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.CollectionOverview
import com.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.DayOfWeek
import java.time.YearMonth

@ExperimentalCoroutinesApi
@Composable
fun CalendarView(
    collectionOverview: ViewState<CollectionOverview>,
    addresses: ListViewState<Address>,
    actions: CalendarViewActions
) {

    val collectionSet = MutableStateFlow<Set<CalposeDate>>(setOf())
    val collections = collectionSet.collectAsState().value
    val selection = MutableStateFlow(CalposeDate(0, DayOfWeek.MONDAY, YearMonth.of(1, 1)))
    val monthFlow = MutableStateFlow(YearMonth.now())

    when (collectionOverview) {
        is ViewState.Success -> collectionSet.value = collectionOverview.payload.toSelectionSet()
        is ViewState.Error -> Napier.e("Error during retrieval of collection: ${collectionOverview.error}")
    }

    Column {
        AddressSwitcher(
            onGoToAddressInput = actions.onGoToAddressInput,
            onTabSelected = { ix -> actions.onTabSelected(ix) }
        )

        Calpose(
            month = monthFlow.collectAsState().value,

            actions = CalposeActions(
                onClickedPreviousMonth = { monthFlow.value = monthFlow.value.minusMonths(1) },
                onClickedNextMonth = { monthFlow.value = monthFlow.value.plusMonths(1) },
            ),

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
                    val isSelected = selection.collectAsState().value == day
                    val hasCollection = collections.contains(day)
                    val onSelected = {
                        selection.value = day
                    }
                    val weight = if (hasCollection || isSelected) 1f else WEIGHT_7DAY_WEEK
                    val bgColor = when {
                        isSelected -> primaryAccent
                        day == today -> errorSecondaryColor
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
                                    .clickable(onClick = onSelected, indication = null)
                                    .background(it)
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

        EventColumn(collections)
    }
}

@Composable
fun EventColumn(collections: Set<CalposeDate>) {
    collections.forEach {
        Text(text = "${it.month.month}-${it.day}")
    }
}

data class CalendarViewActions(
    val onGoToAddressInput: () -> Unit,
    val onSearchCollectionsForAddress: (Address) -> Unit,
    val onTabSelected: (Int) -> Unit
)

private fun CollectionOverview.toSelectionSet(): Set<CalposeDate> =
    mutableSetOf<Collection>().apply {
        addAll(today ?: setOf())
        addAll(tomorrow ?: setOf())
        addAll(upcoming ?: setOf())
    }.map {
        val time = it.timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
        CalposeDate(time.dayOfMonth, time.dayOfWeek, YearMonth.of(time.year, time.month))
    }.toSet()