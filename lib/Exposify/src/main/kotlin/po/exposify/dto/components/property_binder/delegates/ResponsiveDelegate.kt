package po.exposify.dto.components.property_binder.delegates

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.ObservableData
import po.exposify.dto.components.property_binder.interfaces.UpdateParams
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import kotlin.Any
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

sealed class ResponsiveDelegate<DTO, D, E, V: Any> protected constructor(
    protected val dtoProvider: () -> CommonDTO<DTO, D, E>,
    protected val dataProperty:KMutableProperty1<D, V>,
    protected val entityProperty:KMutableProperty1<E, V>,
): ReadWriteProperty<DTO, V>, IdentifiableComponent
      where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    val thisDto : CommonDTO<DTO, D, E> by lazy {
       val dto =  dtoProvider()
       if(dto.tracker.config.observeProperties){
           subscribeUpdates(dto.tracker::propertyUpdated)
       }
       dto
    }

    abstract override val qualifiedName: String
    override val type: ComponentType = ComponentType.ResponsiveDelegate

    private var propertyNameParameter : String = dataProperty.name
    protected val propertyName : String
        get() = propertyNameParameter


    var lastValue : V? = null

    var valueUpdated : Boolean = false
    protected var onValueChanged: ((ObservableData)-> Unit)? = null
    fun subscribeUpdates(valueChanged: (ObservableData)-> Unit){
        onValueChanged = valueChanged
    }

    abstract fun update(dataModel:D)
    abstract fun update(container: EntityUpdateContainer<E, *,*,*>)

    protected fun onValueSet(methodName: String,  value : V){
        if(lastValue != value){
            valueUpdated = true
            onValueChanged?.invoke(UpdateParams(thisDto, CrudOperation.Initialize,  methodName, propertyName, lastValue, value, this))
            lastValue = value
        }
    }

    override fun getValue(thisRef: DTO, property: KProperty<*>): V {
        propertyNameParameter = property.name
        return dataProperty.get(thisDto.dataModel)
    }

    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) {
        propertyNameParameter = property.name
        onValueSet("setValue", value)
    }
}


class SerializedDelegate<DTO, D, E, S, V: Any> internal constructor(
    dtoProvider: () -> CommonDTO<DTO, D, E>,
    dataProperty:KMutableProperty1<D, V>,
    entityProperty:KMutableProperty1<E, V>,
    val serializer:  KSerializer<S>,
) : ResponsiveDelegate<DTO, D, E, V>(dtoProvider, dataProperty, entityProperty)
        where DTO: ModelDTO, D: DataModel, E: LongEntity, S: Any
{

    override val qualifiedName: String
        get() = "SerializedDelegate[${thisDto.dtoName}]"

    override fun update(dataModel: D) {
        val value = dataProperty.get(dataModel)
        onValueSet("update", value)
    }

    override fun update(container: EntityUpdateContainer<E, *, *, *>) {
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
            val value =  dataProperty.get(thisDto.dataModel)
            entityProperty.set(container.ownEntity, value)
            onValueSet("update", value)
        }else{
            val value = entityProperty.get(container.ownEntity)
            dataProperty.set(thisDto.dataModel, value)
            onValueSet("update", value)
        }
    }
}


class PropertyDelegate<DTO, D, E, V: Any> @PublishedApi internal constructor (
    dtoProvider: () -> CommonDTO<DTO, D, E>,
    dataProperty:KMutableProperty1<D, V>,
    entityProperty :KMutableProperty1<E, V>
): ResponsiveDelegate <DTO, D, E, V>(dtoProvider, dataProperty, entityProperty)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    override val qualifiedName: String
        get() = "PropertyDelegate[${thisDto.dtoName}]"

    override fun update(dataModel:D){
        val value = dataProperty.get(dataModel)
        dataProperty.set(thisDto.dataModel, value)
        entityProperty.set(thisDto.daoEntity, value)
        onValueSet("update", value)
    }

    override fun update(container: EntityUpdateContainer<E, *,*,*>){
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
            val value =  dataProperty.get(thisDto.dataModel)
            entityProperty.set(container.ownEntity, value)
            onValueSet("update", value)
        }else{
            val value = entityProperty.get(container.ownEntity)
            dataProperty.set(thisDto.dataModel, value)
            onValueSet("update", value)
        }
    }
}

