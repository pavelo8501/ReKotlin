package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DataTransferObjectsPropertyBinder
import po.db.data_service.binder.PropertyBinding


class DTObConfigContext(
) {
  //  lateinit var  binder  : DataTransferObjectsPropertyBinder<DATA_MODEL, ENTITY, *>

    fun <DATA_MODEL: DTOMarker, ENTITY : LongEntity, TYPE>cretePropertyBindings(vararg props: PropertyBinding<DATA_MODEL, ENTITY, TYPE>): DataTransferObjectsPropertyBinder<DATA_MODEL, ENTITY, TYPE> {
        return DataTransferObjectsPropertyBinder<DATA_MODEL, ENTITY, TYPE>(*props)
    }

}

data class ModelEntityPairContainer<DATA_MODEL: DTOMarker, ENTITY : LongEntity>(
    val uniqueKey : String,
    val dataModel : AbstractDTOModel<DATA_MODEL>,
    val entityModel : LongEntityClass<ENTITY>
)

abstract  class AbstractDTOModel<DATA_MODEL: DTOMarker>(): DTOMarker{
     val  dataModelObject : DATA_MODEL? = null
     protected abstract val id : Long

     companion object{
        fun associateWithDAOEntity(daoEntity : DTOMarker){

        }

        fun <DATA_MODEL: DTOMarker, ENTITY : LongEntity>createModelEntityPair(
            key:String,
            dataModel :  AbstractDTOModel<DATA_MODEL>,
            entity : LongEntityClass<ENTITY>
        ):ModelEntityPairContainer<DATA_MODEL, ENTITY>{
            return  ModelEntityPairContainer(key,dataModel,entity)
        }
     }

    private val configuration = DTObConfigContext()


    override val sysName : String = "Partner"

   init {
       initializeChildConfiguration()
   }

    protected abstract fun <T>dataTransferModelsConfiguration(body: DTObConfigContext, function: DTObConfigContext.() -> Unit)

    private fun <T>config(conf: DTObConfigContext, statement: DTObConfigContext.() -> T): T =   statement.invoke(conf)

    protected fun  <T>configureDataTransferObject(body : DTObConfigContext.() -> T): T = config(configuration){
        body()
    }

    private fun initializeChildConfiguration(){
        dataTransferModelsConfiguration<DTObConfigContext>(configuration){

        }
    }

    protected fun companionObject():AbstractDTOModel<DATA_MODEL>{
        return this
    }
}



