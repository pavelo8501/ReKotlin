package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KMutableProperty1

enum class UpdateMode  (val value:Int) {
    MODEL_TO_ENTITY(10),
    ENTITY_TO_MODEL(11),

    MODEL_TO_ENTITY_FORCED(20),
    ENTITY_TO_MODEL_FORCED(21);

    companion object {
        fun fromValue(value: Int): UpdateMode {
            UpdateMode.entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return MODEL_TO_ENTITY
        }
    }
}

class PropertyBinding<DATA_MODEL, ENTITY, TYPE>(
    val name : String,
    private val dtoProperty : KMutableProperty1<DATA_MODEL, TYPE>,
    private val entityProperty : KMutableProperty1<ENTITY, TYPE>) where  DATA_MODEL : DataModel, ENTITY : LongEntity {
    fun update(dtoModel: DATA_MODEL, entityModel: ENTITY, mode:UpdateMode): Boolean {
        val dtoValue = dtoProperty.get(dtoModel)
        val entityValue = entityProperty.get(entityModel)
        val valuesDiffer = dtoValue != entityValue
        when(mode){
            UpdateMode.ENTITY_TO_MODEL->{
               if(!valuesDiffer){
                   return false
               }
                dtoProperty.set(dtoModel, entityValue)
                return true
            }
            UpdateMode.MODEL_TO_ENTITY->{
                if(!valuesDiffer){
                    return false
                }
                entityProperty.set(entityModel, dtoValue)
                return true
            }
            UpdateMode.MODEL_TO_ENTITY_FORCED->{
                dtoProperty.set(dtoModel, entityValue)
                return true
            }
            UpdateMode.ENTITY_TO_MODEL_FORCED->{
                entityProperty.set(entityModel, dtoValue)
                return true
            }
        }
    }
}

class DTOPropertyBinder <DATA_MODEL, ENTITY>(
    vararg  props : PropertyBinding<DATA_MODEL, ENTITY, *> = emptyArray() )
        where DATA_MODEL : DataModel, ENTITY : LongEntity {

    var onInitialized : ((DTOPropertyBinder <DATA_MODEL, ENTITY>)-> Unit) ? = null
    private var propertyList = props.toList()

    fun setProperties(properties: List<PropertyBinding<DATA_MODEL, ENTITY, *>>){
        propertyList = properties
        onInitialized?.invoke(this)
    }

    private fun <T> updateProps(
        statement: DTOPropertyBinder <DATA_MODEL, ENTITY>.() -> T
    ): T = statement.invoke(this)

    fun <T> update(binderBody: DTOPropertyBinder <DATA_MODEL, ENTITY>.() -> T): T = updateProps() { binderBody() }

    fun updateProperties(dataModel: DATA_MODEL, entityModel: ENTITY, updateMode: UpdateMode) {
        propertyList.forEach { it.update(dataModel, entityModel, updateMode) }
    }
}
