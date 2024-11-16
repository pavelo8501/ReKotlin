package po.db.data_service.dto

import io.ktor.util.reflect.TypeInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.db.data_service.binder.ChildBindingContext
import po.db.data_service.binder.ChildClasses
import po.db.data_service.binder.DTOBinderClass
import po.db.data_service.binder.OneToManyBinding
import po.db.data_service.transportation.ServiceCreateOptions
import po.db.data_service.transportation.TableCreateMode


interface ModelDTOContext{
    companion object
    var id: Long
}

interface ParentDTOContext{
    companion object
    var id: Long
    fun bindings() : ChildClasses<*, *, *, *>

    fun updateChild()

}


open class EntityDTOClass<T : ModelDTOContext, E: LongEntity>(){

    private var entityDaoBindings :  DTOBinderClass<T,E>? = null
    private var entityDAOClass :  LongEntityClass<LongEntity>? = null

    private val dtoClasses = mutableMapOf<DTOClass<T,E>, DTOClass<T,E>>()

    val registeredTables : MutableList<IdTable<Long>> = mutableListOf()

    val onBeforeInit :  (()-> Unit)? = null

    private fun registerTable(table: IdTable<Long>){
        registeredTables.firstOrNull{ it == table }?:registeredTables.add(table)
    }

    fun registerDTOClass(dtoClass : DTOClass<T,E>){
        registerTable(dtoClass.primaryTable!!)
        dtoClass.parentEntityDTO = this
        dtoClasses.putIfAbsent(dtoClass, dtoClass)
    }

    fun isDTOClassRegistered(dtoClass : DTOClass<T,E>): Boolean{
        return dtoClasses.contains(dtoClass)
    }

    fun getEntityDAOById(id: Long): LongEntity{
        if(entityDAOClass == null) throw Exception("Entity DAO is not initialized")
        return entityDAOClass!!.findById(id) ?: throw Exception("Entity with id $id not found")
    }

    fun setEntityDAO(daoEntity: E, dtoCompanion: DTOClass<T,E>){
        dtoClasses.contains(dtoCompanion).let {
            if(it){
                dtoClasses[dtoCompanion]!!.daoEntity = daoEntity
            }
        }
    }

    fun nowDateTime(): LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun beforeInit(){
        onBeforeInit?.invoke()
        dtoClasses.forEach {
            it.value.beforeInit()
        }
    }
}

//Companion for user defined DTO classes
open class DTOClass<T: ModelDTOContext, E : LongEntity>(val propertyBinder : DTOBinderClass<T, E>){

   lateinit var thisTypeInfo :  TypeInfo

   var daoEntity: E? = null
   var parentEntityDTO = EntityDTOClass<T,E>()
   var daoEntityCompanion: LongEntityClass<LongEntity>? = null

   val onInitTasks : MutableList<ServiceCreateOptions<T,E>> = mutableListOf()
   var primaryTable : IdTable<Long>? = null

   fun initialSetup(typeInfo :TypeInfo, serviceCreateOption : ServiceCreateOptions<T,E> ?= null){
        thisTypeInfo = typeInfo
        if(serviceCreateOption != null){
            onInitTasks.add(serviceCreateOption)
        }
    }

    fun beforeInit(){
        if(onInitTasks.isNotEmpty()){
            onInitTasks.forEach {
                when(it.createTable){
                    TableCreateMode.CREATE -> {
                        it.service?.createTable(primaryTable!!)
                    }
                    TableCreateMode.FORCE_RECREATE -> {
                        it.service?.dropTable(primaryTable!!)
                        it.service?.createTable(primaryTable!!)
                    }
                }
            }
        }
    }
    fun nowDateTime(): LocalDateTime{
       return EntityDTO.nowDateTime()
    }


}


abstract class EntityDTO<T: ModelDTOContext, E: LongEntity> : ModelDTOContext{

    companion object : EntityDTOClass<ModelDTOContext,LongEntity>()

    var dtoCompanion :  DTOClass<T,E>? = null
    var daoEntityCompanion: LongEntityClass<LongEntity>? = null

    var daoEntity: E? = null
        set(value) {
            field = value
            if(value != null){
                id = value.id.value
                setEntityDAO(value, dtoCompanion as DTOClass<ModelDTOContext, LongEntity>)
            }
        }

    var initialized: Boolean = false

    var propertyBinder : DTOBinderClass<T,E>? = null

    var hasChild : Boolean = false
    var childBindings: ChildClasses<*,*,*,*>? = null


    constructor(){
        this.initialized = false
    }
    constructor(dtoClass :  DTOClass<T, E>, entityCompanion: LongEntityClass<LongEntity>) : this() {
       this.daoEntityCompanion = entityCompanion
       dtoCompanion = dtoClass
       if(dtoCompanion == null) throw Exception("DTO class is not initialized")
       if(daoEntityCompanion == null) throw Exception("Entity DAO LongEntityClass<LongEntity> is not initialized")
       dtoCompanion!!.daoEntityCompanion = daoEntityCompanion
       dtoCompanion!!.primaryTable =  dtoCompanion!!.daoEntityCompanion!!.table

       propertyBinder = dtoCompanion!!.propertyBinder
       propertyBinder!!.setModelObject(this as T)
       registerDTOClass(dtoCompanion as DTOClass<ModelDTOContext, LongEntity>)
       initChild()
       beforeInit()
       this.initialized = true
    }

   // var bindings :  (ChildBindingContext.() -> Unit )? = null
    fun initChild(){
       if(this is ParentDTOContext){
           val bindings = (this as ParentDTOContext).bindings()
           bindings.bindingList.forEach{
               if(!isDTOClassRegistered(it.childEntity as DTOClass<ModelDTOContext, LongEntity>)){
                   it.childEntity.primaryTable = it.childEntity.daoEntityCompanion?.table
                   registerDTOClass(it.childEntity as DTOClass<ModelDTOContext, LongEntity>)
               }
           }
           childBindings = bindings
           hasChild = true
       }
    }

    fun update(){
        if(daoEntityCompanion == null) throw Exception("Entity DAO is not initialized")
        val propBinder = this.propertyBinder
        if(this.id == 0L){
          val newEntity =  daoEntityCompanion!!.new {
              @Suppress("UNCHECKED_CAST")
              val newDAOEntity = propBinder!!.updateProperties(this as E, true)
              newDAOEntity
            }
          @Suppress("UNCHECKED_CAST")
          daoEntity = newEntity as E
          if(hasChild){
              childBindings?.update()
          }
        }else{
            if(daoEntity != null){
                 propBinder!!.updateProperties(daoEntity!!)
            }else{
                @Suppress("UNCHECKED_CAST")
                daoEntity =  propBinder!!.updateProperties(getEntityDAOById(this.id) as E)
            }
        }
    }
}