package po.test.exposify.setup

import kotlinx.serialization.Serializable
import po.exposify.dao.classes.JSONBType

@Serializable
data class ClassItem(val key: Int, val value: String){
    companion object : JSONBType<ClassItem>({ ClassItem.serializer()})
}

@Serializable
data class MetaTag(val type: Int, val key: String, val value: String){
    companion object : JSONBType<MetaTag>({ MetaTag.serializer() })
}