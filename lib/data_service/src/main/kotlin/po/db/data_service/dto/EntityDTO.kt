package po.db.data_service.dto

import io.ktor.util.reflect.TypeInfo
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.binder.*




interface ModelDTOContext{
    var id: Long
}

interface ParentDTOContext : ModelDTOContext{
    companion object

    fun bindings() : ChildClasses<*, *, *, *>

    fun updateChild()
}


class MyModelDTO(override var id: Long) : ModelDTOContext

class MyEntity(id: EntityID<Long>) : LongEntity(id)

//class MyDTO(data: MyModelDTO, entity: MyEntity) : CommonDTO<MyModelDTO, MyEntity>(data, entity){
//
//    override var id: Long  = data.id
//    override val entityClass: LongEntityClass<MyEntity>
//
//}

//Child class companion
//open class EntityDTOClass<T : ModelDTOContext, E: LongEntity>(){
//    private var entityDaoBindings :  DTOBinderClass<T,E>? = null
//
//    private var entityDAOClass :  LongEntityClass<LongEntity>? = null
//
//    private val dtoClasses = mutableMapOf<DTOClass<T,E>, DTOClass<T,E>>()
//
//    val registeredTables : MutableList<IdTable<Long>> = mutableListOf()
//
//    val onBeforeInit :  (()-> Unit)? = null
//
//    private fun registerTable(table: IdTable<Long>){
//        registeredTables.firstOrNull{ it == table }?:registeredTables.add(table)
//    }
//
//    fun registerDTOClass(dtoClass : DTOClass<T,E>){
//        registerTable(dtoClass.primaryTable!!)
//        dtoClass.parentEntityDTO = this
//        dtoClasses.putIfAbsent(dtoClass, dtoClass)
//    }
//
//    fun isDTOClassRegistered(dtoClass : DTOClass<T,E>): Boolean{
//        return dtoClasses.contains(dtoClass)
//    }
//
//    fun getEntityDAOById(id: Long): LongEntity{
//        if(entityDAOClass == null) throw Exception("Entity DAO is not initialized")
//        return entityDAOClass!!.findById(id) ?: throw Exception("Entity with id $id not found")
//    }
//
//    fun setEntityDAO(daoEntity: E, dtoCompanion: DTOClass<T, E>){
//        dtoClasses.contains(dtoCompanion).let {
//            if(it){
//                dtoClasses[dtoCompanion]!!.daoEntity = daoEntity
//            }
//        }
//    }
//
//    fun nowDateTime(): LocalDateTime{
//        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
//    }
//
//    fun beforeInit(){
//        onBeforeInit?.invoke()
//        dtoClasses.forEach {
//            it.value.beforeInit()
//        }
//    }
//}

interface DAOClassContext<ENTITY>{
    val daoEntityClass: LongEntityClass<LongEntity>
}

interface CompleteDtoContext

class CompleteDto<DATA: ModelDTOContext, ENTITY : LongEntity>(val data: CommonDTO<DATA, ENTITY>, val entity: LongEntity){

}

//Companion for user defined DTO classes
open class DTOClass<DTO,  ENTITY>(
    val propertyBinder: DTOBinder<DTO, ENTITY>
) where DTO : CommonDTO<*, ENTITY>, ENTITY : LongEntity

{

    fun process(dto: DTO) {
        propertyBinder.bind(dto, dto.entityDAO)
    }

   lateinit var thisTypeInfo :  TypeInfo

   var daoEntity: ENTITY? = null
   var daoEntityCompanion: LongEntityClass<LongEntity>? = null
//
//   val onInitTasks : MutableList<ServiceCreateOptions<T,E>> = mutableListOf()
//   var primaryTable : IdTable<Long>? = null
//
//   fun initialSetup(typeInfo :TypeInfo, serviceCreateOption : ServiceCreateOptions<T,E> ?= null){
//        thisTypeInfo = typeInfo
//        if(serviceCreateOption != null){
//            onInitTasks.add(serviceCreateOption)
//        }
//    }
//
//    fun beforeInit(){
//        if(onInitTasks.isNotEmpty()){
//            onInitTasks.forEach {
//                when(it.createTable){
//                    TableCreateMode.CREATE -> {
//                        it.service?.createTable(primaryTable!!)
//                    }
//                    TableCreateMode.FORCE_RECREATE -> {
//                        it.service?.dropTable(primaryTable!!)
//                        it.service?.createTable(primaryTable!!)
//                    }
//                }
//            }
//        }
//    }
//    fun nowDateTime(): LocalDateTime{
//       return EntityDTO.nowDateTime()
//    }
}


//
//open class CommonDTOClass<DTO, ENTITY> (
//    val propertyBinder : DTOBinder<ENTITY>) where DTO : CommonDTO<*, ENTITY>, ENTITY : LongEntity
//{
//    //T::class.java.simpleName
//   private  var primaryTable : IdTable<Long>? = null
//   private  val onInitTasks : MutableList<ServiceCreateOptions<DTO,ENTITY>> = mutableListOf()
//
//    fun beforeInit(){
//        if(onInitTasks.isNotEmpty()){
//            onInitTasks.forEach {
//                when(it.createTable){
//                    TableCreateMode.CREATE -> {
//                        it.service?.createTable(primaryTable!!)
//                    }
//                    TableCreateMode.FORCE_RECREATE -> {
//                        it.service?.dropTable(primaryTable!!)
//                        it.service?.createTable(primaryTable!!)
//                    }
//                }
//            }
//        }
//    }
//


class CommonDTOClass<DATA, ENTITY>(
    val propertyBinder: DTOBinder<DTO, ENTITY>
    ) where
DTO : CommonDTO<*, ENTITY>,
ENTITY : LongEntity {
    fun process(dto: DTO) {
        propertyBinder.bind(dto, dto.entity)
    }
}

abstract class CommonDTO<DATA : ModelDTOContext, ENTITY : LongEntity>() {

    protected abstract val entityClass: LongEntityClass<ENTITY>
    // private val binder: DTOBinder<T, E> by lazy { createBinder() }

    // var dtoCompanion :  DTOClass<ENTITY>? = null
    var initialized: Boolean = false
    private var binder: DTOBinder<ENTITY>? = null

    var entityDAO: ENTITY? = null
        private set

    fun setBinder(binder: DTOBinder<ENTITY>): DTOBinder<ENTITY> {
        // this.binder = binder
        return binder
    }

   // constructor() {}
    constructor(dtoClass : DTOClass<ENTITY> , entityDAO: ENTITY) : this() {
        // this.binder = binder
         this.entityDAO = entityDAO
    }
}


//abstract class EntityDTO<T: ModelDTOContext, E: LongEntity>{
//
//    //companion object : EntityDTOClass<ModelDTOContext,LongEntity>()
//    abstract var id : Long;
//
//    var dtoCompanion :  DTOClass<T, E>? = null
//    var daoEntityCompanion: LongEntityClass<E>? = null
//
//    var daoEntity: E? = null
//        set(value) {
//            field = value
//            if(value != null){
//                id = value.id.value
//                val a = this
//               // setEntityDAO(value, dtoCompanion )
//                val stop = 10
//            }
//        }
//
//    var initialized: Boolean = false
//
//    var propertyBinder : DTOBinder<E>? = null
//
//    var hasChild : Boolean = false
//    var childBindings: ChildClasses<*,*,*,*>? = null
//
//
//    constructor(){
//        this.initialized = false
//    }
//    constructor(dtoClass :  DTOClass<T, E>, entityCompanion: LongEntityClass<E>) : this() {
//
//        this.daoEntityCompanion = entityCompanion
//       dtoCompanion = dtoClass
//       if(dtoCompanion == null) throw Exception("DTO class is not initialized")
//       if(daoEntityCompanion == null) throw Exception("Entity DAO LongEntityClass<LongEntity> is not initialized")
//       dtoCompanion!!.daoEntityCompanion = daoEntityCompanion
//       dtoCompanion!!.primaryTable =  dtoCompanion!!.daoEntityCompanion!!.table
//
//       propertyBinder = dtoCompanion!!.propertyBinder
//       propertyBinder!!.setModelObject(this as T)
//        @Suppress("UNCHECKED_CAST")
//       registerDTOClass(dtoCompanion as DTOClass<ModelDTOContext, LongEntity>)
//       initChild()
//       beforeInit()
//       this.initialized = true
//    }
//
//   // var bindings :  (ChildBindingContext.() -> Unit )? = null
//    fun initChild(){
//       if(this is ParentDTOContext){
//           val bindings = (this as ParentDTOContext).bindings()
//           bindings.bindingList.forEach{
//               @Suppress("UNCHECKED_CAST")
//               if(!isDTOClassRegistered(it.childEntity as DTOClass<ModelDTOContext, LongEntity>)){
//                   it.childEntity.primaryTable = it.childEntity.daoEntityCompanion?.table
//                   registerDTOClass(it.childEntity as DTOClass<ModelDTOContext, LongEntity>)
//               }
//           }
//           childBindings = bindings
//           hasChild = true
//       }
//    }
//
//    fun update(){
//        if(daoEntityCompanion == null) throw Exception("Entity DAO is not initialized")
//        val propBinder = this.propertyBinder
//        if(this.id == 0L){
//          val newEntity =  daoEntityCompanion!!.new {
//              @Suppress("UNCHECKED_CAST")
//              val newDAOEntity = propBinder!!.updateProperties(this as E, true)
//              newDAOEntity
//            }
//          @Suppress("UNCHECKED_CAST")
//          daoEntity = newEntity as E
//          if(hasChild){
//              childBindings?.update()
//          }
//        }else{
//            if(daoEntity != null){
//                 propBinder!!.updateProperties(daoEntity!!)
//            }else{
//                @Suppress("UNCHECKED_CAST")
//                daoEntity =  propBinder!!.updateProperties(getEntityDAOById(this.id) as E)
//            }
//        }
//    }
