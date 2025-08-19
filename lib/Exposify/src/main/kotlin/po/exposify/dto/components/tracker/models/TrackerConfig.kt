package po.exposify.dto.components.tracker.models

class TrackerConfig<K: Enum<K>>(

    var optionalTag:Enum<*>? = null,
    var aliasName: String? = null,
    var observeProperties: Boolean = false,
    var observeRelationBindings: Boolean = false,
    var trackerTag: TrackerTag = TrackerTag.None
){

    fun <K: Enum<K>>  setTag(tag:K){
        optionalTag = tag
    }

}