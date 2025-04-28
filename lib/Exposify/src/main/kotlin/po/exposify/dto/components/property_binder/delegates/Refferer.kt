package po.exposify.dto.components.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntityClass
import po.auth.extensions.testAndLet
import po.exposify.classes.interfaces.DataModel

import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class ModelIDEntityDelegate<DATA : DataModel, ENTITY: ExposifyEntityBase>(
    val dto: ModelDTO,
): SyncedProperty<ModelDTO, Long>  {

    var  getName: String = ""
    var  setName: String = ""
    var  getValueProperty: KProperty<*>? = null
    var  setValueProperty: KProperty<*>? = null

    override fun getValue(thisRef: ModelDTO, property: KProperty<*>):Long{
        getName = property.name
        getValueProperty = property
       return dto.dataContainer.getValue(property.name, property)
    }

    override fun setValue(thisRef: ModelDTO, property: KProperty<*>, value: Long){
        setName = property.name
        setValueProperty = property
        dto.dataContainer.setValue(property.name, property, value)
    }


    fun onBinderUpdate(
        hook: suspend (DATA, ENTITY, UpdateMode) -> Unit
    ): Unit{

    }

    var dataProperty : KMutableProperty1<DATA, Long>? = null
    fun dataModelProperty(dataModelReferenceIdPropertyRef: KMutableProperty1<DATA, Long>) {
        dataProperty = dataModelReferenceIdPropertyRef
        dto.propertyBinder.onUpdate(){data, entity, updateMode->
            when(updateMode){
                UpdateMode.MODEL_TO_ENTITY -> {
                    println(entity.id)
                }
               else -> {

               }
            }
        }

    }

    var refEntity: ENTITY? = null
    var refEntityModel: LongEntityClass<ENTITY>? = null
    fun referencedEntityModel(entityModel: LongEntityClass<ENTITY>) {
        refEntityModel = entityModel
        entityModel.findById(dto.id)?.let { entity ->
            val value = entity.id.value
            refEntity = entity

            setValue(dto, setValueProperty as KProperty<*>, value)
            val a = 10
        }
    }

    internal fun <T> getEntity(itContext: T,  longEntityContext: LongEntityClass<ENTITY>.(T, ENTITY) -> Unit) {
          refEntity?.let {entity->
            refEntityModel.testAndLet({ it != null }){
                onSuccess {
                    longEntityContext.invoke(this!!, itContext, entity)
                }

            }
        }
    }
}

inline fun <DATA : DataModel, reified ENTITY: ExposifyEntityBase> ModelDTO.parentReference(
     containerFn:  ModelIDEntityDelegate<DATA, ENTITY>.()-> Unit
): ModelIDEntityDelegate<DATA , ENTITY> {
    val container : ModelIDEntityDelegate<DATA, ENTITY> = ModelIDEntityDelegate(this)
    container.containerFn()
    return  container
}

sealed class ComplexDelegate<DATA: DataModel, ENTITY: ExposifyEntityBase,V>(
    private val dto: ModelDTO,
    val dataModel : KProperty1<DATA, V>): SyncedProperty<ModelDTO, V> {
    override fun getValue(thisRef: ModelDTO, property: KProperty<*>):V{
        return dto.dataContainer.getValue<V>(property.name, property)
    }
    override fun setValue(thisRef: ModelDTO, property: KProperty<*>, value: V){
        dto.dataContainer.setValue(property.name, property, value)
    }
}
