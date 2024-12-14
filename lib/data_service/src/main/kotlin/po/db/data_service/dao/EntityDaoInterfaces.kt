package po.db.data_service.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DTOEntityMarker
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.AbstractDTOModel


class SysNameKey<DATA_MODEL : DTOEntityMarker<DATA_MODEL, *>>(val sysName: String)

/*
    Acts as an linking interface for of DTOClasses  and Entities
    Part of the property mapping system
 */
interface EntityDAO<DATA_MODEL, ENTITY>
        where ENTITY: LongEntity, DATA_MODEL : DataModel
{
   // var entityDAO : LongEntityClass<ENTITY>

    var entityDao : EntityDAO<DATA_MODEL, ENTITY>


    companion object{
        private val keyedEntityDataModelPairs = mutableMapOf<SysNameKey<*>, Pair<*, *>>()
        private val keyedEntityDataModelContainers = mutableMapOf<String, ModelEntityPairContainer<*,*>>()

        private fun <ENTITY , DATA_MODEL> pair(
            first: LongEntityClass<ENTITY>,
            second: AbstractDTOModel<DATA_MODEL, ENTITY>

        ): Pair<LongEntityClass<ENTITY>, AbstractDTOModel<DATA_MODEL, ENTITY>> where ENTITY:LongEntity, DATA_MODEL  : DataModel {
            return Pair(first,  second)
        }

//        fun <ENTITY : LongEntity, DATA_MODEL> pairEntities(
//            dao: LongEntityClass<ENTITY>,
//            dataTransferObject: AbstractDTOModel<DATA_MODEL, ENTITY>
//        ) where DATA_MODEL: DataModel<DATA_MODEL> {
//            val key = SysNameKey< DTOEntityMarker<DATA_MODEL, ENTITY>>(dataTransferObject.dataModelClassName)
//            keyedEntityDataModelPairs[key] = pair(dao, dataTransferObject)
//        }

        fun <ENTITY : LongEntity, DATA_MODEL : DataModel>saveContainer(
            key : String,
            dataTransferObject: ModelEntityPairContainer<DATA_MODEL, ENTITY>
        ) {
            keyedEntityDataModelContainers[key] = dataTransferObject
        }

//        @Suppress("UNCHECKED_CAST")
//        fun <DATA_MODEL : DataModel<DATA_MODEL>, ENTITY: LongEntity> getDataModel(sysName: String): AbstractDTOModel<DATA_MODEL, ENTITY>? {
//            val key = SysNameKey<DATA_MODEL>(sysName)
//            return (keyedEntityDataModelPairs[key]?.second as? AbstractDTOModel<DATA_MODEL, ENTITY>)
//        }
    }

//    fun initialize(daoEntity :  LongEntityClass<ENTITY>, dataTransferObject : AbstractDTOModel<DATA_MODEL, ENTITY>){
//        pairEntities(daoEntity, dataTransferObject)
//    }
}

