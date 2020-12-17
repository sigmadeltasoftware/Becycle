package be.sigmadelta.becycle.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.*

const val dayWeight = 1 / 7f

@ExperimentalCoroutinesApi
@Composable
fun Calendar(
    widgets: CalendarWidgets,
    monthFlow: StateFlow<YearMonth>
) {
    val month = monthFlow.collectAsState().value

    Column {
        CalendarHeader(month, widgets)
        CalendarMonth(month, widgets)
    }
}

@Composable
fun CalendarHeader(
    month: YearMonth,
    widgets: CalendarWidgets
) {
    Card {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            widgets.header(month)

            Row(
                modifier = Modifier.fillMaxWidth(1f).padding(bottom = 16.dp),
            ) {
                DayOfWeek.values().forEach {
                    Box(
                        modifier = Modifier.weight(dayWeight).align(Alignment.CenterVertically),
                        alignment = Alignment.Center
                    ) {
                        widgets.headerDayItem(it)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarMonth(month: YearMonth, widgets: CalendarWidgets) {

    val firstDayOffset = month.atDay(1).dayOfWeek.ordinal
    val monthLength = month.lengthOfMonth()
    val priorMonthLength = month.minusMonths(1).lengthOfMonth()
    val lastDayCount = (monthLength + firstDayOffset) % 7
    val weekCount = (firstDayOffset + monthLength) / 7

    println(
        """
    month = ${month.month.name}
    firstDayOffset = $firstDayOffset
    monthLength = $monthLength
    lastDayCount = $lastDayCount
    weekCount = $weekCount
    ---------------------
    """.trimIndent()
    )

    for (i in 0..weekCount) {
        CalendarWeek(
            startDayOffSet = firstDayOffset,
            endDayCount = lastDayCount,
            monthWeekNumber = i,
            weekCount = weekCount,
            priorMonthLength = priorMonthLength,
            widgets = widgets
        )
    }
}

@Composable
fun CalendarWeek(
    startDayOffSet: Int,
    endDayCount: Int,
    monthWeekNumber: Int,
    weekCount: Int,
    priorMonthLength: Int,
    widgets: CalendarWidgets
) {
    Row {
        if (monthWeekNumber == 0) {
            for (i in 0 until startDayOffSet) {
                Box(
                    modifier = Modifier.weight(dayWeight).align(Alignment.CenterVertically),
                    alignment = Alignment.Center
                ) {
                    widgets.priorMonthDayItem((priorMonthLength - (startDayOffSet - i - 1)).toString())
                }
            }
        }

        val endDay = when (monthWeekNumber) {
            0 -> 7 - startDayOffSet
            weekCount -> endDayCount
            else -> 7
        }

        for (i in 1..endDay) {
            val day = if (monthWeekNumber == 0) {
                i.toString()
            } else {
                (i + (7 * monthWeekNumber) - startDayOffSet).toString()
            }

            Box(
                modifier = Modifier.weight(dayWeight).align(Alignment.CenterVertically),
                alignment = Alignment.Center
            ) {
                widgets.dayItem(day)
            }
        }

        if (monthWeekNumber == weekCount && endDayCount > 0) {
            for (i in 0 until (7 - endDayCount)) {
                Box(
                    modifier = Modifier.weight(dayWeight).align(Alignment.CenterVertically),
                    alignment = Alignment.Center
                ) {
                    widgets.nextMonthDayItem((i + 1).toString())
                }
            }
        }
    }
}

@Composable
fun CalendarDay(text: String, modifier: Modifier = Modifier.padding(4.dp).fillMaxWidth()) {
    Text(
        text,
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

data class CalendarWidgets(
    val header: @Composable (YearMonth) -> Unit,
    val headerDayItem: @Composable (DayOfWeek) -> Unit,
    val dayItem: @Composable (day: String) -> Unit,
    val priorMonthDayItem: @Composable (day: String) -> Unit,
    val nextMonthDayItem: @Composable (day: String) -> Unit = priorMonthDayItem
)

@ExperimentalCoroutinesApi
@Preview
@Composable
fun previewCalendar() {

    val monthFlow = MutableStateFlow(YearMonth.now())

    Column {
        Calendar(
            monthFlow = monthFlow,
            widgets = CalendarWidgets(
                header = { month ->
                    Text(
                        text = "${month.month.name.toLowerCase().capitalize()} ${month.year}",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                    )
                },
                headerDayItem = { dayOfWeek ->
                    CalendarDay(text = dayOfWeek.name.substring(0, 3).capitalize(Locale.ROOT))
                },
                dayItem = { day ->
                    CalendarDay(text = day)
                },
                priorMonthDayItem = { day ->
                    CalendarDay(
                        text = day,
                        modifier = Modifier.padding(4.dp).fillMaxWidth().drawOpacity(0.4f)
                    )
                }
            )
        )
        Button(onClick = {
            monthFlow.value = monthFlow.value.plusMonths(1)
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = "Increment")
        }

        Button(onClick = {
            monthFlow.value = monthFlow.value.minusMonths(1)
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = "Decrement")
        }
    }
}