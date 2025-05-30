package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.bindings.property_binder.enums.UpdateMode
import po.exposify.dto.components.bindings.property_binder.interfaces.ObservableData
import po.exposify.dto.components.bindings.property_binder.interfaces.UpdateParams
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.helpers.getPropertyRecord
import po.exposify.dto.helpers.getTypeRecord
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.models.MappingCheck
import po.misc.registries.callback.TypedCallbackRegistry
import po.misc.types.castOrThrow
import kotlin.Any
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

sealed class ResponsiveDelegate<DTO, D, E, V: Any> protected constructor(
    protected val hostingDTO: CommonDTO<DTO, D, E>,
): ReadWriteProperty<DTO, V>, IdentifiableComponent
      where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    enum class UpdateType(override val value : Int): ValueBased{
        DATA_UPDATE(1),
        ENTITY_UPDATE(2),
        DTO_UPDATE(3),
        CHANGE(4)
    }

    private var thisAsDTO: DTO? = null

    var dataPropertyParameter:KMutableProperty1<D, V>? = null
    val dataProperty:KMutableProperty1<D, V>
        get() = dataPropertyParameter.getOrOperationsEx()

    var entityPropertyParameter:KMutableProperty1<E, V>? = null
    val entityProperty:KMutableProperty1<E, V>
        get() = entityPropertyParameter.getOrOperationsEx()

    abstract override val qualifiedName: String
    override val type: ComponentType = ComponentType.ResponsiveDelegate

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null
    private var propertyParameter : KProperty<*>? = null
        set(value) {
            field = value
            onPropertyInitialized?.invoke(property)
        }
    val property: KProperty<Any?> get() = propertyParameter.getOrOperationsEx()
    val propertyName : String get() = propertyParameter?.name?:""
    var activeValue : V? = null
    var valueUpdated : Boolean = false

    protected val subscriptions = TypedCallbackRegistry<ObservableData>()
    abstract val propertyChecks : List<MappingCheck>

    private fun asDTO():DTO{
        return thisAsDTO?:run {
            val casted =  hostingDTO.castOrThrow<DTO, OperationsException>(hostingDTO.dtoClass.getTypeRecord<DTO,D,E, DTO>(ComponentType.DTO).clazz)
            thisAsDTO = casted
            casted
        }
    }

    private fun notifyUpdate(type: UpdateType, value: V){
        subscriptions.trigger(
            type,
            UpdateParams(hostingDTO, CrudOperation.Initialize, "update", propertyName, activeValue, value, this)
        )
    }
    private fun propertyProvided(property: KProperty<Any?>){
        if(propertyParameter == null){
            propertyParameter = property
            hostingDTO.bindingHub.setBinding(this)
        }
    }

    private fun valueChanged(updateType : UpdateType, value : V){
        if(activeValue != value){
            valueUpdated = true
            notifyUpdate(updateType, value)
            notifyUpdate(UpdateType.CHANGE, value)
            activeValue = value
        }
    }

    private fun updateEntityProperty(value:V, entity:E?){
        entity?.let { entityProperty.set(it, value) }?:run {
            if(hostingDTO.isEntityInserted){
                entityProperty.set(hostingDTO.entity, value)
            }else{
                hostingDTO.logger.warn("${hostingDTO.dtoName} attempted to update entity not inserted")
            }
        }
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ResponsiveDelegate<DTO, D, E, V> {
        propertyProvided(property)
        return this
    }
    override fun getValue(thisRef: DTO, property: KProperty<*>): V {
        propertyProvided(property)
        return activeValue?: dataProperty.get(hostingDTO.dataModel)
    }
    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) {
        propertyProvided(property)
        updateEntityProperty(value, null)
        valueChanged(UpdateType.DTO_UPDATE, value)
    }

    internal fun updateDTOProperty(data : D, entity:E?){
        val value = dataProperty.get(data)
        updateEntityProperty(value, entity)
        valueChanged(UpdateType.DATA_UPDATE, value)
    }

    internal fun updateDTOProperty(entity:E){
        val value = entityProperty.get(entity)
        dataProperty.set(hostingDTO.dataModel, value)
        valueChanged(UpdateType.ENTITY_UPDATE, value)
    }

    fun subscribeUpdates(subscriber: Identifiable, callback: (ObservableData)-> Unit){
        subscriptions.subscribe(subscriber, UpdateType.CHANGE, callback)
    }
}


class SerializedDelegate<DTO, D, E, V: Any> internal constructor(
    dto : CommonDTO<DTO, D, E>,
    serializedDataProperty:KMutableProperty1<D, V>,
    serializedEntityProperty:KMutableProperty1<E, V>,
    override val  propertyChecks : List<MappingCheck>
) : ResponsiveDelegate<DTO, D, E, V>(dto)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    override val qualifiedName: String
        get() = "SerializedDelegate[${hostingDTO.dtoName}]"

    init {
        dataPropertyParameter = serializedDataProperty
        entityPropertyParameter = serializedEntityProperty
    }
}


class PropertyDelegate<DTO, D, E, V: Any> @PublishedApi internal constructor (
    dto:  CommonDTO<DTO, D, E>,
    datProperty:KMutableProperty1<D, V>?,
    entProperty :KMutableProperty1<E, V>?,
    override var  propertyChecks : MutableList<MappingCheck> = mutableListOf<MappingCheck>()
): ResponsiveDelegate <DTO, D, E, V>(dto)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val qualifiedName: String
        get() = "PropertyDelegate[${hostingDTO.dtoName}]"

    init {
        datProperty?.let {
            dataPropertyParameter = it
        }?:run {
            onPropertyInitialized = {
                dataPropertyParameter =  dto.getPropertyRecord(ComponentType.DATA_MODEL, it.name).property
                    .castOrInitEx("cast to MutableProperty failed")
                propertyChecks.addAll(listOf(MappingCheck(dto.dtoClass, ComponentType.DTO, ComponentType.DATA_MODEL)))
            }
        }

        entProperty?.let {
            entityPropertyParameter = it
        }?:run {
            onPropertyInitialized = {
                entityPropertyParameter =  dto.getPropertyRecord(ComponentType.ENTITY, it.name).property
                    .castOrInitEx("cast to MutableProperty failed")
                propertyChecks.addAll(listOf(MappingCheck(dto.dtoClass, ComponentType.DTO, ComponentType.ENTITY)))
            }
        }
    }
}

