package po.test.exposify.setup

import kotlinx.serialization.Serializable

@Serializable
data class ClassData(val key: Int, val value: String)

@Serializable
data class MetaData(val type: Int, val key: String, val value: String)
