package po.test.exposify.setup

import kotlinx.serialization.Serializable
import po.exposify.dao.classes.JSONBType

@Serializable
data class ClassData(val key: Int, val value: String){
    companion object : JSONBType<ClassData>({ ClassData.serializer()})
}

@Serializable
data class MetaData(val type: Int, val key: String, val value: String){
    companion object : JSONBType<MetaData>({ MetaData.serializer() })
}