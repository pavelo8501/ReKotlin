package po.db.data_service.dto

import com.mysql.cj.conf.StringProperty
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DataTransferObjectsPropertyBinder
import po.db.data_service.binder.PropertyBinding


class DTObConfigContext(
) {
  //  lateinit var  binder  : DataTransferObjectsPropertyBinder<DATA_MODEL, ENTITY, *>



    fun <DATA_MODEL: MarkerInterface, ENTITY : LongEntity, TYPE>cretePropertyBindings(vararg props: PropertyBinding<DATA_MODEL, ENTITY, TYPE>): DataTransferObjectsPropertyBinder<DATA_MODEL, ENTITY, TYPE> {
        return DataTransferObjectsPropertyBinder<DATA_MODEL, ENTITY, TYPE>(*props)
    }

}

data class ModelEntityPairContainer<DATA_MODEL: MarkerInterface, ENTITY : LongEntity>(
    val uniqueKey : String,
    val dataModel : DataTransferObjectsParent<DATA_MODEL>,
    val entityModel : LongEntityClass<ENTITY>
)

abstract  class DataTransferObjectsParent<DATA_MODEL: MarkerInterface>(): MarkerInterface{
     val  dataModelObject : DATA_MODEL? = null
     protected abstract val id : Long

     companion object{
        fun associateWithDAOEntity(daoEntity : MarkerInterface){

        }

        fun <DATA_MODEL: MarkerInterface, ENTITY : LongEntity>createModelEntityPair(
            key:String,
            dataModel :  DataTransferObjectsParent<DATA_MODEL>,
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

    protected fun companionObject():DataTransferObjectsParent<DATA_MODEL>{
        return this
    }
}



