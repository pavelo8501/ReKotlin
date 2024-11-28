package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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

abstract class DTOClass<DATA_MODEL: DTOMarker>{

    fun sharedFunctionality(str: String){
        println(str)
        println(this::class.qualifiedName)
    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

}

abstract  class AbstractDTOModel<DATA_MODEL: DTOMarker>(val  dataModelObject : DTOClass<DATA_MODEL>): DTOMarker{
    // val  dataModelObject : DATA_MODEL? = null
    protected abstract val id : Long


    private val childClass: AbstractDTOModel<DATA_MODEL>
        get (){
            return this
        }

    private val configuration = DTObConfigContext()


    override val sysName : String = "Partner"

   init {

       println("Print Child Class")
       println(this.childClass::class.qualifiedName)
       dataModelObject.sharedFunctionality("Request from Parent through dataModelObject")
       println(dataModelObject::class.qualifiedName)
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


}



