package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.exceptions.ExceptionCodes.*
import po.db.data_service.exceptions.InitializationException


abstract class AbstractDTOModel<DATA_MODEL: DataModel, ENTITY : LongEntity>(): DTOEntityMarker<DATA_MODEL, ENTITY>{

    abstract override var id : Long
    override val dataModelClassName: String = ""

    private var _dataModel : DATA_MODEL? = null
    val dataModel : DATA_MODEL
        get(){
            return _dataModel?: throw InitializationException("dataModel uninitialized", NOT_INITIALIZED)
        }

    private var dtoModel : DTOClass<DATA_MODEL, ENTITY>? = null

    private var _entityDAO : ENTITY? = null
    val entityDAO : ENTITY
        get(): ENTITY {
            return _entityDAO?: throw InitializationException("Trying to access database daoEntity associated with ${this.dataModelClassName}", NOT_INITIALIZED)
        }

    override fun setEntityDAO(entity :ENTITY){
        _entityDAO = entity as ENTITY
        if(id != entity.id.value){
            id = entity.id.value

        }
    }

    fun asDataModel():DATA_MODEL{
        return dataModel
    }

    fun asDTOEntity(): AbstractDTOModel<DATA_MODEL , ENTITY>{
        return this
    }

    val initialized : Boolean
        get(){
            return _entityDAO != null
        }


//    val dtoConfig =  ModelDTOConfig<DATA_MODEL, ENTITY>()
//    abstract fun configuration(dataModelInit : (dataModel : DATA_MODEL) -> Unit )

    init {
//        this.configuration {
//            this._dataModel =  it
//        }
    }

    constructor(dataModelObject : DTOClass<DATA_MODEL, ENTITY>) : this(){
        dtoModel = dataModelObject
    }

    private val childClass: AbstractDTOModel<DATA_MODEL, ENTITY>
        get (){
            return this
        }
}



//abstract class EntityDTO<T: ModelDTOContext, E: LongEntity>{
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
//       this.daoEntityCompanion = entityCompanion
//       dtoCompanion = dtoClass
//       if(dtoCompanion == null) throw Exception("DTO class is not initialized")
//       if(daoEntityCompanion == null) throw Exception("Entity DAO LongEntityClass<LongEntity> is not initialized")
//       dtoCompanion!!.daoEntityCompanion = daoEntityCompanion
//       dtoCompanion!!.primaryTable =  dtoCompanion!!.daoEntityCompanion!!.table
//
//       propertyBinder = dtoCompanion!!.propertyBinder
//       propertyBinder!!.setModelObject(this as T)
//       @Suppress("UNCHECKED_CAST")
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
