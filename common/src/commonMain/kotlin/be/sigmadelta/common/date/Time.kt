package be.sigmadelta.common.date

import be.sigmadelta.common.util.addLeadingZeroBelow10
import kotlinx.serialization.Serializable

@Serializable
data class Time(val hours: Int, val mins: Int) {

    val hhmm = "${addLeadingZeroBelow10(hours)}:${addLeadingZeroBelow10(mins)}"

    companion object {

        fun parseHhMm(hhMm: String): Time? = try {
            Time(hhMm.substringBefore(":").toInt(), hhMm.substringAfter(":").toInt())
        } catch (e: NumberFormatException) {
            print(e.message)
            null
        }
    }

    fun hasPassed(referenceTime: Time): Boolean {
        val totalMins = (hours * 60) + mins
        val totalMinsRef = (referenceTime.hours * 60) + referenceTime.mins

        return totalMins >= totalMinsRef
    }
}