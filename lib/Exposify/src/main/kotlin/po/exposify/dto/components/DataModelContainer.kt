package po.exposify.dto.components


import po.exposify.dto.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KProperty


class DataModelContainer<DTO : ModelDTO, DATA: DataModel>(
    internal val dataModel: DATA,
    val dataBlueprint: ClassBlueprint<DATA>,
    private var propertyBinder: PropertyBinder<DATA, *>
): DataModel {

    override var id: Long = dataModel.id

    //val trackedProperties: MutableMap<String, DataPropertyInfo<DTO, DATA, ExposifyEntity, ModelDTO, DataModel, ExposifyEntity>> = mutableMapOf()

    init {
        propertyBinder.onPropertyUpdate
        propertyBinder.syncedSerializedPropertyList.forEach {
            it.dataProperty
        }
    }

    operator fun <V: Any?>  getValue(name: String,  property: KProperty<*>): V {
        return  dataBlueprint.getProperty(dataModel,  name)
    }

    operator fun <V>  setValue(name: String,  property: KProperty<*>, value: V){
        dataBlueprint.setProperty(dataModel, name,  value)
    }

    fun binderUpdatedProperty(name : String, type : PropertyType, updateMode : UpdateMode){
       // println("BinderUpdatedProperty callback triggered by $name")
    }

    fun setDataModelId(id: Long){
        dataModel.id = id
    }

//    fun setTrackedProperties(list: List<DataPropertyInfo<DTO, DATA, ExposifyEntity, ModelDTO, DataModel, ExposifyEntity>>){
//        list.forEach {propertyInfo->
//            trackedProperties[propertyInfo.name] = propertyInfo
//        }
//    }

//    fun extractChildModels(
//        forPropertyInfo : DataPropertyInfo<DTO, DATA, ExposifyEntity, ModelDTO, DataModel, ExposifyEntity>): List<DataModel>
//    {
//        if(forPropertyInfo.cardinality == Cardinality.ONE_TO_MANY){
//            val property = forPropertyInfo.getOwnModelsProperty()
//            if(property != null) {
//                return property.get(dataModel).toList()
//            }else{
//                throw OperationsException(
//                    "Property for name ${forPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_MANY",
//                    ExceptionCode.BINDING_PROPERTY_MISSING)
//            }
//        }
//
//        if(forPropertyInfo.cardinality == Cardinality.ONE_TO_ONE){
//            val property = forPropertyInfo.getOwnModelProperty()
//            if(property != null) {
//                val dataModel = property.get(dataModel)
//                if(dataModel != null){
//                    return  listOf<DataModel>(dataModel)
//                }
//            }else{
//                throw OperationsException(
//                    "Property for name ${forPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_ONE",
//                    ExceptionCode.BINDING_PROPERTY_MISSING)
//            }
//        }
//        return emptyList()
//    }
}