package be.sigmadelta.becycle.collections

import android.widget.ImageView
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.iconRef
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.CollectionOverview
import kotlinx.datetime.*

@ExperimentalMaterialApi
@Composable
fun Collections(
    collectionOverview: CollectionOverview,
    actions: CollectionActions
) {
//    val dismissState = rememberDismissState(DismissValue.Default)

//    SwipeToRefresh(state = dismissState, background = {Text(text = "DWAAWD", fontSize = 40.sp)}) {
//
//    }

    CollectionView(collectionOverview)
}

@Composable
fun CollectionView(collectionOverview: CollectionOverview) {
    ScrollableColumn(modifier = Modifier.background(Color.White)) {
        CollectionTitle(
            title = R.string.today.str(),
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        collectionOverview.today?.let { collections ->
            LazyRow {
                items(collections) {
                    CollectionItem(it)
                }
            }
        } ?: NoCollectionsSubtitle()

        CollectionTitle(
            title = R.string.tomorrow.str(),
            date = (Clock.System.now()
                .plus(DateTimePeriod(days = 1), TimeZone.currentSystemDefault())).toLocalDateTime(
                    TimeZone.currentSystemDefault()
                )
        )
        collectionOverview.tomorrow?.let { collections ->
            LazyRow {
                items(collections) {
                    CollectionItem(collection = it, true)
                }
            }
        } ?: NoCollectionsSubtitle()

        CollectionTitle(title = R.string.collections__upcoming.str())
        collectionOverview.upcoming?.let {
            it.forEach { collection ->
                UpcomingCollectionItem(collection = collection)
            }
        } ?: NoCollectionsSubtitle()
    }
}

@Composable
fun CollectionTitle(
    title: String,
    date: LocalDateTime? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = titleFontSize,
            color = textPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, start = 8.dp, bottom = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        date?.let {
            Text(
                text = "${it.dayOfMonth}-${it.monthNumber}-${it.year}",
                fontSize = subTextFontSize,
                color = textSecondary
            )
        }
    }
}

@Composable
fun NoCollectionsSubtitle() {
    Text(
        text = R.string.collections__no_scheduled.str(),
        fontSize = regularFontSize,
        color = textSecondary,
        modifier = Modifier.padding(start = 8.dp),
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun CollectionItem(collection: Collection, isTomorrow: Boolean = false) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.width(180.dp).padding(6.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = if (isTomorrow) secondaryAccent else unselectedBackgroundColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                collection.fraction.name.nl.capitalize(),
                fontWeight = FontWeight.Bold,
                fontSize = regularFontSize,
                color = if (isTomorrow) primaryAccent else textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AndroidView(viewBlock = {
                ImageView(it).apply {
                    setImageDrawable(it.getDrawable(collection.collectionType.iconRef()))
                }
            }, modifier = Modifier.width(48.dp),
                update = {
                    it.setImageDrawable(it.context.getDrawable(collection.collectionType.iconRef()))
                })
        }
    }
}

@Composable
fun UpcomingCollectionItem(collection: Collection) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            AndroidView(viewBlock = {
                ImageView(it).apply {
                    setImageDrawable(it.getDrawable(collection.collectionType.iconRef()))
                }
            }, modifier = Modifier.width(36.dp),
                update = {
                    it.setImageDrawable(it.context.getDrawable(collection.collectionType.iconRef()))
                }
            )
            Text(
                collection.fraction.name.nl.capitalize(),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp),
                fontSize = regularFontSize
            )
            Spacer(modifier = Modifier.weight(1f))
            collection.timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault()).let {
                Text(
                    text = "${it.dayOfMonth}/${it.monthNumber}",
                    fontSize = subTextFontSize,
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyCollections(address: Address) {
    Card(
        elevation = 12.dp,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            R.string.collections__no_available_for.str()
             + "${address.street.names.nl} ${address.houseNumber}.",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
    }
}

data class CollectionActions(
    val onSwipeToRefresh: () -> Unit
)