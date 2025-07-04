package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.property_binder.interfaces.ObservableData
import po.exposify.dto.helpers.getPropertyRecord
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.extensions.castOrInit
import po.exposify.extensions.getOrInit
import po.exposify.extensions.getOrOperations
import po.misc.data.SmartLazy
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiableClass
import po.misc.registries.callback.TypedCallbackRegistry
import po.misc.validators.mapping.models.MappingCheck
import kotlin.Any
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

sealed class ResponsiveDelegate<DTO, D, E, V: Any> protected constructor(
    protected val hostingDTO: CommonDTO<DTO, D, E>
): ReadWriteProperty<DTO, V>, DelegateInterface<DTO, DTO>, IdentifiableClass
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
    val property: KProperty<V> get() = propertyParameter.getOrInit(this)

    val name: String by SmartLazy("Uninitialized"){
        propertyParameter?.name
    }
    override val identity = asIdentifiableClass("ResponsiveDelegate", hostingDTO.sourceName)

    var dataPropertyParameter:KMutableProperty1<D, V>? = null
    val dataProperty:KMutableProperty1<D, V>
        get() = dataPropertyParameter.getOrOperations("dataProperty", this)

    var entityPropertyParameter:KMutableProperty1<E, V>? = null
    val entityProperty:KMutableProperty1<E, V>
        get() = entityPropertyParameter.getOrOperations("entityProperty", this)

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null

    private var effectiveValue : V? = null
    private val value  : V
        get() =  effectiveValue.getOrOperations("Value accessed before initialization", this)
    val isValueNull : Boolean  get() = effectiveValue == null

    var valueUpdated : Boolean = false

    protected val subscriptions : TypedCallbackRegistry<ObservableData, Unit> = TypedCallbackRegistry()
    abstract val propertyChecks : List<MappingCheck<V>>


    private fun notifyUpdate(type: UpdateType, value: V){

    }
    override fun resolveProperty(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property.castOrInit(this)
            identity.updateSourceName(property.name)
            hostingDTO.bindingHub.setResponsiveDelegate(this)
            onPropertyInitialized?.invoke(property)
        }
    }

    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    private fun valueChanged(updateType : UpdateType, newValue : V){
        valueUpdated = true
        effectiveValue = newValue
        notifyUpdate(updateType, newValue)
        notifyUpdate(UpdateType.CHANGE, value)
    }

//    private fun updateEntityProperty(value:V, entity:E?){
//        entity?.let {
//            entityProperty.set(it, value)
//            dataProperty.set(hostingDTO.dataModel, value)
//        }?:run {
//            if(hostingDTO.isEntityInserted){
//                entityProperty.set(hostingDTO.getEntity(), value)
//            }else{
//                hostingDTO.logger.warn("${hostingDTO.completeName} attempted to update entity not inserted")
//            }
//        }
//    }

    internal fun updateBy(data:D){
        val newValue = dataProperty(data)
        if(effectiveValue != newValue){
            if(hostingDTO.isEntityInserted){
                val entity = hostingDTO.getEntity()
                entityProperty.set(entity, newValue)
            }else{
                hostingDTO.logger.warn("${hostingDTO.completeName} attempted to update entity not inserted")
            }
            valueChanged(UpdateType.DATA_UPDATE, newValue)
        }
    }

    /**
     * Updates entity form data model
     */
    internal fun update(entity:E){
        val newValue = dataProperty(hostingDTO.dataModel)
        if(effectiveValue!= newValue){
            entityProperty.set(entity, newValue)
            valueChanged(UpdateType.ENTITY_UPDATE, newValue)
        }
    }
    internal fun updateBy(entity:E){
        val newValue = entityProperty.get(entity)
        if(effectiveValue!= newValue){
            effectiveValue = newValue
            dataProperty.set(hostingDTO.dataModel, newValue)
            valueChanged(UpdateType.ENTITY_UPDATE, newValue)
        }
    }


    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ResponsiveDelegate<DTO, D, E, V> {
        resolveProperty(property)
        return this
    }
    override fun getValue(thisRef: DTO, property: KProperty<*>): V {
        resolveProperty(property)
        return value
    }
    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) {
        resolveProperty(property)
        if(effectiveValue != value){
            val dataModel = hostingDTO.dataModel
            dataProperty.set(dataModel, value)
            if(hostingDTO.isEntityInserted){
                val entity = hostingDTO.getEntity()
                entityProperty.set(entity, value)
            }else{
                hostingDTO.logger.warn("${hostingDTO.completeName} update through delegate set. Entity must have been inserted but its not")
            }
            valueChanged(UpdateType.DTO_UPDATE, value)
        }
    }





//    internal fun updateDataProperty(data : D, entity:E?){
//        val value = dataProperty.get(data)
//        updateEntityProperty(value, entity)
//        valueChanged(UpdateType.DATA_UPDATE, value)
//    }

//    internal fun updateDTOProperty(entity:E){
//        val value = entityProperty.get(entity)
//        dataProperty.set(hostingDTO.dataModel, value)
//        valueChanged(UpdateType.ENTITY_UPDATE, value)
//    }

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
                    .castOrInit(this)
            }
        }

        entProperty?.let {
            entityPropertyParameter = it
        }?:run {
            onPropertyInitialized = {
                entityPropertyParameter =  dto.getPropertyRecord(SourceObject.Entity, it.name)
                    .castOrInit(this)
            }
        }
    }
}

