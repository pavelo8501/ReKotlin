package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.helpers.getPropertyRecord
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.extensions.castOrInit
import po.exposify.extensions.getOrInit
import po.exposify.extensions.getOrOperations
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.builders.callbackManager
import po.misc.context.CTX
import po.misc.data.SmartLazy
import po.misc.reflection.properties.models.PropertyUpdate
import po.misc.types.getOrManaged
import kotlin.Any
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

sealed class ResponsiveDelegate<DTO, D, E, V: Any> protected constructor(
    protected val hostingDTO: CommonDTO<DTO, D, E>,
   // override val identity: Iden,
): ReadWriteProperty<DTO, V>, DelegateInterface<DTO, DTO>, CTX where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override var status: DelegateStatus = DelegateStatus.Created
    enum class UpdateType(){
        PropertyUpdated
    }
    val notifier: CallbackManager<UpdateType> = callbackManager(
        { CallbackManager.createPayload<UpdateType, List<PropertyUpdate<V>>>(this, UpdateType.PropertyUpdated) }
    )
    val hub: BindingHub<DTO, D, E> = hostingDTO.hub

    override val hostingClass: DTOBase<DTO, *, *>
        get() = hostingDTO.dtoClass

    protected val dataModel:D get(){
        return hostingDTO.dataContainer.source
    }

    protected val entity:E get(){
        return hostingDTO.entityContainer.source
    }



    private var propertyParameter : KProperty<V>? = null
    val property: KProperty<V> get() = propertyParameter.getOrInit(this)

    val name: String by SmartLazy("Uninitialized"){
        propertyParameter?.name
    }
    var dataPropertyParameter:KMutableProperty1<D, V>? = null
    val dataProperty:KMutableProperty1<D, V>
        get() = dataPropertyParameter.getOrOperations(this)

    var entityPropertyParameter:KMutableProperty1<E, V>? = null
    val entityProperty:KMutableProperty1<E, V>
        get() = entityPropertyParameter.getOrOperations(this)

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null

    private var effectiveValue : V? = null
    private val value  : V
        get() =  effectiveValue.getOrManaged("V")
    val isValueNull : Boolean  get() = effectiveValue == null
    var valueUpdated : Boolean = false

    abstract val propertyChecks : List<MappingCheck<V>>

    override fun resolveProperty(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property.castOrInit(this)
            identity.updateSourceName(property.name)
            hostingDTO.hub.registerResponsiveDelegate(this)
            onPropertyInitialized?.invoke(property)
        }
    }

    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    private fun valueChanged(newValue : V){
        valueUpdated = true
        effectiveValue = newValue
    }

    internal fun updateBy(data:D):V{
        val newValue = dataProperty(data)
        if(effectiveValue != newValue){
            effectiveValue = newValue
            valueChanged(newValue)
        }
        return newValue
    }

    internal fun updateBy(entity:E){
        val newValue = entityProperty.get(entity)
        if(effectiveValue!= newValue){
            effectiveValue = newValue
            dataProperty.set(dataModel, newValue)
            valueChanged(newValue)
        }
    }

    /**
     * Updates entity form data model
     */
    internal fun update(entity:E){

        val value = effectiveValue?:dataProperty(dataModel)

        entityProperty.set(entity, value)
    }

    /**
     * Updates data model  form pre-saved entity
     */
    internal fun updateData(){
        val value = effectiveValue?:entityProperty(entity)
        dataProperty.set(dataModel, value)
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

            dataProperty.set(dataModel, value)
            if(hostingDTO.entityContainer.isSourceAvailable){
                entityProperty.set(entity, value)
            }else{
                hostingDTO.logger.dataProcessor.warn("${hostingDTO.completeName} update through delegate set. Entity must have been inserted but its not")
            }
            valueChanged(value)
        }
    }
}


class SerializedDelegate<DTO, D, E, V: Any> @PublishedApi internal constructor(
    dto : CommonDTO<DTO, D, E>,
    serializedDataProperty:KMutableProperty1<D, V>,
    serializedEntityProperty:KMutableProperty1<E, V>,
    override val  propertyChecks : List<MappingCheck<V>>
) : ResponsiveDelegate<DTO, D, E, V>(dto, ClassIdentity.create("SerializedDelegate", dto.sourceName))
        where DTO: ModelDTO, D: DataModel, E: LongEntity {
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
): ResponsiveDelegate <DTO, D, E, V>(dto, ClassIdentity.create("PropertyDelegate", dto.sourceName))
        where DTO: ModelDTO, D: DataModel, E: LongEntity{
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

