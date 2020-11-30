package be.sigmadelta.common.util

import be.sigmadelta.common.date.Time
import kotlinx.datetime.*

fun addLeadingZeroBelow10(value: Int) = if (value < 10) "0$value" else value

fun LocalDateTime.toYyyyMmDd() =
    "${year}-${addLeadingZeroBelow10(monthNumber)}-${addLeadingZeroBelow10(dayOfMonth)}"

fun LocalDateTime.isToday(): Boolean {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    println("this.dayOfYear = ${this.dayOfYear} == now.dayOfYear = ${now.dayOfYear}")
    val isToday = (now.dayOfYear == dayOfYear && now.year == year)
    return isToday
}

fun LocalDateTime.isTomorrow(): Boolean {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return (now.dayOfYear + 1 == dayOfYear && now.year == year)
}

fun yesterday() = Instant.fromEpochSeconds(Clock.System.now().epochSeconds - 86400)

// TODO: Look at how 12/24 hour systems are handled by KotlinX.DateTime
fun LocalDateTime.toTime() = Time(this.hour, this.minute)

fun Time.toLocalDateTime(): LocalDateTime {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return LocalDateTime(now.year, now.month, now.dayOfMonth, hours, mins)
}