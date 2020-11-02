package be.sigmadelta.common.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun addLeadingZeroBelow10(value: Int) = if (value < 10) "0$value" else value

fun LocalDateTime.toYyyyMmDd() =
    "${year}-${addLeadingZeroBelow10(monthNumber)}-${addLeadingZeroBelow10(dayOfMonth)}"

fun LocalDateTime.isToday(): Boolean {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return (now.dayOfYear == dayOfYear && now.year == year)
}

fun LocalDateTime.isTomorrow(): Boolean {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return (now.dayOfYear + 1 == dayOfYear && now.year == year)
}