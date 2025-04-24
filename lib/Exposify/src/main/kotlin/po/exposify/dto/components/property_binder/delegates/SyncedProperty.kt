package po.exposify.dto.components.property_binder.delegates

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.getPropertyByValue
import po.misc.getKType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


interface SyncedProperty<DATA : DataModel, V> : ReadWriteProperty<ModelDTO, V>{

}

class SyncedContainer<DATA: DataModel, ENTITY: ExposifyEntityBase, Any>(private val dataModel: DATA, private val entity: ENTITY){

    @PublishedApi
    internal var dataModelResult: Int? = null
    fun dataModel(block: DATA.()-> Int) {
        dataModelResult = block.invoke(dataModel)
    }

    @PublishedApi
    internal var  entityModelResult: Int? = null
    fun entityModel(block: ENTITY.()-> Int) {
        entityModelResult = block.invoke(entity)
    }
}

inline fun  <DTO: ModelDTO> DTO.propertyBindings(block: DTO.()-> Unit){


   return this.block()

}


inline fun <reified DATA : DataModel, reified ENTITY: ExposifyEntityBase, V>  ModelDTO.synced2(block : SyncedContainer<DATA, ENTITY, V>.()-> Unit): ReadWriteProperty<ModelDTO, V> {

    val castedDataModel = dataModel.castOrInitEx<DATA>()
    val entityDataModel =  daoService.entity.castOrInitEx<ENTITY>()

    val container = SyncedContainer<DATA, ENTITY, V>(castedDataModel, entityDataModel)
    container.block()

    val dataModelResult = container.dataModelResult.castOrInitEx<DATA>()
    val kType = dataModel.getKType().getOrOperationsEx()
    val propRef = castedDataModel.getPropertyByValue(kType, dataModelResult)

    val result =  when (kType) {
        String::class -> DataModelStringDelegate(this, propRef as  KProperty1<DATA, String>)
        Int::class -> DataModelIntDelegate(this, propRef as KProperty1<DATA, Int>)
        Long::class -> DataModelLongDelegate(this,  propRef as KProperty1<DATA, Long>)
//        Boolean::class -> DataModelBooleanDelegate(this, propName)
//        Float::class -> DataModelFloatDelegate(this, propName)
//        Double::class -> DataModelDoubleDelegate(this, propName)
//        LocalDateTime::class -> DataModelDateTimeDelegate(this, propName)
        else -> throw IllegalArgumentException("Unsupported type")
    }
    return result.castOrInitEx<ReadWriteProperty<ModelDTO,  V>>("ReadWriteProperty<DTO, V> cast failed")

}



inline fun <reified DATA : DataModel, V>  ModelDTO.synced(block :  DATA.()->V): ReadWriteProperty<ModelDTO, V> {

    val casted = dataModel.castOrInitEx<DATA>()
    val valueResult =  block.invoke(casted)
    val kType = valueResult?.getKType().getOrOperationsEx()
    val propRef = casted.getPropertyByValue(kType, valueResult)

   val result =  when (kType) {
        String::class -> DataModelStringDelegate(this, propRef as  KProperty1<DATA, String>)
        Int::class -> DataModelIntDelegate(this, propRef as KProperty1<DATA, Int>)
        Long::class -> DataModelLongDelegate(this,  propRef as KProperty1<DATA, Long>)
//        Boolean::class -> DataModelBooleanDelegate(this, propName)
//        Float::class -> DataModelFloatDelegate(this, propName)
//        Double::class -> DataModelDoubleDelegate(this, propName)
//        LocalDateTime::class -> DataModelDateTimeDelegate(this, propName)
        else -> throw IllegalArgumentException("Unsupported type")
    }
    return result.castOrInitEx<ReadWriteProperty<ModelDTO,  V>>("ReadWriteProperty<DTO, V> cast failed")
}


class DataModelStringDelegate<DATA : DataModel>(
    dto: ModelDTO,
    property: KProperty1<DATA, String>
) : SimpleDelegate<DATA, String>(dto, property)

class DataModelIntDelegate<DATA : DataModel>(
    dto: ModelDTO,
    property: KProperty1<DATA, Int>
) : SimpleDelegate<DATA, Int>(dto, property)

class DataModelLongDelegate<DATA : DataModel>(
    dto: ModelDTO,
    property: KProperty1<DATA, Long>
) : SimpleDelegate<DATA, Long>(dto, property)

//class DataModelIntDelegate<DATA: DataModel>(private val dto: DTO, name: String): SimpleDelegate<DTO, Int>(dto, name)
//
//class DataModelLongDelegate<DATA: DataModel>(private val dto: DTO, name: String): SimpleDelegate<DTO, Long>(dto, name)
//
//class DataModelDoubleDelegate<DATA: DataModel>(private val dto: DTO, name: String): SimpleDelegate<DTO, Double>(dto, name)
//
//class DataModelBooleanDelegate<DATA: DataModel>(dto: DTO, name: String) : SimpleDelegate<DTO, Boolean>(dto, name)
//
//class DataModelFloatDelegate<DATA: DataModel>(dto: DTO, name: String) : SimpleDelegate<DTO, Float>(dto, name)
//
//class DataModelDateTimeDelegate<DATA: DataModel>(private val dto: DTO, name: String): SimpleDelegate<DTO, LocalDateTime>(dto, name)

sealed class SimpleDelegate<DATA: DataModel, V>(private val dto: ModelDTO, val dataModel : KProperty1<DATA, V>): SyncedProperty<ModelDTO, V> {
    override fun getValue(thisRef: ModelDTO, property: KProperty<*>):V{
       return dto.dataContainer.getValue<V>(property.name, property)
    }

    override fun setValue(thisRef: ModelDTO, property: KProperty<*>, value: V){
        dto.dataContainer.setValue(property.name, property, value)
    }
}
