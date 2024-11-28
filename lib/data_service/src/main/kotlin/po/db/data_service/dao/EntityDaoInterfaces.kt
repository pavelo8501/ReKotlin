package po.db.data_service.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.DTOMarker
import po.db.data_service.dto.ModelEntityPairContainer


class SysNameKey<DATA_MODEL : DTOMarker>(val sysName: String)

/*
    Acts as an linking interface for of DTOClasses  and Entities
    Part of the property mapping system
 */
interface EntityDAO<DATA_MODEL, ENTITY>
        where ENTITY: LongEntity, DATA_MODEL : DTOMarker
{
   // var entityDAO : LongEntityClass<ENTITY>

    var entityDao : EntityDAO<DATA_MODEL, ENTITY>


    companion object{
        private val keyedEntityDataModelPairs = mutableMapOf<SysNameKey<*>, Pair<*, *>>()
        private val keyedEntityDataModelContainers = mutableMapOf<String, ModelEntityPairContainer<*,*>>()

        private fun <ENTITY , DATA_MODEL> pair(
            first: LongEntityClass<ENTITY>,
            second: AbstractDTOModel<DATA_MODEL>

        ): Pair<LongEntityClass<ENTITY>, AbstractDTOModel<DATA_MODEL>> where ENTITY:LongEntity, DATA_MODEL  : DTOMarker {
            return Pair(first,  second)
        }

        fun <ENTITY : LongEntity, DATA_MODEL : DTOMarker> pairEntities(
            dao: LongEntityClass<ENTITY>,
            dataTransferObject: AbstractDTOModel<DATA_MODEL>
        ) {
            val key = SysNameKey<DATA_MODEL>(dataTransferObject.sysName)
            keyedEntityDataModelPairs[key] = pair(dao, dataTransferObject)
        }

        fun <ENTITY : LongEntity, DATA_MODEL : DTOMarker>saveContainer(
            key : String,
            dataTransferObject: ModelEntityPairContainer<DATA_MODEL, ENTITY>
        ) {
            keyedEntityDataModelContainers[key] = dataTransferObject
        }

        @Suppress("UNCHECKED_CAST")
        fun <DATA_MODEL : DTOMarker> getDataModel(sysName: String): AbstractDTOModel<DATA_MODEL>? {
            val key = SysNameKey<DATA_MODEL>(sysName)
            return (keyedEntityDataModelPairs[key]?.second as? AbstractDTOModel<DATA_MODEL>)
        }
    }

    fun initialize(daoEntity :  LongEntityClass<ENTITY>, dataTransferObject : AbstractDTOModel<DATA_MODEL>){
        pairEntities(daoEntity, dataTransferObject)
    }
}

