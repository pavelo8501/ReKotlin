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
import po.exposify.dto.components.bindings.helpers.withDTOContext
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.createDTOContext
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInit
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.functions.registries.require
import po.misc.functions.registries.subscribe
import po.misc.types.TypeData
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class RelationDelegate<DTO, D, E, F, FD, FE>(
    protected val hostingDTO : CommonDTO<DTO, D, E>,
    val foreignClass: DTOClass<F, FD, FE>
): DelegateInterface<DTO, D, E>, TasksManaged
        where DTO : ModelDTO, D : DataModel,  E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity
{

    abstract val entityProperty: KProperty<*>
    override var status: DelegateStatus = DelegateStatus.Created
    abstract val cardinality : Cardinality

    override val hostingClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    protected val tracker: DTOTracker<DTO, D, E> get() = hostingDTO.tracker

    protected val dtoType: TypeData<DTO> = hostingDTO.commonType.dtoType

    protected val ownEntity:E get(){
        return hostingDTO.entityContainer.getValue(this)
    }
    protected val ownDataModel:D get(){
        return hostingDTO.dataContainer.getValue(this)
    }

    private var propertyBacking : KProperty<F>? = null
    val property: KProperty<F> get() = propertyBacking.getOrOperations(this)
    
    internal val childBindingHubs:List<BindingHub<F,FD, FE>> get(){
      return  childCommonDTOList.map { it.bindingHub }
    }
    abstract val childCommonDTOList: List<CommonDTO<F, FD, FE>>

    protected abstract fun save(common: CommonDTO<F, FD, FE>)
    protected abstract fun save(common: CommonDTO<F, FD, FE>, data: FD)
    protected abstract fun save(dto: F)

    override fun resolveProperty(property: KProperty<*>){
        propertyBacking = property.castOrInit(this)


       subscribe(hostingDTO.onIdResolved){
            when(this){
                is OneToManyDelegate->{
                    identity.setNamePattern {"${it.identifiedByName} ${property.name} : ${foreignClass.identifiedByName}" }
                }
                is OneToOneDelegate ->{
                    identity.setNamePattern {"${it.identifiedByName} ${property.name} : ${foreignClass.identifiedByName}" }
                }
            }
        }
        hostingDTO.bindingHub.registerRelationDelegate(this)

        hostingDTO.createDTOContext(foreignClass, foreignClass.commonDTOType)

        require(hostingClass.onNewMember){
            if(it === foreignClass){
                if(!hostingDTO.hasExecutionContext(foreignClass.commonDTOType)){
                    val context = DTOExecutionContext(foreignClass, hostingDTO)
                    hostingDTO.registerExecutionContext(foreignClass.commonDTOType, context)
                    notify("Reusing context")
                }else{
                    warning("Context already created")
                }
            }else{
                throw OperationsException("DTOExecutionContext creation failed", ExceptionCode.METHOD_MISUSED, this)
            }
        }

//        subscribe(hostingClass.onNewMember){
//            if(it === foreignClass ){
//                if(!hostingDTO.hasExecutionContext(foreignClass.commonDTOType)){
//                    val context = DTOExecutionContext(foreignClass, hostingDTO)
//                    hostingDTO.registerExecutionContext(foreignClass.commonDTOType, context)
//                }else{
//                    notify("Context already created", SeverityLevel.WARNING)
//                }
//            }else{
//                throw OperationsException("DTOExecutionContext creation failed", ExceptionCode.METHOD_MISUSED, this)
//            }
//        }
    }
    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }
    abstract fun extractChildDataModels(data:D):List<FD>
    abstract fun attachChildDataModel()
    abstract fun extractChildEntities(entity:E):List<FE>

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

             val dto =  hostingDTO.withDTOContext(foreignClass.commonDTOType){
                   val newDTO = dtoFactory.createDto(childData)
                    newDTO.addTrackerInfo(CrudOperation.Create)
                    newDTO
                }
                save(dto.asDTO())
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
                newDto.bindingHub.resolveHierarchy(childEntity)
            }
        }
    }
}


class OneToManyDelegate<DTO, D, E, F, FD, FE>(
    dto : CommonDTO<DTO, D, E>,
    childClass: DTOClass<F, FD, FE>,
    val dataProperty: KProperty1<D, MutableList<FD>>,
    override val entityProperty: KProperty1<E, SizedIterable<FE>>,
    val ownOnForeignEntityProperty: KMutableProperty1<FE, E>,
) : RelationDelegate<DTO, D, E, F, FD,  FE>(dto, childClass)
        where DTO : ModelDTO, D : DataModel, E : LongEntity,
              F: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val identity: CTXIdentity<OneToManyDelegate<DTO, D, E, F, FD, FE>> = asIdentity()
    override val cardinality : Cardinality = Cardinality.ONE_TO_MANY

    private val dtos : MutableList<F> = mutableListOf()

    override val childCommonDTOList: List<CommonDTO<F, FD, FE>> get(){
        return dtos.filterIsInstance<CommonDTO<F, FD, FE>>()
    }

    override fun extractChildDataModels(data:D): List<FD> {
        return dataProperty.get(data)
    }

    override fun attachChildDataModel(){
        val childDataList = dataProperty.get(ownDataModel)
        childCommonDTOList.forEach {
           val dataModel =  it.dataContainer.getValue(this)
           childDataList.add(dataModel)
        }
    }
    override fun extractChildEntities(entity: E): List<FE> {
        return entityProperty.get(entity).toList()
    }

    private fun commonDTOSave(common: CommonDTO<F, FD, FE>){
        common.cardinality = cardinality
        common.registerParentDTO(hostingDTO)
        dtos.add(common.asDTO())
    }


    override fun save(common: CommonDTO<F, FD, FE>){
        commonDTOSave(common)
    }
    override fun save(common: CommonDTO<F, FD, FE>, data:FD){
        commonDTOSave(common)
    }
    override fun save(dto: F){
        dtos.add(dto)
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): OneToManyDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }
    operator fun getValue(thisRef: DTO, property: KProperty<*>): List<F> {
        return dtos
    }
}


class OneToOneDelegate<DTO, D, E, F, FD, FE>(
    dto : CommonDTO<DTO, D, E>,
    childClass: DTOClass<F, FD, FE>,
    val dataProperty: KMutableProperty1<D, FD>,
    override val entityProperty: KMutableProperty1<E, FE>,
    val ownOnForeignEntityProperty: KMutableProperty1<FE, E>,
) : RelationDelegate<DTO, D, E, F, FD, FE>(dto, childClass)
        where DTO : ModelDTO, D : DataModel, E : LongEntity, F: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val identity: CTXIdentity<OneToOneDelegate<DTO, D, E, F, FD, FE>> = asIdentity()

    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE

    private var childDTOBacking : F? = null
    internal val childDTO: F  get() = childDTOBacking.getOrOperations(foreignClass.commonDTOType.dtoType.kClass, this)

    override val childCommonDTOList: List<CommonDTO<F, FD, FE>> get(){
        return childDTOBacking?.let {
            listOf(it.castOrOperations(this))
        }?:emptyList()
    }

    override fun extractChildDataModels(data: D): List<FD> {
        return listOf(dataProperty.get(data))
    }
    override fun attachChildDataModel(){
        childCommonDTOList.forEach {
            it.dataContainer.getValue(this)
        }
    }
    override fun extractChildEntities(entity: E): List<FE> {
        return listOf(entityProperty.get(entity))
    }

    private fun dtoSave(common: CommonDTO<F, FD, FE>){
        common.cardinality = cardinality
        common.registerParentDTO(hostingDTO)
        childDTOBacking = common.asDTO()
    }

    override fun save(common: CommonDTO<F, FD, FE>){
        dtoSave(common)
    }
    override fun save(common: CommonDTO<F, FD, FE>, data:FD){
        dtoSave(common)
    }

    override fun save(dto: F){
        childDTOBacking = dto
    }

    operator fun getValue(thisRef:DTO, property: KProperty<*>): F {
        return childDTO
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): OneToOneDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }

}