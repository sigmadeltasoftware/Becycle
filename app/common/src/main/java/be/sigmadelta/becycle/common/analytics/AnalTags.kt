package be.sigmadelta.becycle.common.analytics

class AnalTag {
    companion object {
        // Access token
        const val GET_ACCESS_TOKEN = "get_access_token"

        // Address
        const val SAVE_ADDRESS = "save_address"
        const val LOAD_SAVED_ADDRESSES = "load_saved_addresses"
        const val CLEAR_ALL_ADDRESSES = "clear_all_addresses"
        const val REMOVE_ADDRESS = "remove_address"
        const val SEARCH_ZIP_CODE = "search_zip_code"
        const val SEARCH_STREETS = "search_streets"
        const val VALIDATE_ADDRESS = "validate_address"
        const val VALIDATE_EXISTING_ADDRESS = "validate_existing_address"
        const val RESET_ALL = "reset_all"
        const val RESET_VALIDATION = "reset_validation"
        const val CREATE_DEFAULT_NOTIFICATION_SETTINGS = "create_default_notification_settings"

        // Base Headers
        const val GET_BASE_HEADERS = "get_base_headers"

        // Collections
        const val SEARCH_COLLECTIONS = "search_collections"
        const val REMOVE_COLLECTIONS = "remove_collections"

        // Notifications
        const val LOAD_NOTIFICATION_PROPS = "load_notification_props"
        const val SET_TOMORROW_ALARM_TIME = "set_tomorrow_alarm_time"
        const val SCHEDULE_WORKER = "schedule_worker"
    }
}
