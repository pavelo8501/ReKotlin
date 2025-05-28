package po.exposify.dto.components.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.interfaces.ObservableData
import po.exposify.dto.components.relation_binder.models.RelationsUpdateParams
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.lognotify.TasksManaged
import po.misc.types.castListOrThrow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class RelationBindingDelegate<DTO, DATA, ENTITY, C_DTO, FD, FE, V>(
    internal val dto : CommonDTO<DTO, DATA, ENTITY>,
    internal val childModel: DTOClass<C_DTO, FD, FE>,
    val dataPropertyName : String
): ReadOnlyProperty<DTO, V>, TasksManaged, IdentifiableComponent
        where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity,
              C_DTO: ModelDTO,  FD: DataModel, FE : LongEntity
{

    abstract val componentName : String
    var crudOperation : CrudOperation = CrudOperation.Update
    override val type: ComponentType = ComponentType.RelationBindingDelegate

    override val qualifiedName : String  get() = "$componentName[]"
    private var propertyNameParameter: String = dataPropertyName
    val propertyName: String get()= propertyNameParameter

    abstract val cardinality : Cardinality

    abstract fun getEffectiveValue():V

    abstract fun getForeignDataModels(dataModel: DATA): List<FD>
    abstract fun getForeignEntities(entity: ENTITY): List<FE>

    var valueUpdated : Boolean = false
    abstract var onValueChanged: ((ObservableData)-> Unit)?
    fun subscribeRelationUpdates(valueChanged: (ObservableData)-> Unit){
        onValueChanged = valueChanged
    }

    abstract fun getForeignEntity(id: Long):FE?

    override fun getValue(thisRef: DTO, property: KProperty<*>): V{
        propertyNameParameter  = property.name
        return getEffectiveValue()
    }
}


class OneToManyDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    dto : CommonDTO<DTO, DATA, ENTITY>,
    childModel: DTOClass<F_DTO, FD, FE>,
    private val dataProperty: KMutableProperty1<DATA, MutableList<FD>>,
    private val entitiesProperty: KProperty1<ENTITY, SizedIterable<FE>>,
) : RelationBindingDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, List<F_DTO>>(dto, childModel,  dataProperty.name)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity {

    override val componentName: String =  "OneToManyDelegate"
    override fun getEffectiveValue(): List<F_DTO> {
        val clazz = childModel.config.registry.getRecord<F_DTO, OperationsException>(ComponentType.DTO).clazz
        val result = multipleRepository.getDTO()
            .castListOrThrow<F_DTO, OperationsException>(clazz, null, 0)
        onValueUpdated("getEffectiveValue", result)
        return result
    }

    val multipleRepository : MultipleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE> by lazy {
        dto.getOneToManyRepository(childModel)
    }
    override var onValueChanged: ((ObservableData)-> Unit)? = null

    override val cardinality : Cardinality = Cardinality.ONE_TO_MANY

    fun <V: Any> onValueUpdated(methodName: String, value: V){
        val updateData =  RelationsUpdateParams(dto, crudOperation, methodName,  propertyName, null, value, this)
        onValueChanged?.invoke(updateData)
    }

    fun saveDataModels(foreignDataModels: List<FD>){
        onValueUpdated("saveDataModels" ,foreignDataModels)
        dataProperty.get(dto.dataModel).addAll(foreignDataModels)
    }

    override fun getForeignDataModels(dataModel: DATA): List<FD>{
        onValueUpdated("getDataModels", dataModel)
        return dataProperty.get(dataModel).toList()
    }

    override fun getForeignEntities(entity: ENTITY): List<FE>{
        onValueUpdated("getForeignEntities", entity)
       return entitiesProperty.get(entity).toList()
    }

    override fun getForeignEntity(id: Long): FE?{
       onValueUpdated("getForeignEntity", id)
       return entitiesProperty.get(dto.daoEntity).firstOrNull { it.id.value == id }
    }

//   suspend fun processForeignEntities(entity: ENTITY, processFn:suspend (List<FE>)-> List<CommonDTO<F_DTO, FD, FE>>){
//        val foreignEntities = entitiesProperty.get(entity).toList()
//        val foreignDtos =  processFn.invoke(foreignEntities)
//        val mutableListOfForeignDataModels =   dataProperty.get(dto.dataModel)
//        foreignDtos.forEach {
//            mutableListOfForeignDataModels.add(it.dataModel)
//        }
//    }

    fun attachToForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>){
        TODO("Not yet implemented")
       // entitiesProperty.set(container.ownEntity, dto.daoEntity)
    }
}



class OneToOneDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    dto : CommonDTO<DTO, DATA, ENTITY>,
    childModel: DTOClass<F_DTO, FD, FE>,
    private val dataProperty: KMutableProperty1<DATA, FD>,
    private val entityProperty: KMutableProperty1<ENTITY, FE>,
) : RelationBindingDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, F_DTO>(dto, childModel,  dataProperty.name)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity {

    override val componentName: String =  "OneToManyDelegate"


    val singleRepository : SingleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE> by lazy {
        dto.getOneToOneRepository(childModel)
    }

    override var onValueChanged: ((ObservableData)-> Unit)? = null

    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE

    fun <V: Any> onValueUpdated(methodName: String, value: V){
        val updateData =  RelationsUpdateParams(dto, crudOperation, methodName, propertyName, null, value, this)
        onValueChanged?.invoke(updateData)
    }

    override fun getEffectiveValue():F_DTO{
        val dto = singleRepository.getDTO()
        return dto as F_DTO
    }

    fun getForeignDataModel(dataModel: DATA): FD{
        onValueUpdated("getDataModel", dataModel)
        return dataProperty.get(dataModel)
    }

    override fun getForeignDataModels(dataModel: DATA): List<FD>{
      return listOf(getForeignDataModel(dataModel))
    }


    fun saveDataModel(dataModel:FD){
        onValueUpdated("saveDataModel", dataModel)
        dataProperty.set(dto.dataModel, dataModel)
    }

    fun attachForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>){
        onValueUpdated("attachForeignEntity", container.ownEntity)
       // entityProperty.set(container.ownEntity, dto.daoEntity)
    }

    fun getForeignEntity(entity: ENTITY): FE{
        val foreignEntity = entityProperty.get(entity)
        onValueUpdated("getForeignEntity", foreignEntity)
        return foreignEntity
    }

    override fun getForeignEntity(id: Long): FE?{
        onValueUpdated("getForeignEntityById", id)
        val childEntity = entityProperty.get(dto.daoEntity)
        return if(childEntity.id.value == id){
            childEntity
        }else{
            null
        }
    }

    override fun getForeignEntities(entity: ENTITY): List<FE>{
      return  getForeignEntity(entity)?.let {
            listOf(it)
        }?:run {
            emptyList()
        }
    }
}