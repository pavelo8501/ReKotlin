package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.structure.ServiceContext


data class ModelEntityPairContainer<DATA_MODEL: DTOMarker, ENTITY : LongEntity>(
    val uniqueKey : String,
    val dataModel : AbstractDTOModel<DATA_MODEL, ENTITY>,
    val entityModel : LongEntityClass<ENTITY>
)

abstract class DTOClass<DATA_MODEL: DTOMarker, ENTITY : LongEntity>{

    private lateinit var serviceContext: ServiceContext<DATA_MODEL, ENTITY>

    fun passContext(context : ServiceContext<DATA_MODEL, ENTITY>){
        serviceContext = context
    }
   // var modelConfiguration: ModelDTOConfig<DATA_MODEL, *>? = null

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }
    fun sharedFunctionality(str: String){
        println(str)
        println(this::class.qualifiedName)
    }

    fun config(body: ModelDTOConfig<DATA_MODEL,ENTITY>.() ->  Unit) = serviceContext.config{
        body.invoke(this)
    }

    fun initModelDao(){

    }
}

abstract  class AbstractDTOModel<DATA_MODEL: DTOMarker, ENTITY : LongEntity>(private val  dataModelObject : DTOClass<DATA_MODEL, ENTITY>): DTOMarker{
    // val  dataModelObject : DATA_MODEL? = null
    protected abstract val id : Long


    private val childClass: AbstractDTOModel<DATA_MODEL, ENTITY>
        get (){
            return this
        }

    //private val configuration = DTObConfigContext()


    override val sysName : String = "Partner"


   init {

       println("Print Child Class")
       println(this.childClass::class.qualifiedName)
       dataModelObject.sharedFunctionality("Request from Parent through dataModelObject")
       println(dataModelObject::class.qualifiedName)
      // initializeChildConfiguration()
   }

    //protected abstract fun <T>dataTransferModelsConfiguration(body: DTObConfigContext, function: DTObConfigContext.() -> Unit)

   // private fun <T>config(conf: DTObConfigContext, statement: DTObConfigContext.() -> T): T =   statement.invoke(conf)

//    protected fun  <T>configureDataTransferObject(body : DTObConfigContext.() -> T): T = config(configuration){
//        body()
//    }
//
//    private fun initializeChildConfiguration(){
//        dataTransferModelsConfiguration<DTObConfigContext>(configuration){
//
//        }
//    }


}



