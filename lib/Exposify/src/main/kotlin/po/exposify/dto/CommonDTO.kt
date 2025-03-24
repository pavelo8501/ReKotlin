package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.InternalApi
import po.exposify.binder.BindingKeyBase
import po.exposify.binder.PropertyBinder
import po.exposify.binder.UpdateMode
import po.exposify.classes.DTOClass
import po.exposify.classes.components.MultipleRepository
import po.exposify.classes.components.RepositoryBase
import po.exposify.classes.components.SingleRepository
import po.exposify.classes.interfaces.DataModel
import po.exposify.constructors.ConstructorBuilder
import po.exposify.constructors.DataModelBlueprint
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.functions.toCommonDtoList
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.models.DTOInitStatus
import kotlin.reflect.KClass

class HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    injectedDataModel: DATA
): DTOBase<DATA, ENTITY,  CHILD_DATA, CHILD_ENTITY>(injectedDataModel)
        where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    val repositories = mutableMapOf<BindingKeyBase, RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>()

    var onUpdate: (suspend ()-> Unit)? = null
    var onUpdateFromEntity: (suspend (ENTITY)-> Unit)? = null
    var onDelete:(suspend ()-> Unit)? = null

    val hasChild: Boolean
        get(){return repositories.isNotEmpty()}

  suspend fun initializeRepositories(entity: ENTITY){
        repositories.values.forEach {
            it.initialize(entity)
        }
    }

   suspend  fun initializeRepositories(){
        repositories.values.forEach {
            it.initialize(getInjectedModel())
        }
    }

    suspend fun updateRootRepositories(){
        if(!isSaved){
            sourceModel.daoService.saveNew(this)?.let {
                onUpdate?.invoke()
            }
        }else{
            sourceModel.daoService.updateExistent(this).let{
                onUpdate?.invoke()
            }
        }
    }

    private val onDeleteFnList = mutableListOf<
            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,() -> Unit>>()

    fun subscribeOnDelete(callback:  ()-> Unit){
        onDeleteFnList.add(Pair(this, callback))
    }

    suspend fun deleteInRepositories() {
        repositories.values.forEach { repository ->
            repository.deleteAllRecursively()
        }
    }

    fun compileDataModel():DATA{
        repositories.values.forEach {repo->
            repo.dtoList.forEach {
                it.compileDataModel()
                when (repo){
                    is MultipleRepository->{
                        repo.getSourceProperty().let {sourceProperty->
                              dataModelContainer.addToMutableProperty(sourceProperty.name, it.getInjectedModel())
                        }
                    }
                    is SingleRepository->{
                        repo.getSourceProperty().let {
                            sourceProperty->
                            dataModelContainer.setProperty(sourceProperty.name, it.getInjectedModel())
                        }
                    }
                }
            }
        }
        return injectedDataModel
    }

    fun getChildren():List<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>{
        val result = mutableListOf<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>()
        repositories.forEach { result.addAll(it.value.dtoList)}
        return result
    }

    companion object{
        fun <DATA : DataModel , ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>createHosted(
            dataModel : DATA,
            dtoModel: DTOClass<DATA, ENTITY>,
        ): HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
        {
            val hosted = HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(dataModel)
            hosted.initialize(dtoModel)
            return hosted
        }

        fun <DATA: DataModel, ENTITY: LongEntity, CHILD_DATA:DataModel, CHILD_ENTITY:LongEntity> copyAsCommonDTO(
            hosted: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
        ): CommonDTO<DATA, ENTITY> {
            return object : CommonDTO<DATA, ENTITY>(hosted.getInjectedModel()) {}.apply {
                this.sourceModel = hosted.sourceModel
                this.propertyBinder.setProperties(hosted.propertyBinder.getAllProperties())
                this.hostDTO = hosted
            }
        }
    }
}

class CommonDTOClassRegistry<DATA, ENTITY>(
    val dto:  KClass<CommonDTO<DATA, ENTITY>>,
    val data: KClass<DATA>,
    val entity: KClass<ENTITY>) where DATA: DataModel , ENTITY: LongEntity

abstract class CommonDTO<DATA, ENTITY>(
    injectedDataModel: DATA
): DTOBase<DATA, ENTITY, DataModel, LongEntity>(injectedDataModel), Cloneable
        where DATA: DataModel , ENTITY: LongEntity {

    var hostDTO  : HostDTO<DATA, ENTITY, *, *>? = null
    internal val typeKeyCombined: String get() = "${dataKClass.qualifiedName}:${entityKClass.qualifiedName}"
    internal val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    internal val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    private var _kClassPair : Pair<KClass<DATA>, KClass<ENTITY>>? = null
    internal val kClassPair : Pair<KClass<DATA>, KClass<ENTITY>>
        get(){return  _kClassPair?:throw OperationsException(
            "Data/Entity pair uninitialized",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val dataKClass: KClass<DATA> by lazy { kClassPair.first }
    val entityKClass: KClass<ENTITY> by lazy { kClassPair.second }

//    internal inline fun <reified DATA : DataModel, reified ENTITY : LongEntity>
//            CommonDTO<DATA, ENTITY>.safeCast(): CommonDTO<DATA, ENTITY>? {
//
//        return if (this.kClassPair.first  == DATA::class && this.kClassPair.second == ENTITY::class)
//            this as CommonDTO<DATA, ENTITY>
//        else null
//    }

    suspend fun initializeRepositories(entity:ENTITY){
        hostDTO?.initializeRepositories(entity)
    }
    suspend fun initializeRepositories(){
        hostDTO?.initializeRepositories()
    }
    suspend  fun updateRepositories(){
       hostDTO?.updateRootRepositories() ?:run { sourceModel.daoService.updateExistent(this) }
    }
    suspend fun deleteInRepositories(){
        hostDTO?.let {
            it.deleteInRepositories()
            it.sourceModel.daoService.delete(it)
        }
    }

    internal constructor(injectedDataModel: DATA, data:  KClass<DATA>,  entity : KClass<ENTITY>): this(injectedDataModel){
        _kClassPair = Pair(data, entity)
        selfRegistration(this)
    }

    fun compileDataModel():DATA{
        hostDTO?.compileDataModel()?.let {
            return it
        }?:run {
            return this.injectedDataModel
        }
    }

    fun <CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity> getChildren(
        model: DTOClass<CHILD_DATA, CHILD_ENTITY>): List<CommonDTO<CHILD_DATA, CHILD_ENTITY>>{
       val  filteredByClass  =  hostDTO?.getChildren()?.filter { it.sourceModel.sourceClass == model.sourceClass }
        filteredByClass?.let {filtered->
            return  (filtered as List<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>).toCommonDtoList()
        }?:run{ return emptyList<CommonDTO<CHILD_DATA, CHILD_ENTITY>>()}
    }

    companion object{

        var dtoRegistry: Map<KClass<out CommonDTO<*, *>>, CommonDTO<*, *>> = emptyMap()

        inline operator fun <reified DATA : DataModel, reified ENTITY : LongEntity>
                invoke(injectedDataModel: DATA): CommonDTO<DATA, ENTITY> {
            return object : CommonDTO<DATA, ENTITY>(
                injectedDataModel = injectedDataModel, DATA::class, ENTITY::class
            ) {  }
        }

        fun <DATA :DataModel, ENTITY: LongEntity> selfRegistration(dto :  CommonDTO<DATA, ENTITY>){
            dtoRegistry = mapOf(dto::class to dto)
        }

        fun <DATA: DataModel, ENTITY: LongEntity, CHILD_DATA:DataModel, CHILD_ENTITY:LongEntity> copyAsCommonDTO(
            hosted: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
        ): CommonDTO<DATA, ENTITY> {
            return object : CommonDTO<DATA, ENTITY>(hosted.getInjectedModel()) {}.apply {
                 this.sourceModel = hosted.sourceModel
                 this.propertyBinder.setProperties(hosted.propertyBinder.getAllProperties())
                this.hostDTO = hosted
            }
        }
    }
}

sealed class DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   protected  val injectedDataModel : DATA
) where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    var onInitializationStatusChange : ((DTOBase<DATA, ENTITY, *, *>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    var id : Long
        get(){return injectedDataModel.id}
        set(value) {injectedDataModel.id = value}

   private  var _sourceModel: DTOClass<DATA, ENTITY>? = null
   var sourceModel : DTOClass<DATA, ENTITY>
        get(){return  _sourceModel?: throw OperationsException(
            "Trying to access dtoModel property of DTOContainerBase id :$id while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }
        set(value){ _sourceModel = value}

   private var _entityDAO : ENTITY? = null
   var entityDAO : ENTITY
        set(value){  _entityDAO = value }
        get(){return  _entityDAO?:throw OperationsException(
            "Entity uninitialized",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val isSaved : Boolean
        get(){
            return id != 0L
        }

    val dataModelContainer = DataModelContainer(injectedDataModel)

    val propertyBinder: PropertyBinder<DATA,ENTITY> by lazy { initialize(sourceModel) }

    fun getInjectedModel(): DATA =  this.injectedDataModel

    fun initialize(model: DTOClass<DATA, ENTITY>): PropertyBinder<DATA, ENTITY> {
       sourceModel = model
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       return model.conf.propertyBinder
   }

    fun update(entity : ENTITY, mode: UpdateMode){
        propertyBinder.update(injectedDataModel, entity, mode)
        entityDAO = entity
        if(mode == UpdateMode.ENTITY_TO_MODEL || mode == UpdateMode.ENTITY_TO_MODEL_FORCED){
            id =  entity.id.value
        }
        initStatus = DTOInitStatus.INITIALIZED
    }

    fun update(dataModel: DATA, mode: UpdateMode){
        propertyBinder.update(dataModel, entityDAO, mode)
    }


}