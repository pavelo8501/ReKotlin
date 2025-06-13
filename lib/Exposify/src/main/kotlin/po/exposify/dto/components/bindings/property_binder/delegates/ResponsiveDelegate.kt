package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.property_binder.interfaces.ObservableData
import po.exposify.dto.components.bindings.property_binder.interfaces.UpdateParams
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.enums.Delegates
import po.exposify.dto.helpers.getPropertyRecord
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.misc.data.SmartLazy
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiableModule
import po.misc.registries.callback.TypedCallbackRegistry
import po.misc.validators.mapping.models.MappingCheck
import kotlin.Any
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

sealed class ResponsiveDelegate<DTO, D, E, V: Any> protected constructor(
    protected val hostingDTO: CommonDTO<DTO, D, E>
): ReadWriteProperty<DTO, V>, DelegateInterface<DTO, DTO>
      where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override var status: DelegateStatus = DelegateStatus.Created

    enum class UpdateType(override val value : Int): ValueBased{
        DATA_UPDATE(1),
        ENTITY_UPDATE(2),
        DTO_UPDATE(3),
        CHANGE(4)
    }


    override val hostingClass: DTOBase<DTO, *, *>
        get() = hostingDTO.dtoClass
    override val foreignClass: DTOBase<DTO, *, *>
        get() = hostingDTO.dtoClass

    private var propertyParameter : KProperty<V>? = null
    val property: KProperty<V> get() = propertyParameter.getOrInitEx()


    val name: String by SmartLazy("Uninitialized"){
        propertyParameter?.name
    }

    override val module: IdentifiableModule = asIdentifiableModule(name, hostingDTO.sourceName,
        Delegates.ResponsiveDelegate)

    var dataPropertyParameter:KMutableProperty1<D, V>? = null
    val dataProperty:KMutableProperty1<D, V>
        get() = dataPropertyParameter.getOrOperationsEx()

    var entityPropertyParameter:KMutableProperty1<E, V>? = null
    val entityProperty:KMutableProperty1<E, V>
        get() = entityPropertyParameter.getOrOperationsEx()

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null

    var activeValue : V? = null
    var valueUpdated : Boolean = false

    protected val subscriptions : TypedCallbackRegistry<ObservableData, Unit> = TypedCallbackRegistry()
    abstract val propertyChecks : List<MappingCheck<V>>


    private fun notifyUpdate(type: UpdateType, value: V){
        subscriptions.triggerForAll(
            type,
            UpdateParams(hostingDTO, CrudOperation.Initialize, "update", name, activeValue, value, module)
        )
    }
    override fun resolveProperty(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property.castOrInitEx()
            module.updateName(property.name)
            hostingDTO.bindingHub.setResponsiveDelegate(this)
            onPropertyInitialized?.invoke(property)
        }
    }

    override fun updateStatus(status: DelegateStatus) {
        this.status = status
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
        entity?.let {
            entityProperty.set(it, value)
            dataProperty.set(hostingDTO.dataModel, value)
        }?:run {
            if(hostingDTO.isEntityInserted){
                entityProperty.set(hostingDTO.getEntity(module), value)
            }else{
                hostingDTO.logger.warn("${hostingDTO.completeName} attempted to update entity not inserted")
            }
        }
    }


    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ResponsiveDelegate<DTO, D, E, V> {
        resolveProperty(property)
        return this
    }
    override fun getValue(thisRef: DTO, property: KProperty<*>): V {
        resolveProperty(property)
        return activeValue?: dataProperty.get(hostingDTO.dataModel)
    }
    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) {
        resolveProperty(property)
        updateEntityProperty(value, null)
        valueChanged(UpdateType.DTO_UPDATE, value)
    }

    internal fun updateDataProperty(data : D, entity:E?){
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


class SerializedDelegate<DTO, D, E, V: Any> @PublishedApi internal constructor(
    dto : CommonDTO<DTO, D, E>,
    serializedDataProperty:KMutableProperty1<D, V>,
    serializedEntityProperty:KMutableProperty1<E, V>,
    override val  propertyChecks : List<MappingCheck<V>>
) : ResponsiveDelegate<DTO, D, E, V>(dto)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    init {
        dataPropertyParameter = serializedDataProperty
        entityPropertyParameter = serializedEntityProperty
    }
}


class PropertyDelegate<DTO, D, E, V: Any> @PublishedApi internal constructor (
    dto:  CommonDTO<DTO, D, E>,
    datProperty:KMutableProperty1<D, V>?,
    entProperty :KMutableProperty1<E, V>?,
    override var  propertyChecks : MutableList<MappingCheck<V>> = mutableListOf()
): ResponsiveDelegate <DTO, D, E, V>(dto)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    init {
        datProperty?.let {
            dataPropertyParameter = it
        }?:run {
            onPropertyInitialized = {
                dataPropertyParameter =  dto.getPropertyRecord(DTOClass, it.name)
                    .castOrInitEx("cast to MutableProperty failed")
            }
        }

        entProperty?.let {
            entityPropertyParameter = it
        }?:run {
            onPropertyInitialized = {
                entityPropertyParameter =  dto.getPropertyRecord(SourceObject.Entity, it.name)
                    .castOrInitEx("cast to MutableProperty failed")
            }
        }
    }
}

