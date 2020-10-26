package be.sigmadelta.becycle.collections

import android.widget.ImageView
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.bottomNavigationMargin
import be.sigmadelta.becycle.common.ui.theme.primaryBackgroundColor
import be.sigmadelta.becycle.common.ui.theme.textPrimary
import be.sigmadelta.becycle.common.ui.util.iconRef
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import java.text.SimpleDateFormat

@Composable
fun Collections(collections: List<Collection>) {
    // TODO: Check why bottom padding is necessary for BottomNavigation

    val collectionViewItems = mutableListOf<@Composable () -> Unit>(
        {
            Text(
                text = "Upcoming Collections",
                fontSize = 20.sp,
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, start = 8.dp, bottom = 16.dp)
            )
        }
    ).apply {
        addAll(collections
            .sortedBy { it.timestamp }
            .map { { CollectionItem(collection = it) } }
        )
    }.apply {
        add {
            Divider(color = primaryBackgroundColor, modifier = Modifier.padding(bottom = 12.dp))
        }
    }

    LazyColumnFor(
        items = collectionViewItems,
        modifier = Modifier.fillMaxSize().padding(bottom = bottomNavigationMargin)
    ) {
        it()
    }
}

private val fullFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
private val compactFormat = SimpleDateFormat("dd-MM-yyyy")

@Composable
fun CollectionItem(collection: Collection) {
    Card(
        elevation = 12.dp,
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            AndroidView(viewBlock = {
                ImageView(it).apply {
                    setImageDrawable(it.getDrawable(collection.collectionType.iconRef()))
                }
            }, modifier = Modifier.width(24.dp),
            update = {
                it.setImageDrawable(it.context.getDrawable(collection.collectionType.iconRef()))
            })
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(collection.fraction.name.nl.capitalize(), fontWeight = FontWeight.Bold)
                Text(
                    "CollectionId: ${collection.id}\nAddressId: ${collection.addressId}",
                    fontSize = 10.sp
                )
                val timeStamp = fullFormat.parse(collection.timestamp.substringBefore('.')).time
                Text(compactFormat.format(timeStamp), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun EmptyCollections(address: Address) {
    Card(
        elevation = 12.dp,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            "No collections available for ${address.street.names.nl} ${address.houseNumber}.",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
    }
}
