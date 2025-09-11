package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dao.transaction.withTransactionIfNone
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.executioncontext.SplitLists
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.helpers.checkDtoId
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrInit
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.misc.collections.exactlyOneOrThrow
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.functions.registries.builders.require
import po.misc.functions.registries.builders.subscribe
import po.misc.types.castListOrManaged
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


/**
 * Here hostingDTO is Parent dto instance
 * dtoClass is own(child) to hosting dto  class
 */

sealed class RelationDelegate<DTO, D, E, F, FD, FE, R>(
    protected val hostingDTO : CommonDTO<DTO, D, E>,
    val dtoClass: DTOClass<F, FD, FE>
):TasksManaged where DTO : ModelDTO, D : DataModel,  E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {

    abstract val entityProperty: KProperty<*>
    var status: DelegateStatus = DelegateStatus.Created
    abstract val cardinality : Cardinality

    val hostingDTOClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass

    protected val abnormalState:ExceptionCode = ExceptionCode.ABNORMAL_STATE

    protected val commonDTOSBacking: MutableList<CommonDTO<F, FD, FE>> = mutableListOf()
    val commonDTOS : List<CommonDTO<F, FD, FE>> get() = commonDTOSBacking

    protected val wrongListSize: (listName: String, context: String)-> String ={listName, context->
        "$listName should contain exactly 1 record in $context"
    }
    protected val idNotSetMsg: (dto: CommonDTO<*, *, *>, context: String)-> String = {dto, context ->
        "Registering DTO : $dto with unassigned id in $context"
    }
    protected val dtoWithSameIdExistsMsg: (dto: CommonDTO<*, *, *>, context: String)-> String = {dto, context ->
        "DTO with same id: ${dto.id} already present in the collection in $context"
    }

    private var propertyBacking : KProperty<R>? = null
    val property: KProperty<R> get() = propertyBacking.getOrOperations(this)

    internal abstract val delegateName: String

    internal abstract val result:R

    protected abstract fun beforeRegistered()
    protected abstract fun getDataModels():List<FD>
    protected abstract fun getEntities(sourceEntity:E?):List<FE>
    protected abstract fun applyToDataModel(dtoList: List<CommonDTO<F, FD, FE>>)

    internal abstract fun applyDataModels()

    private fun checkIfAlreadyExists(
        common: CommonDTO<F, FD, FE>
    ): Boolean{
        val existentSameId = commonDTOSBacking.firstOrNull { it.id ==  common.id}
        if(existentSameId != null){
            warning(dtoWithSameIdExistsMsg(existentSameId, identifiedByName))
            return true
        }else{
            return false
        }
    }


    internal fun extractDataModels(): List<FD>{
       return  getDataModels()
    }

    internal fun extractEntities(sourceEntity:E? = null): List<FE>{
        return withTransactionIfNone(dtoClass.debugger, false){
            getEntities(sourceEntity)
        }
    }

    fun createDTOS(existentProvider:(CommonDTO<F, FD, FE>)-> Unit): List<CommonDTO<F, FD, FE>>{
       val newDTOList = mutableListOf<CommonDTO<F, FD, FE>>()
       val dataModels =  getDataModels()
        dataModels.forEach { dataModel->
            val existent = getExistentDTO(dataModel.id)
            if(existent != null){
                existentProvider.invoke(existent)
            }else{
               val commonDTO =  dtoClass.newDTO(dataModel)
                commonDTO.bindingHub.resolveAttachedForeign(this, dataModel)
                commonDTOSave(commonDTO)
                newDTOList.add(commonDTO)
            }
        }
        return newDTOList
    }

    fun createDTO(dataModel :FD): CommonDTO<F, FD, FE>{
        val commonDTO =  dtoClass.newDTO(dataModel)
        commonDTO.bindingHub.resolveAttachedForeign(this, dataModel)
        commonDTOSave(commonDTO)
        return commonDTO
    }

    fun createDTOS(
        parentEntity: E,
        existentProvider:((CommonDTO<F, FD, FE>)-> Unit)? = null
    ): List<CommonDTO<F, FD, FE>>{

        val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
        extractEntities(parentEntity).forEach {entity->
            val commonDTO =  dtoClass.newDTO()

            commonDTO.bindingHub.resolveAttachedForeign(this, entity)
            commonDTO.bindingHub.updateByEntity(entity)
            commonDTOSave(commonDTO)
            resultingList.add(commonDTO)
        }
        return resultingList
    }

    fun storeDTOS(
        commonDTOS: List<CommonDTO<F, FD, FE>>
    ):  List<CommonDTO<F, FD, FE>>{
        val existentList = mutableListOf<CommonDTO<F, FD, FE>>()

        commonDTOS.forEach {
           if(!checkIfAlreadyExists(it)){
               commonDTOSave(it)
           }else{
               existentList.add(it)
           }
        }
        applyToDataModel(existentList)
        return existentList
    }



    protected fun commonDTOSave(
        commonDTO: CommonDTO<F, FD, FE>
    ){
        commonDTO.cardinality = cardinality
        commonDTO.bindingHub.resolveParent(hostingDTO)
        commonDTOSBacking.add(commonDTO)
    }

    fun resolveProperty(
        property: KProperty<*>
    ){
        propertyBacking = property.castOrInit(this)
        beforeRegistered()
        hostingDTO.bindingHub.registerRelationDelegate(this)
    }

    fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    abstract fun onParentResolved(parentDTO: CommonDTO<F, FD, FE>)

    private fun getExistentDTO(id: Long): CommonDTO<F, FD, FE>?{
        if(id != 0L){
           return commonDTOS.firstOrNull { it.id == id }
        }
        return null
    }

    operator fun getValue(thisRef:DTO, property: KProperty<*>): R {
        return result
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): RelationDelegate<DTO, D, E, F, FD, FE, R> {
        resolveProperty(property)
        return this
    }

}


class OneToManyDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO : CommonDTO<DTO, D, E>,
    dtoClass: DTOClass<F, FD, FE>,
    val dataProperty: KProperty1<D, MutableList<FD>>,
    override val entityProperty: KProperty1<E, SizedIterable<FE>>
): RelationDelegate<DTO, D, E, F, FD, FE, List<F>>(hostingDTO, dtoClass)
        where DTO : ModelDTO, D : DataModel, E : LongEntity, F: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val identity: CTXIdentity<OneToManyDelegate<DTO, D, E, F, FD, FE>> = asIdentity()
    override val cardinality : Cardinality = Cardinality.ONE_TO_MANY

    override val delegateName: String get() =  "OneToManyDelegate[${property.name}]"
    override val result:List<F> get() = commonDTOS.castListOrManaged(dtoClass.commonDTOType.dtoType.kClass, this)

    override fun beforeRegistered() {
        require(hostingDTO.onIdResolved){
            identity.setNamePattern {"${it.identifiedByName} ${property.name} : ${hostingDTOClass.identifiedByName}" }
        }
    }

    override fun getDataModels(): List<FD> {
       val hostingDTODataModel =  hostingDTO.dataContainer.getValue(this)
       return dataProperty.get(hostingDTODataModel)
    }

    override fun getEntities(sourceEntity:E?): List<FE> {
        val parentEntity = sourceEntity?:hostingDTO.entityContainer.getValue(this)
        val entityList = entityProperty.get(parentEntity)
        return entityList.toList()
    }

    override fun applyToDataModel(dtoList: List<CommonDTO<F, FD, FE>>) {
        val existing = dataProperty.get(hostingDTO.dataContainer.getValue(this))
        val existingIds = existing.map { it.id }.toSet()
        val uniqueDtos = dtoList.filter { dto ->
            dto.id !in existingIds
        }
        val dataModels =  uniqueDtos.map { it.dataContainer.getValue(this) }
        existing.addAll(dataModels)
    }

    override fun applyDataModels() {
        val existing = dataProperty.get(hostingDTO.dataContainer.getValue(this))
        val existingIds = existing.map { it.id }.toSet()
        val uniqueDtos = commonDTOS.filter { dto ->
            dto.id !in existingIds
        }
        val dataModels =  uniqueDtos.map { it.dataContainer.getValue(this) }
        existing.addAll(dataModels)
    }

    override fun onParentResolved(parentDTO: CommonDTO<F, FD, FE>) {
        identity.setNamePattern {"${it.identifiedByName} ${property.name} : ${hostingDTOClass.identifiedByName}" }
    }
}


class OneToOneDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO : CommonDTO<DTO, D, E>,
    dtoClass: DTOClass<F, FD, FE>,
    val dataProperty: KMutableProperty1<D, FD?>,
    override val entityProperty: KMutableProperty1<E, FE>
) : RelationDelegate<DTO, D, E, F, FD, FE, F>(hostingDTO, dtoClass)
        where DTO : ModelDTO, D : DataModel, E : LongEntity, F: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val identity: CTXIdentity<OneToOneDelegate<DTO, D, E, F, FD, FE>> = asIdentity()
    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE


    override val delegateName: String get() = "OneToOneDelegate[${property.name}]"
    override val result:F get() = commonDTOS.first().asDTO()
    override fun beforeRegistered() {
        require(hostingDTO.onIdResolved){
            identity.setNamePattern {"${it.identifiedByName} ${property.name} : ${hostingDTOClass.identifiedByName}" }
        }
    }

    override fun getDataModels(): List<FD> {
        val hostingDTODataModel =  hostingDTO.dataContainer.getValue(this)
        val dataModel = dataProperty.get(hostingDTODataModel)
        return dataModel?.let {
            listOf(it)
        }?:run {
            emptyList()
        }
    }

    override fun getEntities(sourceEntity: E?): List<FE> {
        val parentEntity = sourceEntity?:hostingDTO.entityContainer.getValue(this)
        val result = entityProperty.get(parentEntity)
        return listOf(result)
    }

    override fun applyToDataModel(dtoList: List<CommonDTO<F, FD, FE>>) {
       val found =  dtoList.firstOrNull{ it.cardinality == cardinality }
        found?.let {
           val dataModel =  it.dataContainer.getValue(this)
            dataProperty.set(hostingDTO.dataContainer.getValue(this),  dataModel)
        }
    }

    override fun applyDataModels() {
        commonDTOS.firstOrNull()?.let {
           val dataModel =  it.dataContainer.getValue(this)
            dataProperty.set(hostingDTO.dataContainer.getValue(this), dataModel)
        }
    }

    internal val childDTO: CommonDTO<F, FD, FE> get() = commonDTOS.exactlyOneOrThrow {
        operationsException(wrongListSize("childDTOS", identifiedByName), abnormalState)
    }

    override fun onParentResolved(parentDTO: CommonDTO<F, FD, FE>) {
        identity.setNamePattern {"${it.identifiedByName} ${property.name} : ${hostingDTOClass.identifiedByName}" }
    }


}