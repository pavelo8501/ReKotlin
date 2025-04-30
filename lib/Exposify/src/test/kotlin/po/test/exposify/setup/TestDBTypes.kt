package po.test.exposify.setup

import kotlinx.serialization.Serializable

@Serializable
data class ClassItem(val key: Int, val value: String)

@Serializable
data class MetaTag(val type: Int, val key: String, val value: String)