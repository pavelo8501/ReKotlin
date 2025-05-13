package po.exposify.dto.components.property_binder.delegates

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.Any
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty



sealed class ResponsiveDelegate<DTO, D, E, V>(
    protected val dto: CommonDTO<DTO, D, E>,
    protected val dataProperty:KMutableProperty1<D, V>,
    protected val entityProperty:KMutableProperty1<E, V>,
): ReadWriteProperty<DTO, V>
      where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    data class UpdateParams<DTO : ModelDTO, D: DataModel, E: LongEntity, V>(
        val name: String,
        val dto: CommonDTO<DTO, D, E>,
        val oldValue: V?,
        val newValue: V
    )

    private var initName: String? = null
    var name: String
        set(value) {
            if(initName != value){
                initName = value
            }
        }
        get() {
            return if(initName != null){
                initName!!
            }else{
                "Unknown"
            }
        }

    var lastValue : V? = null

    abstract fun update(dataModel:D)
    abstract fun update(container: EntityUpdateContainer<E, *,*,*>)

    protected fun onValueSet(value : V){
        if(lastValue != value){
            valueUpdated = true
            onValueChanged?.invoke(UpdateParams(name, dto, lastValue, value))
            lastValue = value
        }
    }

    override fun getValue(thisRef: DTO, property: KProperty<*>): V {
        name = property.name
        return dataProperty.get(dto.dataModel)
    }

    var valueUpdated : Boolean = false
    var onValueChanged: ((UpdateParams<DTO, D, E, V>)-> Unit)? = null
    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) {
        name = property.name
        onValueSet(value)
    }
}


class SerializedDelegate<DTO, D, E, S, V>(
    dto: CommonDTO<DTO, D, E>,
    dataProperty:KMutableProperty1<D, V>,
    entityProperty:KMutableProperty1<E, V>,
    val serializer:  KSerializer<S>,
) : ResponsiveDelegate<DTO, D, E, V>(dto, dataProperty, entityProperty)
        where DTO: ModelDTO, D: DataModel, E: LongEntity, S: Any
{

    override fun update(dataModel: D) {
        val value = dataProperty.get(dataModel)
        onValueSet(value)
    }

    override fun update(container: EntityUpdateContainer<E, *, *, *>) {
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
            val value =  dataProperty.get(dto.dataModel)
            entityProperty.set(container.ownEntity, value)
            onValueSet(value)
        }else{
            val value = entityProperty.get(container.ownEntity)
            dataProperty.set(dto.dataModel, value)
            onValueSet(value)
        }
    }
}

class PropertyDelegate<DTO, D, E, V>(
    dto: CommonDTO<DTO, D, E>,
    dataProperty:KMutableProperty1<D, V>,
    entityProperty :KMutableProperty1<E, V>
): ResponsiveDelegate <DTO, D, E, V>(dto, dataProperty, entityProperty)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    override fun update(dataModel:D){
        val value = dataProperty.get(dataModel)
        onValueSet(value)
    }

    override fun update(container: EntityUpdateContainer<E, *,*,*>){
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
            val value =  dataProperty.get(dto.dataModel)
            entityProperty.set(container.ownEntity, value)
            onValueSet(value)
        }else{
            val value = entityProperty.get(container.ownEntity)
            dataProperty.set(dto.dataModel, value)
            onValueSet(value)
        }
    }
}

