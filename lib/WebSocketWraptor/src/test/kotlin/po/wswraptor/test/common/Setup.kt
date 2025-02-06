package po.wswraptor.test.common

import kotlinx.serialization.Serializable

@Serializable
data class Test1(
    val id: Long,
    val name: String
)

@Serializable
data class Test2(
    val id: Long,
    val  age: Int
)

@Serializable
data class Test3(
    val id: Long,
    val  age: Int,
    val name: String,
    val isTest: Boolean
)
