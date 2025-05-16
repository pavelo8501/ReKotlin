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

class OneToOneDelegate<DTO, DATA, ENTITY, C_DTO,  CD,  FE>(
    dtoProvider : () -> CommonDTO<DTO, DATA, ENTITY>,
    private val childModel: DTOClass<C_DTO, CD, FE>,
    private val dataProperty: KMutableProperty1<DATA, CD>,
    private val entityProperty: KProperty1<ENTITY, FE>,
    private val foreignEntity: KMutableProperty1<FE, ENTITY>,
) : RelationBindingDelegate<DTO, DATA, ENTITY, C_DTO,  CD, FE, CommonDTO<C_DTO, CD, FE>>(dtoProvider, "OneToOneDelegate", dataProperty.name)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              C_DTO: ModelDTO,  CD : DataModel, FE : LongEntity
{

    val singleRepository : SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, FE> by lazy {
        thisDto.getOneToOneRepository(childModel)
    }

    override var onValueChanged: ((ObservableData)-> Unit)? = null

    fun <V: Any> onValueUpdated(methodName: String, value: V){
        val updateData =  RelationsUpdateParams(thisDto, methodName, propertyName, null, value, this)
        onValueChanged?.invoke(updateData)
    }

    override fun getEffectiveValue():CommonDTO<C_DTO, CD, FE>{
        val dto = singleRepository.getDTO()
        onValueUpdated("getEffectiveValue", dto)
        return dto
    }

    fun getDataModel(dataModel: DATA): CD{
        onValueUpdated("getDataModel", dataModel)
       return dataProperty.get(dataModel)
    }
    fun saveDataModel(dataModel:CD){
        onValueUpdated("saveDataModel", dataModel)
        dataProperty.set(thisDto.dataModel, dataModel)
    }

    fun attachForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>){
        onValueUpdated("attachForeignEntity", container.ownEntity)
        foreignEntity.set(container.ownEntity, thisDto.daoEntity)
    }

    fun getForeignEntity(entity: ENTITY): FE{
        val foreignEntity = entityProperty.get(entity)
        onValueUpdated("getForeignEntity", foreignEntity)
        return foreignEntity
    }

    override fun getForeignEntity(id: Long): FE?{
        onValueUpdated("getForeignEntityById", id)
       val childEntity = entityProperty.get(thisDto.daoEntity)
       return if(childEntity.id.value == id){
             childEntity
        }else{
            null
       }
    }
}

class OneToManyDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    dtoProvider : () -> CommonDTO<DTO, DATA, ENTITY>,
    private val childModel: DTOClass<F_DTO, FD, FE>,
    private val dataProperty: KProperty1<DATA, MutableList<FD>>,
    private val entitiesProperty: KProperty1<ENTITY, SizedIterable<FE>>,
    private val foreignEntity: KMutableProperty1<FE, ENTITY>,
) : RelationBindingDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, List<F_DTO>>(dtoProvider, "OneToManyDelegate", dataProperty.name)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity {



    val multipleRepository : MultipleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE> by lazy {
        thisDto.getOneToManyRepository(childModel)
    }
    override var onValueChanged: ((ObservableData)-> Unit)? = null


    fun <V: Any> onValueUpdated(methodName: String, value: V){
        val updateData =  RelationsUpdateParams(thisDto, methodName, propertyName, null, value, this)
        onValueChanged?.invoke(updateData)
    }

    override fun getEffectiveValue(): List<F_DTO> {
        val result = multipleRepository.getDTO().castListOrThrow<F_DTO, OperationsException>(childModel.config.registryRecord.derivedDTOClazz, null, 0)
        onValueUpdated("getEffectiveValue", result)
        return result
    }

    fun saveDataModels(foreignDataModels: List<FD>){
        onValueUpdated("saveDataModels" ,foreignDataModels)
        dataProperty.get(thisDto.dataModel).addAll(foreignDataModels)
    }

    fun getDataModels(dataModel: DATA): List<FD>{
        onValueUpdated("getDataModels", dataModel)
        return dataProperty.get(dataModel).toList()
    }

    fun getForeignEntities(entity: ENTITY): List<FE>{
        onValueUpdated("getForeignEntities", entity)
       return entitiesProperty.get(entity).toList()
    }

    override fun getForeignEntity(id: Long): FE?{
       onValueUpdated("getForeignEntity", id)
       return entitiesProperty.get(thisDto.daoEntity).firstOrNull { it.id.value == id }
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
        foreignEntity.set(container.ownEntity, thisDto.daoEntity)
    }
}

sealed class RelationBindingDelegate<DTO, DATA, ENTITY, C_DTO, FD, FE, R>(
    private val dtoProvider : () -> CommonDTO<DTO, DATA, ENTITY>,
    val componentName : String,
    val dataPropertyName : String
): ReadOnlyProperty<DTO, R>, TasksManaged, IdentifiableComponent
        where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity,
              C_DTO: ModelDTO,  FD: DataModel, FE : LongEntity
{

    override val type: ComponentType = ComponentType.RelationBindingDelegate
    override val qualifiedName : String  get() = "$componentName[]"
    private var propertyNameParameter: String = dataPropertyName
    val propertyName: String get()= propertyNameParameter

    protected val thisDto: CommonDTO<DTO, DATA, ENTITY> by lazy {
        val dto = dtoProvider()
        if(dto.tracker.config.observeRelationBindings){
            subscribeRelationUpdates(dto.tracker::relationPropertyUpdated)
        }
        dto
    }



    var valueUpdated : Boolean = false
    abstract var onValueChanged: ((ObservableData)-> Unit)?
    fun subscribeRelationUpdates(valueChanged: (ObservableData)-> Unit){
        onValueChanged = valueChanged
    }

    abstract fun getForeignEntity(id: Long):FE?

    abstract fun getEffectiveValue():R
    override fun getValue(thisRef: DTO, property: KProperty<*>): R{
        propertyNameParameter  = property.name
        return getEffectiveValue()
    }
}