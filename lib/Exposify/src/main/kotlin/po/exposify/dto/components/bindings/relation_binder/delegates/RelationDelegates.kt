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
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ForeignDataModels
import po.exposify.dto.models.ForeignEntities
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrInit
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.misc.collections.hasExactlyOne
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.registries.require
import po.misc.functions.registries.subscribe
import po.misc.types.TypeData
import po.misc.types.castListOrManaged
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

    protected val abnormalState:ExceptionCode = ExceptionCode.ABNORMAL_STATE

    protected val ownEntity:E get(){
        return hostingDTO.entityContainer.getValue(this)
    }
    protected val ownDataModel:D get(){
        return hostingDTO.dataContainer.getValue(this)
    }

    protected val childDTOSBacking: MutableList<CommonDTO<F, FD, FE>> = mutableListOf()
    val childDTOS : List<CommonDTO<F, FD, FE>> get() = childDTOSBacking

    protected val wrongListSize: (listName: String, context: String)-> String ={listName, context->
        "$listName should contain exactly 1 record in $context"
    }
    protected val idNotSetMsg: (dto: CommonDTO<*, *, *>, context: String)-> String = {dto, context ->
        "Registering DTO : $dto with unassigned id in $context"
    }

    protected val dtoWithSameIdExistsMsg: (dto: CommonDTO<*, *, *>, context: String)-> String = {dto, context ->
        "DTO with same id: ${dto.id} already present in the collection in $context"
    }


    private var propertyBacking : KProperty<F>? = null
    val property: KProperty<F> get() = propertyBacking.getOrOperations(this)
    
    internal val childBindingHubs:List<BindingHub<F,FD, FE>> get(){
      return  childDTOS.map { it.bindingHub }
    }

    private fun warnIfIdNotSet(common: CommonDTO<F, FD, FE>){
        if(common.id <= 0){
            warning(idNotSetMsg(common, identifiedByName))
        }
    }

    private fun doIfIdExists(common: CommonDTO<F, FD, FE>, action:(CommonDTO<F, FD, FE>)-> Unit){

        val existentSameId = childDTOSBacking.firstOrNull { it.id ==  common.id}
        if(existentSameId != null){
            warning(dtoWithSameIdExistsMsg(existentSameId, identifiedByName))
            action.invoke(existentSameId)
        }
    }



    protected fun commonDTOSave(common: CommonDTO<F, FD, FE>){
        common.cardinality = cardinality
        if(!common.parentDTO.isValueAvailable){
            common.registerParentDTO(hostingDTO)
        }else{
            warning("common.parentDTO.isValueAvailable ${common.parentDTO.isValueAvailable}")
        }
        warnIfIdNotSet(common)

        doIfIdExists(common){
            println(it)
        }
        val dataModel = common.dataContainer.getValue(this)
        attachDataModelIfMissing(dataModel)
        childDTOSBacking.add(common)
    }

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

       // hostingDTO.createDTOContext(foreignClass)

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
    }
    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    abstract fun extractChildDataModels(data:D):List<FD>
    abstract fun attachChildDataModel()
    abstract fun attachDataModelIfMissing(data:FD)
    abstract fun extractEntities(entity:E):List<FE>

    private fun getExistentDTO(id: Long): CommonDTO<F,FD, FE>?{
        if(id != 0L){
           return childDTOS.firstOrNull { it.id == id }
        }
        return null
    }

    fun attachDTOS(dtoList: List<CommonDTO<F, FD, FE>>){
        if(this is OneToManyDelegate){
            dtoList.forEach {
                commonDTOSave(it)
            }
        }
    }

    fun extractChildData(data:D): ForeignDataModels<F, FD, FE>?{
       val childData = extractChildDataModels(data)
        return if(childData.isNotEmpty()){
            ForeignDataModels(childData, foreignClass)
        }else{
            null
        }
    }

    fun extractChildEntities(entity: E): ForeignEntities<F, FD, FE>?{
        val childEntities = extractEntities(entity)
        return if(childEntities.isNotEmpty()){
            ForeignEntities(childEntities, foreignClass)
        }else{
            null
        }
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
                commonDTOSave(dto.castOrOperations(this))
            }
        }
    }

    fun createForeignDTOS(entity:E, ifExists:(F)-> CrudOperation){
        val childEntities = extractEntities(entity)
        childEntities.forEach { childEntity ->
            val existent = getExistentDTO(childEntity.id.value)
            if(existent != null){
                ifExists.invoke(existent.asDTO())
            }else{
                val newDto = foreignClass.newDTO()
                commonDTOSave(newDto)
                newDto.bindingHub.resolveHierarchy(childEntity)
            }
        }
    }

    fun createForeignDTO(data:FD, ifExists:(F)-> CrudOperation): CommonDTO<F,FD, FE>{
        var commonDTO = getExistentDTO(data.id)
        if(commonDTO != null){
            val crud =  ifExists.invoke(commonDTO.asDTO())
        }else{
            commonDTO = foreignClass.dtoConfiguration.dtoFactory.createDto(data)
            commonDTOSave(commonDTO)
        }
        return commonDTO
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

    override fun extractChildDataModels(data:D): List<FD> {
        return dataProperty.get(data)
    }

    override fun attachChildDataModel(){
        val childDataList = dataProperty.get(ownDataModel)
        childDTOS.forEach {
           val dataModel =  it.dataContainer.getValue(this)
           childDataList.add(dataModel)
        }
    }

    override fun attachDataModelIfMissing(data: FD) {
        val dataModes = dataProperty.get(hostingDTO.dataContainer.getValue(this))
        if(!dataModes.contains(data)){
            dataModes.add(data)
        }
    }

    override fun extractEntities(entity: E): List<FE> {
        return entityProperty.get(entity).toList()
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): OneToManyDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }
    operator fun getValue(thisRef: DTO, property: KProperty<*>): List<F> {
        return childDTOS.castListOrManaged(foreignClass.commonDTOType.dtoType.kClass, this)
    }
}


class OneToOneDelegate<DTO, D, E, F, FD, FE>(
    dto : CommonDTO<DTO, D, E>,
    childClass: DTOClass<F, FD, FE>,
    val dataProperty: KMutableProperty1<D, FD?>,
    override val entityProperty: KMutableProperty1<E, FE>,
    val ownOnForeignEntityProperty: KMutableProperty1<FE, E>,
) : RelationDelegate<DTO, D, E, F, FD, FE>(dto, childClass)
        where DTO : ModelDTO, D : DataModel, E : LongEntity, F: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val identity: CTXIdentity<OneToOneDelegate<DTO, D, E, F, FD, FE>> = asIdentity()
    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE

    internal val childDTO: CommonDTO<F, FD, FE>
        get() = childDTOS.hasExactlyOne {
                operationsException(wrongListSize("childDTOS", identifiedByName), abnormalState)
            }

    override fun extractChildDataModels(data: D): List<FD> {
        val dataModel = dataProperty.get(data)
        return if(dataModel != null){
            listOf(dataModel)
        }else{
            emptyList()
        }
    }
    override fun attachChildDataModel(){
        childDTOS.forEach {
            it.dataContainer.getValue(this)
        }
    }

    override fun attachDataModelIfMissing(data: FD) {
        val ownDataModel = hostingDTO.dataContainer.getValue(this)
        val dataModel = dataProperty.get(ownDataModel)
        if(dataModel == null){
            dataProperty.set(ownDataModel, data)
        }
    }

    override fun extractEntities(entity: E): List<FE> {
        return listOf(entityProperty.get(entity))
    }

    operator fun getValue(thisRef:DTO, property: KProperty<*>): F {
        return childDTO.asDTO()
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): OneToOneDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }

}