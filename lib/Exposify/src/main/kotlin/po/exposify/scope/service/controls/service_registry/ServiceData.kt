package po.exposify.scope.service.controls.service_registry

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.classes.interfaces.DataModel

class ServiceDataBuilder<DATA_MODEL, ENTITY>  where DATA_MODEL: DataModel, ENTITY : LongEntity {
    var rootDTOModelData: DTOData<DATA_MODEL, ENTITY>? = null
    val childDTOModels = mutableListOf<ChildDTOData<*, *>>()

    inline fun <reified CHILD_DATA_MODEL, reified CHILD_ENTITY> childDTOModel(
        init: ChildDTODataBuilder<CHILD_DATA_MODEL, CHILD_ENTITY>.() -> Unit
    ) where CHILD_DATA_MODEL : DataModel, CHILD_ENTITY :  LongEntity {
        val builder = ChildDTODataBuilder<CHILD_DATA_MODEL, CHILD_ENTITY>()
        builder.init()
        childDTOModels.add(builder.build())
    }

    fun build(): ServiceData<DATA_MODEL,ENTITY>{
        requireNotNull(rootDTOModelData) { "ServiceData must have a rootDTOModel" }
        return ServiceData(rootDTOModelData!!,childDTOModels)
    }
}