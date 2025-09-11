package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrInit
import po.exposify.extensions.getOrInit
import po.lognotify.TasksManaged
import po.misc.collections.BufferAction
import po.misc.collections.BufferItem
import po.misc.collections.BufferItemStatus
import po.misc.collections.SlidingBuffer
import po.misc.collections.addToBuffer
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.data.SmartLazy
import po.misc.types.TypeData
import kotlin.Any
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty


enum class DataType{
    DataModel,
    Entity
}

data class AuxBufferParam(
    val externalCall: Boolean,
    var initial: Boolean,
){

    var updateByData: DataType = DataType.DataModel
    var updatingEntity: Boolean = false


    fun <E: LongEntity> updateByData(entity: E?):AuxBufferParam{
        if(entity != null){
            updatingEntity = true
        }
        updateByData = DataType.DataModel
        initial = false
        return this
    }

    fun updateByEntity():AuxBufferParam{
        updateByData = DataType.Entity
        initial = false
        updatingEntity = true
        return this
    }
}

internal fun newInternalParam(
    initial: Boolean = false
):AuxBufferParam{
  return  AuxBufferParam(externalCall = false, initial =  initial)
}

internal fun newExternalParam(
    initial: Boolean = false
):AuxBufferParam{
    return  AuxBufferParam(externalCall = true, initial =  initial)
}

sealed class ResponsiveDelegate<DTO, D, E, V : Any> protected constructor(
    protected val hostingDTO: CommonDTO<DTO, D, E>,
    protected val dataProperty: KMutableProperty1<D, V>,
    protected val entityProperty: KMutableProperty1<E, V>,
    protected val typeData: TypeData<V>,
) : ReadWriteProperty<DTO, V>, DelegateInterface<DTO, D, E>, TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {

    override var status: DelegateStatus = DelegateStatus.Created

    val dtoClass : DTOBase<DTO, D, E> = hostingDTO.dtoClass

    internal val buffer: SlidingBuffer<V, AuxBufferParam> = SlidingBuffer(this, typeData)


    private var propertyParameter: KProperty<V>? = null
    val property: KProperty<V> get() = propertyParameter.getOrInit(this)

    val name: String by SmartLazy("Uninitialized") {
        propertyParameter?.name
    }

    protected fun onValueCommit(value:V){

        hostingDTO.dataContainer.value?.let {dataModel->
            val dataValue = dataProperty.get(dataModel)
            if(dataValue != value){
                dataProperty.set(dataModel, value)
            }
        }

        hostingDTO.entityContainer.value?.let {
            entityProperty.set(it, value)
        }
    }

    protected fun onDataSubmitted(item:(BufferItem<V, AuxBufferParam>)): BufferAction{
        var result: BufferAction
        item.parameter?.let {auxParam->
            if(auxParam.externalCall){
                return BufferAction.Buffer
            }
            when(auxParam.updateByData){
                DataType.Entity -> {
                    val dataModel = hostingDTO.dataContainer.getValue(this)
                    dataProperty.set(dataModel, item.value)
                    return   BufferAction.Commit
                }
                DataType.DataModel->{
                    return  BufferAction.Commit
                }
            }
        }
       if(item.itemStatus != BufferItemStatus.SameAsRecent){
           result =   BufferAction.Commit
          BufferAction.Commit
        }else{
           result = BufferAction.Buffer
        }
        return result
    }

    protected fun onSameDataSubmitted(item:(BufferItem<V, AuxBufferParam>)): BufferAction{
       warning("dataValue ${item.value} Re-Submitted")
       return BufferAction.Ignore
    }

    fun resolveProperty(property: KProperty<*>) {
        propertyParameter = property.castOrInit(this)
        identity.setNamePattern { "ResponsiveDelegate[${hostingDTO.identifiedByName}, ${property.name}]" }

        buffer.onCommit(::onValueCommit)
        buffer.onValueReceived(::onDataSubmitted)
        buffer.onSameAsRecent(::onSameDataSubmitted)
        hostingDTO.dataContainer.requestValue(this){dataModel->
            val value = dataProperty.get(dataModel)
            addToBuffer(buffer, value, newInternalParam())
            hostingDTO.bindingHub.registerResponsiveDelegate(this)
        }
    }

    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    internal fun updateBy(
        callingContext: CTX,
        entity: E
    ):V {
        val value = entityProperty.get(entity)
        val param = newInternalParam().updateByEntity()
        callingContext.addToBuffer(buffer, value, param)
        return value
    }

    internal fun updateBy(
        callingContext: CTX,
        dataModel: D
    ):V {
        val value = dataProperty.get(dataModel)
        val param = newInternalParam().updateByData(null)
        callingContext.addToBuffer(buffer, value, param)
        return value
    }

    internal fun updateEntity(
        callingContext: CTX,
        entity: E
    ) {
        val dataModel = hostingDTO.dataContainer.getValue(this)
        val value =  dataProperty.get(dataModel)
        entityProperty.set(entity, value)
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ResponsiveDelegate<DTO, D, E, V> {
        resolveProperty(property)
        return this
    }

    override fun getValue(thisRef: DTO, property: KProperty<*>): V {
        return  buffer.getValue(this)
    }

    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) {
        addToBuffer(buffer, value, newExternalParam())
    }

    override fun toString(): String = "$name = ${buffer.value?:"N/A"}"
}

class SerializedDelegate<DTO, D, E, V : Any>
    @PublishedApi
    internal constructor(
        dto: CommonDTO<DTO, D, E>,
        serializedDataProperty: KMutableProperty1<D, V>,
        serializedEntityProperty: KMutableProperty1<E, V>,
        typeData: TypeData<V>
    ) : ResponsiveDelegate<DTO, D, E, V>(dto,serializedDataProperty, serializedEntityProperty,  typeData) where DTO : ModelDTO, D : DataModel, E : LongEntity {
    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dto)

}

class PropertyDelegate<DTO, D, E, V : Any>
    @PublishedApi
    internal constructor(
        dto: CommonDTO<DTO, D, E>,
        dataProperty: KMutableProperty1<D, V>,
        entityProperty: KMutableProperty1<E, V>,
        typeData: TypeData<V>
    ) : ResponsiveDelegate<DTO, D, E, V>(dto, dataProperty,entityProperty, typeData) where DTO : ModelDTO, D : DataModel, E : LongEntity {


    override val identity: CTXIdentity<PropertyDelegate<DTO, D, E, V>> = asSubIdentity(this, dto)

}
