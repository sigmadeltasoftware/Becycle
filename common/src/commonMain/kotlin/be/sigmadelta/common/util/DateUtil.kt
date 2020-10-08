package be.sigmadelta.common.util

class DateUtil {
    fun getDaysInMonthByMonthIndex(monthIndex: Int) = when(monthIndex) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> 28 // Leap years can get rekt
    }
}

fun addLeadingZeroBelow10(value: Int) = if (value < 10) "0$value" else value