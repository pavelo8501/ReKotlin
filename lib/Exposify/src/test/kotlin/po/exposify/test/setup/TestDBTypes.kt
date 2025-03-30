package po.exposify.test.setup

import kotlinx.serialization.Serializable

@Serializable
data class TestClassItem(val key: Int, val value: String)

@Serializable
data class TestMetaTag(val type: Int, val key: String, val value: String)