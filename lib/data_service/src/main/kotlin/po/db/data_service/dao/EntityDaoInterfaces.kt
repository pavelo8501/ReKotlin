package po.db.data_service.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.dto.DataTransferObjectsParent
import po.db.data_service.dto.MarkerInterface
import po.db.data_service.dto.ModelEntityPairContainer


class SysNameKey<DATA_MODEL : MarkerInterface>(val sysName: String)

/*
    Acts as an linking interface for of DTO  and DataBase entities
    Part of the property mapping system
 */
interface EntityDAO<ENTITY, DATA_MODEL>
        where ENTITY: LongEntity, DATA_MODEL : MarkerInterface
{
    var entityDAO : LongEntityClass<ENTITY>


    companion object{
        private val keyedEntityDataModelPairs = mutableMapOf<SysNameKey<*>, Pair<*, *>>()
        private val keyedEntityDataModelContainers = mutableMapOf<String, ModelEntityPairContainer<*,*>>()

        private fun <ENTITY , DATA_MODEL> pair(
            first: LongEntityClass<ENTITY>,
            second: DataTransferObjectsParent<DATA_MODEL>

        ): Pair<LongEntityClass<ENTITY>, DataTransferObjectsParent<DATA_MODEL>> where ENTITY:LongEntity, DATA_MODEL  : MarkerInterface {
            return Pair(first,  second)
        }

        fun <ENTITY : LongEntity, DATA_MODEL : MarkerInterface> pairEntities(
            dao: LongEntityClass<ENTITY>,
            dataTransferObject: DataTransferObjectsParent<DATA_MODEL>
        ) {
            val key = SysNameKey<DATA_MODEL>(dataTransferObject.sysName)
            keyedEntityDataModelPairs[key] = pair(dao, dataTransferObject)
        }

        fun <ENTITY : LongEntity, DATA_MODEL : MarkerInterface>saveContainer(
            key : String,
            dataTransferObject: ModelEntityPairContainer<DATA_MODEL, ENTITY>
        ) {
            keyedEntityDataModelContainers[key] = dataTransferObject
        }

        @Suppress("UNCHECKED_CAST")
        fun <DATA_MODEL : MarkerInterface> getDataModel(sysName: String): DataTransferObjectsParent<DATA_MODEL>? {
            val key = SysNameKey<DATA_MODEL>(sysName)
            return (keyedEntityDataModelPairs[key]?.second as? DataTransferObjectsParent<DATA_MODEL>)
        }
    }

    fun initialize(daoEntity :  LongEntityClass<ENTITY>, dataTransferObject : DataTransferObjectsParent<DATA_MODEL>){
        pairEntities(daoEntity, dataTransferObject)
    }
}

