package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DTOPropertyBinder
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private


interface ModelDTOContext{
    var id: Long
}

abstract  class AbstractDTOModel<DATA_MODEL: DataModel, ENTITY : LongEntity>(): DTOEntityMarker{
    // val  dataModelObject : DATA_MODEL? = null

    abstract val id : Long
    abstract val dataModel : DATA_MODEL

    private var dtoModel : DTOClass<DATA_MODEL, ENTITY>? = null

    var entityDAO : ENTITY? = null

    val initialized : Boolean
        get(){
            return entityDAO != null
        }



    fun update() {
      //  binder.updateProperties(this,)
    }

    constructor(dataModelObject : DTOClass<DATA_MODEL, ENTITY>) : this(){
        dtoModel = dataModelObject
    }

    private val childClass: AbstractDTOModel<DATA_MODEL, ENTITY>
        get (){
            return this
        }
}


//abstract class CommonDTO<DATA: ModelDTOContext, ENTITY : LongEntity>() {
//
//    // private val binder: DTOBinder<T, E> by lazy { createBinder() }
//    abstract val id : Long
//    var daoClass : LongEntityClass<ENTITY>? = null
//    var dtoEntity : CommonDTO<DATA,ENTITY>? = null
//    var initialized: Boolean = false
//
// //   private var binder: DTOBinder : DTOClass ? = null
//
//    private var entityDAO: ENTITY? = null
//
//    init {
//        if(this::class.isCompanion){
//            dtoEntity = this::class.objectInstance
//        }

//        if(entityDTO::class.isCompanion){
//            dtoEntity = entityDTO::class.objectInstance
//        }
   // }
//    fun setBinder(binder: DTOBinder<CommonDTO<DATA, ENTITY>, DATA, ENTITY>) {
//        this.binder = binder
//    }

  //  init {

      //  entityDAO = entityClass.get(id)
   // }
//}


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
