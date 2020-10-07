package be.sigmadelta.becycle.settings

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Settings() {
    Column(verticalArrangement = Arrangement.Bottom) {
        Text(text = "Sigma Delta", modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}