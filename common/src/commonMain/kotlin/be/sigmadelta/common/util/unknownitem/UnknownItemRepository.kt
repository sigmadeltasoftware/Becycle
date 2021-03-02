package be.sigmadelta.common.util.unknownitem

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UnknownItemRepository(private val unknownItemApi: UnknownItemApi) {

    fun postUnknownCollection(item: UnknownCollectionItem) {

        GlobalScope.launch(Dispatchers.Default) {
            unknownItemApi.logUnknownCollection(item)
        }
    }
}