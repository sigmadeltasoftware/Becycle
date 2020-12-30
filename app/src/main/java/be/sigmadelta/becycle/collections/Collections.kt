package be.sigmadelta.becycle.collections

import android.widget.ImageView
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.ui.res.vectorResource
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.iconRef
import be.sigmadelta.becycle.common.util.name
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.CollectionException
import be.sigmadelta.common.collections.recapp.RecAppCollectionDao
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

    CollectionView(collectionOverview, actions)
}

@Composable
fun CollectionView(
    collectionOverview: CollectionOverview,
    actions: CollectionActions
) {
    ScrollableColumn(modifier = Modifier.background(Color.White)) {
        CollectionTitle(
            title = R.string.today.str(),
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        collectionOverview.today?.let { collections ->
            LazyRow {
                items(collections) {
                    CollectionItem(it) {
                        actions.onExceptionInfoClicked(it)
                    }
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
                    CollectionItem(collection = it, true) {
                        actions.onExceptionInfoClicked(it)
                    }
                }
            }
        } ?: NoCollectionsSubtitle()

        CollectionTitle(title = R.string.collections__upcoming.str())
        collectionOverview.upcoming?.let {
            it.forEach { collection ->
                UpcomingCollectionItem(collection = collection) {
                    actions.onExceptionInfoClicked(it)
                }
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
fun CollectionItem(
    collection: Collection,
    isTomorrow: Boolean = false,
    onExceptionInfoClicked: (CollectionException) -> Unit
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.width(180.dp).padding(6.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = if (isTomorrow) secondaryAccent else unselectedBackgroundColor
    ) {
        Box {
            collection.exception?.let {
                CollectionExceptionInfoButton(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) { onExceptionInfoClicked(it) }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp).align(Alignment.Center)
            ) {
                Text(
                    collection.type.name(AmbientContext.current),
                    fontWeight = FontWeight.Bold,
                    fontSize = regularFontSize,
                    color = if (isTomorrow) primaryAccent else textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AndroidView(viewBlock = {
                    ImageView(it).apply {
                        setImageDrawable(it.getDrawable(collection.type.iconRef()))
                    }
                }, modifier = Modifier.width(48.dp),
                    update = {
                        it.setImageDrawable(it.context.getDrawable(collection.type.iconRef()))
                    }
                )
            }
        }
    }
}

@Composable
fun UpcomingCollectionItem(
    collection: Collection,
    onExceptionInfoClicked: (CollectionException) -> Unit
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            AndroidView(viewBlock = {
                ImageView(it).apply {
                    setImageDrawable(it.getDrawable(collection.type.iconRef()))
                }
            }, modifier = Modifier.width(36.dp),
                update = {
                    it.setImageDrawable(it.context.getDrawable(collection.type.iconRef()))
                }
            )
            Text(
                collection.type.name(AmbientContext.current),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp),
                fontSize = regularFontSize
            )
            collection.exception?.let {
                CollectionExceptionInfoButton { onExceptionInfoClicked(it) }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${collection.date.dayOfMonth}/${collection.date.monthNumber}",
                fontSize = subTextFontSize,
                color = textSecondary
            )
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
             + "${address.street} ${address.houseNumber}.",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun CollectionExceptionInfoButton(
    modifier: Modifier = Modifier,
    onExceptionInfoClicked: () -> Unit
) {
    IconButton(onClick = { onExceptionInfoClicked() }, modifier = modifier) {
        Icon(imageVector = vectorResource(id = R.drawable.ic_info),
            tint = primaryAccent,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

data class CollectionActions(
    val onSwipeToRefresh: () -> Unit,
    val onExceptionInfoClicked: (CollectionException) -> Unit
)