package be.sigmadelta.common.util

import kotlinx.datetime.LocalDateTime

fun addLeadingZeroBelow10(value: Int) = if (value < 10) "0$value" else value

fun LocalDateTime.toYyyyMmDd() =
    "${year}-${addLeadingZeroBelow10(monthNumber)}-${addLeadingZeroBelow10(dayOfMonth)}"