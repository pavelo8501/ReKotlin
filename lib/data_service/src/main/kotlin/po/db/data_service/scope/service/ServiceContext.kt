package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.dto.components.ContextState
import po.db.data_service.dto.components.DTOComponents
import po.db.data_service.models.AbstractDTOModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.connection.ConnectionContext
import po.db.data_service.scope.service.controls.ServiceMetadata
import kotlin.reflect.KClass

class ServiceContext<DATA_MODEL : DataModel, ENTITY : LongEntity>(
    val name : String,
    private val rootDtoModel : DTOClass<DATA_MODEL, ENTITY>,
    private val dbConnection: Database,
    private val connectionContext : ConnectionContext,
) {
    companion object : ConstructorBuilder()

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    var state : ContextState = ContextState.UNINITIALIZED
        set(value){
            if(field != value){
                field = value
                when(field){
                    ContextState.INITIALIZED->{
                     //   initialize(metaData)
                    }
                    else->{}
                }
            }
        }

    private var _metaData : ServiceMetadata<DATA_MODEL, ENTITY>? = null
    private val metaData : ServiceMetadata<DATA_MODEL, ENTITY>
        get(){
            return _metaData?: throw InitializationException("Service $name failed to initialize properly. MetaData missing", ExceptionCodes.NOT_INITIALIZED)
        }
    fun setServiceMetadata(meta : ServiceMetadata<DATA_MODEL, ENTITY>){
        _metaData  = meta
        state = ContextState.INITIALIZED
    }


    var dataModelClass: KClass<DATA_MODEL>? = null

    private var modelConfiguration = DTOConfig<DATA_MODEL,ENTITY>()
    private  fun <T>configuration(conf: DTOConfig<DATA_MODEL,ENTITY>, statement: DTOConfig<DATA_MODEL, ENTITY>.() -> T): T =   statement.invoke(conf)
    fun  <T>config(serviceBody: DTOConfig<DATA_MODEL, ENTITY>.() -> T): T = configuration(modelConfiguration) {
        serviceBody()
    }

    init {

    }

    private fun handleDtoInitialization(outer : DTOContext<DATA_MODEL, ENTITY>, inner : DTOComponents<DATA_MODEL, ENTITY> ){
        val modelBlueprint = getConstructorBlueprint(outer.dataModelClass)
        metaData.addModelBlueprint(outer.dataModelClass, modelBlueprint)
        val dtoBlueprint =  getConstructorBlueprint(outer.dtoModelClass)
        metaData.addDtoBlueprint(outer.dtoModelClass, dtoBlueprint)
        inner.setBlueprints(dtoBlueprint, modelBlueprint)
    }


    fun <T : DTOClass<DATA_MODEL, ENTITY>> T.update(single: AbstractDTOModel<DATA_MODEL, ENTITY>, block: T.() -> Unit): Unit {
      // this@ServiceContext.initDTO(single)
        this.block()
    }

    fun <DTO : DTOClass<DATA_MODEL, ENTITY>> DTO.update(list : List<AbstractDTOModel<DATA_MODEL, ENTITY>>, block: DTO.() -> Unit): Unit {
       // list.forEach { this@ServiceContext.initDTO(it) }
        this.block()
    }

    fun <DATA_MODEL : DataModel, ENTITY: LongEntity> DTOClass<DATA_MODEL, ENTITY>.select(block: DTOClass<DATA_MODEL, ENTITY>.(List<DATA_MODEL>) -> Unit): Unit {

        val result  = mutableListOf<CommonDTO<DATA_MODEL, ENTITY>>()
        dbQuery {
            this.daoEntityModel.all().forEach {
                result.add(this.create(it))
            }
        }

      //  return result
    }



//    private fun getDependantTables(forTable:IdTable<Long>): List<IdTable<Long>> {
//        val dependant = dtoCompanion.parentEntityDTO.registeredTables.filter { it.foreignKeys.map { keyMap->keyMap.targetTable.tableName }.contains(forTable.tableName) }
//        return dependant
//    }
//
//    fun dropTable(table : IdTable<Long>, allTables : List<IdTable<Long>> = emptyList() ): Boolean{
//        try {
//            return  dbQuery {
//                var result : Boolean = false
//                if(table.exists()) {
//                    val dependantTables = getDependantTables(table)
//                    if(dependantTables.isNotEmpty()){
//                        dependantTables.forEach{
//                            dropTable(it, allTables)
//                        }
//                        SchemaUtils.drop(table)
//                        result = true
//                    }else{
//                        SchemaUtils.drop(table)
//                        result = true
//                    }
//                }else{
//                    result = true
//                }
//                result
//            }
//        }catch (e: Exception){
//            return false
//        }
//    }
//    fun createTable(table : IdTable<Long>): Boolean{
//        return try {
//            dbQuery {
//                if(!table.exists()) {
//                    SchemaUtils.create(table)
//                    val dependantTables = getDependantTables(table)
//                    dependantTables.forEach{
//                        createTable(it)
//                    }
//                }
//                true
//            }
//        }catch (e: Exception){
//            false
//        }
//    }
//


}