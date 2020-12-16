package be.sigmadelta.becycle.calendar

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.secondaryAccent
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.*

val dayList = DayOfWeek.values().map { it.name.substring(0, 3).capitalize(Locale.ROOT) }
val dayWeight = 1 / dayList.size.toFloat()

@Composable
fun Calendar(
    actions: CalendarActions,
    composables: CalendarComposables
) {
    val month = YearMonth.now().minusMonths(3)

    Column {
        CalendarHeader()
        CalendarMonth(month, composables)
    }
}

@Composable
fun CalendarHeader() {

    Card {
        Row(
            modifier = Modifier.fillMaxWidth(1f).padding(vertical = 16.dp),
        ) {
            dayList.forEach {
                Text(
                    it,
                    modifier = Modifier.weight(dayWeight),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CalendarMonth(month: YearMonth, composables: CalendarComposables) {

    val firstDayOffset = month.atDay(1).dayOfWeek.ordinal
    val monthLength = month.lengthOfMonth()
    val lastDayCount = monthLength - if (month.isLeapYear) 29 else 28
    val weekCount =
        monthLength / 7 + if (monthLength % 7 > 0) (firstDayOffset + lastDayCount) / 7 else 0

    for (i in 0..weekCount) {
        CalendarWeek(
            startDayOffSet = firstDayOffset,
            endDayCount = if (i == weekCount) lastDayCount else 7,
            monthWeekNumber = i,
            weekCount = weekCount,
            composables = composables
        )
    }
}

@Composable
fun CalendarWeek(
    startDayOffSet: Int,
    endDayCount: Int,
    monthWeekNumber: Int,
    weekCount: Int,
    composables: CalendarComposables
) {
    Row {
        if (monthWeekNumber == 0) {
            for (i in 0 until startDayOffSet) {
                Box(modifier = Modifier.weight(dayWeight)) {
                    composables.priorMonthCalendarItem()
                }
            }
        }

        val endDay = when (monthWeekNumber) {
            0 -> 7 - startDayOffSet
            weekCount -> endDayCount + startDayOffSet + 1
            else -> 7
        }

        for (i in 1..endDay) {
            val day = if (monthWeekNumber == 0) {
                i.toString()
            } else {
                (i + (7 * monthWeekNumber) - startDayOffSet).toString()
            }

            Box(modifier = Modifier.weight(dayWeight)) {
                composables.calendarItem(day)
            }
        }

        if (monthWeekNumber == weekCount) {
            for (i in 0 until (7 - endDayCount - startDayOffSet - 1)) {
                Box(modifier = Modifier.weight(dayWeight)) {
                    composables.nextMonthCalendarItem()
                }
            }
        }
    }
}

@Composable
fun CalendarDay(text: String, modifier: Modifier) {
    Text(
        text,
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

data class CalendarActions(
    val onCalendarUpdated: (View) -> Unit
)

data class CalendarComposables(
    val calendarItem: @Composable (day: String) -> Unit,
    val priorMonthCalendarItem: @Composable () -> Unit,
    val nextMonthCalendarItem: @Composable () -> Unit = priorMonthCalendarItem
)

@Preview
@Composable
fun previewCalendar() {
    Calendar(
        actions = CalendarActions(
            onCalendarUpdated = {}
        ),
        composables = CalendarComposables(
            calendarItem = { day ->
                CalendarDay(text = day, modifier = Modifier.padding(4.dp).fillMaxWidth())
            },
            priorMonthCalendarItem = {
                IconButton(onClick = {}) {
                    Icon(asset = vectorResource(id = R.drawable.ic_info), tint = secondaryAccent)
                }
            }
        )
    )
}