package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.DTOExecutionContext
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.interfaces.ForeignDelegateInterface
import po.exposify.dto.components.createDTOContext
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInit
import po.exposify.extensions.getOrOperations
import po.misc.containers.LazyBackingContainer
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.data.SmartLazy
import po.misc.context.asIdentity
import po.misc.functions.subscribers.subscribe
import po.misc.types.TypeData
import po.misc.types.containers.Multiple
import po.misc.types.containers.Single
import po.misc.types.containers.SingleOrList
import po.misc.types.containers.updatable.ActionValue
import po.misc.types.filterByType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class RelationDelegate<DTO, D, E, F, FD, FE>(
    protected val hostingDTO : CommonDTO<DTO, D, E>,
    val foreignClass: DTOClass<F, FD, FE>,
    delegateName: String
): DelegateInterface<DTO, D, E>, ForeignDelegateInterface, CTX
        where DTO : ModelDTO, D : DataModel,  E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity
{

    override var status: DelegateStatus = DelegateStatus.Created
    abstract val cardinality : Cardinality

    override val identity: CTXIdentity<RelationDelegate<DTO, D, E, F, FD, FE>> = asIdentity()

    override val hostingClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    protected val tracker: DTOTracker<DTO, D, E> get() = hostingDTO.tracker
   // protected val commonType: CommonDTOType<DTO, D, E> get() = hostingDTO.commonType

    protected val dtoType: TypeData<DTO> = hostingDTO.commonType.dtoType
    protected val foreignDTOType: TypeData<F> = foreignClass.commonDTOType.dtoType

    protected val ownEntity:E get(){
        return hostingDTO.entityContainer.getValue(this)
    }
    protected val ownDataModel:D get(){
        return hostingDTO.dataContainer.getValue(this)
    }

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null
    private var propertyParameter : KProperty<F>? = null
    val property: KProperty<F> get() = propertyParameter.getOrOperations(this)
    val name : String by SmartLazy("Uninitialized"){ propertyParameter?.name }

    internal val childBindingHubs:List<BindingHub<F,FD, FE>> get(){
      return  getChildDTOList().map { it.hub }
    }
    abstract val childCommonDTOList: List<CommonDTO<F, FD, FE>>
    abstract val bindEntity: Boolean

//    private fun createDTOContext(){
//        foreignCommon.registerExecutionContext(hostingClass.commonDTOType, DTOExecutionContext(hostingClass, foreignCommon))
//    }

    abstract fun getChild(): SingleOrList<F>
    protected abstract fun save(common: CommonDTO<F, FD, FE>)
    protected abstract fun save(common: CommonDTO<F, FD, FE>, data: FD)
    override fun resolveProperty(property: KProperty<*>){
        if(propertyParameter == null){
            try {
                propertyParameter = property.castOrInit(this)
                identity.setNamePattern { property.name }
                hostingDTO.hub.registerRelationDelegate(this)

                hostingClass.onNewMember.subscribe(this, DTOBase.Events.NewHierarchyMember){
                    if(it === foreignClass ){
                        val context = DTOExecutionContext(foreignClass, hostingDTO)
                        hostingDTO.registerExecutionContext(foreignClass.commonDTOType, context)
                    }else{
                       throw OperationsException("DTOExecutionContext creation failed", ExceptionCode.METHOD_MISUSED, this)
                    }
                }
                onPropertyInitialized?.invoke(property)
            }catch (th: Throwable){
                throw th
            }
        }
    }
    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }
    abstract fun extractChildDataModels(data:D):List<FD>
    abstract fun attachChildDataModel()
    abstract fun extractChildEntities(entity:E):List<FE>
   // abstract fun assignForeign(entity:E, foreignEntity: FE)
    protected abstract fun getChildDTOList(): List<CommonDTO<F, FD, FE>>

    private fun getExistentDTO(id: Long): CommonDTO<F,FD, FE>?{
        if(id != 0L){
           return childCommonDTOList.firstOrNull { it.id == id }
        }
        return null
    }

    fun createForeignDTOS(data:D, ifExists:(F)-> CrudOperation){
        val childDataModels = extractChildDataModels(data)
        childDataModels.forEach {childData->
           val existent = getExistentDTO(childData.id)
            if(existent != null){
                val crud =  ifExists.invoke(existent.asDTO())
            }else{
                val newDto = foreignClass.newDTO()
               // val context = DTOExecutionContext(foreignClass, hostingDTO)
             //   hostingDTO.registerExecutionContext(foreignClass.commonDTOType, context)
                newDto.addTrackerInfo(CrudOperation.Create)
                save(newDto)
                newDto.hub.resolveHierarchy(childData, newDto.commonType.dataType)
            }
        }
    }

    fun createForeignDTOS(entity:E, ifExists:(F)-> CrudOperation){
        val childEntities = extractChildEntities(entity)
        childEntities.forEach { childEntity ->
            val existent = getExistentDTO(childEntity.id.value)
            if(existent != null){
                ifExists.invoke(existent.asDTO())
            }else{
                val newDto = foreignClass.newDTO()
                val dataModel = newDto.dtoFactory.createDataModel()
                save(newDto, dataModel)
                newDto.hub.resolveHierarchy(childEntity)
            }
        }
    }
}


class OneToManyDelegate<DTO, D, E, F, FD, FE>(
    dto : CommonDTO<DTO, D, E>,
    childClass: DTOClass<F, FD, FE>,
    val dataProperty: KProperty1<D, MutableList<FD>>,
    val entitiesProperty: KProperty1<E, SizedIterable<FE>>,
    val ownOnForeignEntityProperty: KMutableProperty1<FE, E>,
) : RelationDelegate<DTO, D, E, F, FD,  FE>(dto, childClass, "OneToManyDelegate")
        where DTO : ModelDTO, D : DataModel, E : LongEntity,
              F: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val cardinality : Cardinality = Cardinality.ONE_TO_MANY
    private val dtos : MutableList<CommonDTO<F, FD, FE>> = mutableListOf()

    override val childCommonDTOList: List<CommonDTO<F, FD, FE>> get(){
        return dtos.toList()
    }

    override var bindEntity: Boolean = true
    private val entityBinder: ActionValue<FE> = ActionValue(this){
        if(bindEntity){
            val data = it.getData()
            val childEntity = data.value
            ownOnForeignEntityProperty.set(childEntity, ownEntity)
            tracker.logDebug("Entity binding created", this)
        }

    }

    override fun getChild(): Multiple<F> {
        val castedList = filterByType(foreignClass.commonDTOType.dtoType, dtos)
        return Multiple(castedList)
    }
    override fun getChildDTOList(): List<CommonDTO<F, FD, FE>>{
        return dtos
    }

    override fun extractChildDataModels(data:D): List<FD> {
        return dataProperty.get(data)
    }

    override fun attachChildDataModel(){
        val childDataList = dataProperty.get(ownDataModel)
        dtos.forEach {
           val dataModel =  it.dataContainer.getValue(this)
           childDataList.add(dataModel)
        }
    }
    override fun extractChildEntities(entity: E): List<FE> {
        return entitiesProperty.get(entity).toList()
    }

    private fun dtoSave(common: CommonDTO<F, FD, FE>){
        common.cardinality = cardinality
        common.hub.assignParent(hostingDTO.asDTO(), dtoType,  entityBinder)
        dtos.add(common)
    }

    override fun save(common: CommonDTO<F, FD, FE>){
        bindEntity = true
        dtoSave(common)
    }
    override fun save(common: CommonDTO<F, FD, FE>, data:FD){
        bindEntity = false
        dtoSave(common)
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): OneToManyDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }
    operator fun getValue(thisRef: DTO, property: KProperty<*>): List<F> {
        return filterByType(foreignDTOType, dtos)
    }
}


class OneToOneDelegate<DTO, D, E, F, FD, FE>(
    dto : CommonDTO<DTO, D, E>,
    childClass: DTOClass<F, FD, FE>,
    val dataProperty: KMutableProperty1<D, FD>,
    val entityProperty: KMutableProperty1<E, FE>,
    val ownOnForeignEntityProperty: KMutableProperty1<FE, E>,
) : RelationDelegate<DTO, D, E, F, FD, FE>(dto, childClass, "OneToOneDelegate")
        where DTO : ModelDTO, D : DataModel, E : LongEntity,
              F: ModelDTO,  FD : DataModel, FE : LongEntity {

    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE

    private var childDTOBacking : CommonDTO<F, FD, FE>? = null
    internal val childDTO get() = childDTOBacking.getOrOperations(this)

    override val childCommonDTOList: List<CommonDTO<F, FD, FE>> get(){
        return childDTOBacking?.let {
            listOf(it)
        }?:emptyList()
    }

    override var bindEntity: Boolean = true
    private val entityBinder: ActionValue<FE> = ActionValue(this){
        if(bindEntity){
            val data = it.getData()
            val childEntity = data.value
            ownOnForeignEntityProperty.set(childEntity, ownEntity)
            tracker.logDebug("Entity binding created", this)
        }
    }

    override fun getChild(): Single<F> {
        return Single(childDTO.asDTO())
    }
    override fun getChildDTOList(): List<CommonDTO<F, FD, FE>> {
        return childDTOBacking?.let {
            listOf(it)
        }?:emptyList()
    }

    override fun extractChildDataModels(data: D): List<FD> {
        return listOf(dataProperty.get(data))
    }
    override fun attachChildDataModel(){
        childDTOBacking?.let {
            val childDataModel = it.dataContainer.getValue(this)
            dataProperty.set(ownDataModel, childDataModel)
        }
    }
    override fun extractChildEntities(entity: E): List<FE> {
        return listOf(entityProperty.get(entity))
    }

    private fun dtoSave(common: CommonDTO<F, FD, FE>){
        common.cardinality = cardinality
        common.hub.assignParent(hostingDTO.asDTO(), dtoType,  entityBinder)
        childDTOBacking = common
    }

    override fun save(common: CommonDTO<F, FD, FE>){
        bindEntity = true
        dtoSave(common)
    }
    override fun save(common: CommonDTO<F, FD, FE>, data:FD){
        bindEntity = false
        dtoSave(common)
    }

    operator fun getValue(thisRef:DTO, property: KProperty<*>): F {
        return childDTO.asDTO()
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): OneToOneDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }

}