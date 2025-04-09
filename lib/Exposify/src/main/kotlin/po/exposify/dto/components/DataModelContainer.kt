package po.exposify.dto.components

import po.exposify.binders.PropertyBinder
import po.exposify.binders.PropertyType
import po.exposify.binders.UpdateMode
import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.models.DataPropertyInfo
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible


class DataModelContainer<DTO : ModelDTO, DATA: DataModel>(
    internal val dataModel: DATA,
    val dataBlueprint: ClassBlueprint<DATA>,
    private var binder: PropertyBinder<DATA, *>? = null,
): DataModel {

    override var id: Long = dataModel.id

    val trackedProperties: MutableMap<String, DataPropertyInfo<DTO, DATA, ExposifyEntityBase, ModelDTO>> = mutableMapOf()

    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableList<DATA> {
        property.getter.let { getter ->
            getter.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return getter.call(thisRef) as MutableList<DATA>
        }
    }

    fun binderUpdatedProperty(name : String, type : PropertyType, updateMode : UpdateMode){
        println("BinderUpdatedProperty callback triggered by $name")
    }

    fun setDataModelId(id: Long){
        dataModel.id = id
    }

    fun setTrackedProperties(list: List<DataPropertyInfo<DTO, DATA, ExposifyEntityBase, ModelDTO>>){
        list.forEach {propertyInfo->
            dataBlueprint.propertyMap[propertyInfo.name]?.let {blueprintProperty->
                propertyInfo.inBlueprint = blueprintProperty
            }
            trackedProperties[propertyInfo.name] = propertyInfo
        }
    }

    fun attachBinder(propertyBinder: PropertyBinder<DATA, *>){
        binder = propertyBinder
        propertyBinder.syncedList.forEach {
            it.onPropertyUpdated(::binderUpdatedProperty)
        }
    }

    fun extractChildModels(
        forPropertyInfo : DataPropertyInfo<DTO, DATA, ExposifyEntityBase, ModelDTO>): List<DataModel>
    {
        if(forPropertyInfo.cardinality == Cardinality.ONE_TO_MANY){
            val property = forPropertyInfo.getOwnModelsProperty()
            if(property != null) {
                return property.get(dataModel).toList()
            }else{
                throw OperationsException(
                    "Property for name ${forPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_MANY",
                    ExceptionCode.BINDING_PROPERTY_MISSING)
            }
        }

        if(forPropertyInfo.cardinality == Cardinality.ONE_TO_ONE){
            val property = forPropertyInfo.getOwnModelProperty()
            if(property != null) {
                val dataModel = property.get(dataModel)
                if(dataModel != null){
                    return  listOf<DataModel>(dataModel)
                }
            }else{
                throw OperationsException(
                    "Property for name ${forPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_ONE",
                    ExceptionCode.BINDING_PROPERTY_MISSING)
            }
        }
        return emptyList()
    }

    fun <CHILD_DATA: DataModel>addToMutableProperty(name: String, value: CHILD_DATA){
        dataBlueprint.propertyMap[name]?.let {
            @Suppress("UNCHECKED_CAST")
            it as KProperty1<DATA, MutableList<CHILD_DATA>>
            it.isAccessible = true
            it.get(dataModel).add(value)
        }
    }

    fun <CHILD_DATA: DataModel>setProperty(name: String, value: CHILD_DATA){

        dataBlueprint.propertyMap[name]?.let {
            @Suppress("UNCHECKED_CAST")
            it as KMutableProperty1<DATA, CHILD_DATA>
            it.isAccessible = true
            it.set(dataModel, value)
        }
    }

}