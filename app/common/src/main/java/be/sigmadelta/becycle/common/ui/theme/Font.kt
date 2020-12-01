package be.sigmadelta.becycle.common.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.common.R

val montserrat = fontFamily(
    font(R.font.montserrat_regular),
    font(R.font.montserrat_bold, FontWeight.Bold),
    font(R.font.montserrat_light, FontWeight.Light),
    font(R.font.montserrat_medium, FontWeight.Medium),
    font(R.font.montserrat_semibold, FontWeight.SemiBold),
    font(R.font.montserrat_thin, FontWeight.Thin)
)

val splashScreenLogoFontSize = 64.sp
val titleFontSize = 20.sp
val regularFontSize = 16.sp
val subTextFontSize = 12.sp
val minimalFontSize = 10.sp