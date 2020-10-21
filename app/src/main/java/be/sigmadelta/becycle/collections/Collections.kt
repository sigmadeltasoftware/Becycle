package be.sigmadelta.becycle.collections

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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.bottomNavigationMargin
import be.sigmadelta.becycle.common.ui.theme.primaryBackgroundColor
import be.sigmadelta.becycle.common.ui.theme.textPrimary
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import java.text.SimpleDateFormat

@Composable
fun Collections(collections: List<Collection>) {
    // TODO: Check why bottom padding is necessary for BottomNavigation

    val collectionViewItems = mutableListOf<@Composable () -> Unit>(
        {
            Text(
                text = "Upcoming collections:",
                fontSize = 20.sp,
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, start = 8.dp, bottom = 8.dp)
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
            Icon(
                asset = vectorResource(
                    id = when (collection.collectionType) {
                        else -> R.drawable.ic_home
                    }
                )
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(collection.fraction.name.nl, fontWeight = FontWeight.Bold)
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
