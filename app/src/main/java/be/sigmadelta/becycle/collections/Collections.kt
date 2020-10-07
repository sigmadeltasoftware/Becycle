package be.sigmadelta.becycle.collections

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.common.collections.Collection
import java.text.SimpleDateFormat

@Composable
fun Collections(collections: List<Collection>) {
    Text(text = "Collections:")
    LazyColumnFor(items = collections.sortedBy { it.timestamp }) {
        collectionItem(collection = it)
    }
}

private val fullFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
private val compactFormat = SimpleDateFormat("dd-MM-yyyy")

@Composable
fun collectionItem(collection: Collection) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp).fillMaxWidth( )) {
        Text(collection.fraction.name.nl, fontWeight = FontWeight.Bold)
        Text("CollectionId: ${collection.id}\nAddressId: ${collection.addressId}", fontSize = 10.sp)
        val timeStamp = fullFormat.parse(collection.timestamp.substringBefore('.')).time
        Text(compactFormat.format(timeStamp), fontSize = 10.sp)
    }
}