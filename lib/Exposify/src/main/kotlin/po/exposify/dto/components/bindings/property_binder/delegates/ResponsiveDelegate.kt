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
    protected val dto: CommonDTO<DTO, D, E>,
): ReadWriteProperty<DTO, V>, IdentifiableComponent
      where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    enum class SubscriptionType(override val value : Int): ValueBased{
        ON_CHANGE(1)
    }

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

    private fun propertyProvided(property: KProperty<Any?>){
        if(propertyParameter == null){
            propertyParameter = property
            dto.bindingHub.setBinding(this)
        }
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ResponsiveDelegate<DTO, D, E, V> {
        propertyProvided(property)
        return this
    }
    override fun getValue(thisRef: DTO, property: KProperty<*>): V {
        propertyProvided(property)
        return dataProperty.get(dto.dataModel)
    }
    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) {
        propertyProvided(property)
        valueChanged("setValue", value)
    }

    fun subscribeUpdates(subscriber: Identifiable, callback: (ObservableData)-> Unit){
        subscriptions.subscribe(subscriber, SubscriptionType.ON_CHANGE, callback)
    }

    private var thisAsDTO: DTO? = null
    private fun asDTO():DTO{
      return thisAsDTO?:run {
          val casted =  dto.castOrThrow<DTO, OperationsException>(dto.dtoClass.getTypeRecord<DTO,D,E, DTO>(ComponentType.DTO).clazz)
          thisAsDTO = casted
          casted
      }
    }

    //abstract fun insert(dataModel:D)
   // abstract fun updateProperties(container: EntityUpdateContainer<E, *,*,*>)
    protected fun valueChanged(methodName: String,  value : V){
        if(activeValue != value){
            valueUpdated = true
            subscriptions.trigger(
                SubscriptionType.ON_CHANGE,
                UpdateParams(dto, CrudOperation.Initialize,  methodName, propertyName, activeValue, value, this)
            )
            activeValue = value
        }
    }

    internal fun updateDTOProperty(data : D){
        val value = dataProperty.get(data)
        setValue(asDTO(), property, value)
        valueChanged("update", value)
    }

    internal fun updateDTOProperty(entity:E){
        val value = entityProperty.get(entity)
        setValue(asDTO(), property, value)
        dataProperty.set(dto.dataModel, value)
        valueChanged("update", value)
    }

    internal fun updateEntityProperty(entity:E){
        val value = getValue(asDTO(), property)
        entityProperty.set(entity, value)
    }

}


class SerializedDelegate<DTO, D, E, V: Any> internal constructor(
    dto : CommonDTO<DTO, D, E>,
    var serializedDataProperty:KMutableProperty1<D, V>,
    var serializedEntityProperty:KMutableProperty1<E, V>,
    override val  propertyChecks : List<MappingCheck>
) : ResponsiveDelegate<DTO, D, E, V>(dto)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{

    override val qualifiedName: String
        get() = "SerializedDelegate[${dto.dtoName}]"

    init {
        dataPropertyParameter = serializedDataProperty
        entityPropertyParameter = serializedEntityProperty
    }

//    internal fun updateDTOProperty(data : D){
//        val value = dataProperty.get(data)
//        setValue(asDTO(), property, value)
//        valueChanged("update", value)
//    }
//
//    override fun insert(dataModel: D) {
//        val value = dataProperty.get(dataModel)
//        if(activeValue != value){
//            entityProperty.set(dto.entity, value)
//            dataProperty.set(dto.dataModel, value)
//            valueChanged("update", value)
//        }
//    }
//
//    override fun updateProperties(container: EntityUpdateContainer<E, *, *, *>) {
//        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
//            val value =  dataProperty.get(dto.dataModel)
//            entityProperty.set(container.ownEntity, value)
//            valueChanged("update", value)
//        }else{
//            val value = entityProperty.get(container.ownEntity)
//            dataProperty.set(dto.dataModel, value)
//            valueChanged("update", value)
//        }
//    }
}


class PropertyDelegate<DTO, D, E, V: Any> @PublishedApi internal constructor (
    dto:  CommonDTO<DTO, D, E>,
    val datProperty:KMutableProperty1<D, V>?,
    val entProperty :KMutableProperty1<E, V>?,
    override var  propertyChecks : MutableList<MappingCheck> = mutableListOf<MappingCheck>()
): ResponsiveDelegate <DTO, D, E, V>(dto)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val qualifiedName: String
        get() = "PropertyDelegate[${dto.dtoName}]"

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

