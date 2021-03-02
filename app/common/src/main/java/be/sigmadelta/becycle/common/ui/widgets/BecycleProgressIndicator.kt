package be.sigmadelta.becycle.common.ui.widgets

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import be.sigmadelta.becycle.common.R

@Composable
fun BecycleProgressIndicator(modifier: Modifier = Modifier.padding(8.dp).height(24.dp)) {
    val ctx = AmbientContext.current
    val d = ctx.getDrawable(R.drawable.rotating_recycle_icon) as AnimatedVectorDrawable

    d.registerAnimationCallback(object: Animatable2.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            super.onAnimationEnd(drawable)
            (drawable as? AnimatedVectorDrawable)?.start()
        }
    })
    AndroidView(viewBlock = {
        val img = ImageView(it)
        img.setImageDrawable(d)
        d.start()
        img
    }, modifier = modifier)
    CircularProgressIndicator(color = Color(0x00000000), modifier = Modifier.width(0.dp).height(0.dp))
}