package po.exposify.dto.components.tracker.models

class TrackerConfig(
    var aliasName: String? = null,
    var observeProperties: Boolean = false,
    var observeRelationBindings: Boolean = false,
    var trackerTag: TrackerTag = TrackerTag.None
)