package com.example.xrexp.ui

data class ExpActivityInfo(
    val activityClass: Class<*>,
    val description: String,
    val isFullSpace: Boolean = false, // by default application starts in Home space, check AndroidManifest property
    val permissionsToRequest : Array<String> = emptyArray<String>()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExpActivityInfo

        if (isFullSpace != other.isFullSpace) return false
        if (activityClass != other.activityClass) return false
        if (description != other.description) return false
        if (!permissionsToRequest.contentEquals(other.permissionsToRequest)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isFullSpace.hashCode()
        result = 17 * 19 * 29 * 31 * result + activityClass.hashCode()
        result = 17 * 19 * 29 * 31 * result + description.hashCode()
        result = 17 * 19 * 29 * 31 * result + permissionsToRequest.contentHashCode()
        return result
    }
}