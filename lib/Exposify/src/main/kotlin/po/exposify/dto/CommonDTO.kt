package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.binder.BindingKeyBase
import po.exposify.binder.PropertyBinder
import po.exposify.binder.UpdateMode
import po.exposify.classes.DTOClass
import po.exposify.classes.components.RepositoryBase
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.common.classes.ClassData
import po.exposify.common.classes.MapBuilder
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.DataModelContainer2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DtoClassRegistryItem
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.InitErrorCodes
import po.exposify.models.DTOInitStatus
import kotlin.reflect.KClass

//internal class HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
//    val sourceDtoClass: DTOClass<DATA, ENTITY>,
//    override val registryItem: DtoClassRegistryItem<DATA, ENTITY>,
//    override val dataContainer: DataModelContainer<DATA>,
//    override val dataModel: DATA,
//): DTOBase<DATA, ENTITY,  CHILD_DATA, CHILD_ENTITY>(sourceDtoClass)
//        where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{
//
//   // val repositories = mutableMapOf<BindingKeyBase, RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>()
//
//    var onUpdate: (suspend ()-> Unit)? = null
//    var onUpdateFromEntity: (suspend (ENTITY)-> Unit)? = null
//    var onDelete:(suspend ()-> Unit)? = null

//    val hasChild: Boolean
//        get(){return repositories.isNotEmpty()}

//  suspend fun initializeRepositories(entity: ENTITY){
//        repositories.values.forEach {
//          //  it.initialize(entity)
//        }
//    }

//   suspend  fun initializeRepositories(){
//        repositories.values.forEach {
//          //  it.initialize(dataContainer.dataModel)
//        }
//    }

//    suspend fun updateRootRepositories(){
//        if(!isSaved){
//            sourceModel.daoService.saveNew(this)?.let {
//                onUpdate?.invoke()
//            }
//        }else{
//            sourceModel.daoService.updateExistent(this).let{
//                onUpdate?.invoke()
//            }
//        }
//    }

//    private val onDeleteFnList = mutableListOf<
//            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,() -> Unit>>()
//
//    fun subscribeOnDelete(callback:  ()-> Unit){
//        onDeleteFnList.add(Pair(this, callback))
//    }

//    suspend fun deleteInRepositories() {
//        repositories.values.forEach { repository ->
//            repository.deleteAllRecursively()
//        }
//    }



//    fun getChildren():List<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>{
//        val result = mutableListOf<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>()
//        repositories.forEach { result.addAll(it.value.dtoList)}
//        return result
//    }

//}


abstract class CommonDTO<DATA, ENTITY>(
   dtoClass: DTOClass<DATA, ENTITY>
): DTOBase<DATA, ENTITY, DataModel, LongEntity>(dtoClass), ModelDTO
        where DATA: DataModel , ENTITY: LongEntity {

   abstract override val dataModel: DATA

    private var _regItem : DtoClassRegistryItem<DATA, ENTITY>? = null
    private val regItem : DtoClassRegistryItem<DATA, ENTITY>
        get(){
            return _regItem?:throw InitializationException("DtoClassRegistryItem uninitialized", InitErrorCodes.KEY_PARAM_UNINITIALIZED)
        }
    override val registryItem: DtoClassRegistryItem<DATA, ENTITY> by lazy { regItem }
    override val dataContainer: DataModelContainer2<DATA> =  DataModelContainer2(dataModel, dtoClass.factory.dataBlueprint as ClassBlueprint<DATA>, propertyBinder)


    internal var repositories =  MapBuilder<BindingKeyBase,  RepositoryBase<DATA, ENTITY, *, *>> ()

    fun compileDataModel():DATA{
        return dataContainer.dataModel
    }

//    suspend fun initializeRepositories(entity:ENTITY){
//        hostDTO?.initializeRepositories(entity)
//    }
//    suspend fun initializeRepositories(){
//        hostDTO?.initializeRepositories()
//    }
//    suspend  fun updateRepositories(){
//       hostDTO?.updateRootRepositories() ?:run { sourceModel.daoService.updateExistent(this) }
//    }
//    suspend fun deleteInRepositories(){
//        hostDTO?.let {
//            it.deleteInRepositories()
//            it.sourceModel.daoService.delete(it)
//        }
//    }

    internal constructor(dtoClass : DTOClass<DATA, ENTITY>, dataKClass:  KClass<DATA>,  entity : KClass<ENTITY>): this(dtoClass ){
        val regItem =  DtoClassRegistryItem<DATA,ENTITY>(this::class, dataKClass, entity)
        _regItem =  regItem
        selfRegistration(regItem)
    }

//    fun compileDataModel():DATA{
//        hostDTO?.compileDataModel()?.let {
//            return it
//        }?:run {
//            return this.injectedDataModel
//        }
//    }

//    fun <CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity> getChildren(
//        model: DTOClass<CHILD_DATA, CHILD_ENTITY>): List<CommonDTO<CHILD_DATA, CHILD_ENTITY>>{
//       val  filteredByClass  =  hostDTO?.getChildren()?.filter { it.sourceModel.sourceClass == model.sourceClass }
//        filteredByClass?.let {filtered->
//            return  (filtered as List<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>).toCommonDtoList()
//        }?:run{ return emptyList<CommonDTO<CHILD_DATA, CHILD_ENTITY>>()}
//    }

    companion object{
        internal val dtoRegistry: MapBuilder<String, DtoClassRegistryItem<*,*>> = MapBuilder<String, DtoClassRegistryItem<*,*>>()

        inline operator fun <reified DATA : DataModel, reified ENTITY : LongEntity>
                invoke(dtoClass: DTOClass<DATA, ENTITY>): CommonDTO<DATA, ENTITY> {
            return object : CommonDTO<DATA, ENTITY>(
                dtoClass =  dtoClass,
                DATA::class,
                ENTITY::class){
                abstract override val dataModel: DATA
            }
        }

       internal fun <DATA :DataModel, ENTITY: LongEntity> selfRegistration(regItem :  DtoClassRegistryItem<DATA, ENTITY>){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }

    }
}

sealed class DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   val dtoClass: DTOClass<DATA,ENTITY>
) where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    abstract val dataModel: DATA
    abstract val dataContainer : DataModelContainer2<DATA>

    val propertyBinder: PropertyBinder<DATA, ENTITY> =  dtoClass.conf.propertyBinder

    var onInitializationStatusChange : ((DTOBase<DATA, ENTITY, *, *>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    var id : Long
        get(){return dataContainer.dataModel.id}
        set(value) {dataContainer.dataModel.id = value}

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

   internal abstract val registryItem: DtoClassRegistryItem<DATA, ENTITY>

   fun initialize(): DTOClass<DATA, ENTITY> {
       dataContainer.attachBinder(propertyBinder)
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       return dtoClass
   }

    fun updateBinding(entity : ENTITY, updateMode: UpdateMode){
        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
        entityDAO = entity
        id =  entity.id.value
        initStatus = DTOInitStatus.INITIALIZED
    }

    fun updateBinding(dataModel: DATA, updateMode: UpdateMode){
        propertyBinder.update(dataModel, entityDAO, updateMode)
    }


}