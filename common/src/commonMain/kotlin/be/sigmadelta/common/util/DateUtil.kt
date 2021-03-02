package be.sigmadelta.common.util

import be.sigmadelta.common.date.Time
import kotlinx.datetime.*

fun addLeadingZeroBelow10(value: Int) = if (value < 10) "0$value" else value

fun LocalDateTime.toYyyyMm() = "${year}-${addLeadingZeroBelow10(monthNumber)}"

fun LocalDateTime.toYyyyMmDd() =
    "${toYyyyMm()}-${addLeadingZeroBelow10(dayOfMonth)}"

fun LocalDateTime.isToday(): Boolean {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return (now.dayOfYear == dayOfYear && now.year == year)
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

fun String.parseYyyyMmDdToLocalDateTime(): LocalDateTime {
    val yyyy = substringBefore('-').toInt()
    val mm = substringAfter('-').substringBefore('-').toInt()
    val dd = substringAfter('-').substringAfter('-').substringBefore('T').toInt()
    return LocalDateTime(yyyy, mm, dd, 1, 1)
}