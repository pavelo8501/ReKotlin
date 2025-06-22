package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.lastTaskHandler
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.asIdentifiableClass

class ServiceClass<DTO, DATA, ENTITY>(
    private val rootDTOModel: RootDTO<DTO, DATA, ENTITY>,
    @PublishedApi internal val connectionClass : ConnectionClass,
    private val serviceCreateOption: TableCreateMode = TableCreateMode.CREATE,
):  TasksManaged, IdentifiableClass  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    private val serviceContext: ServiceContext<DTO, DATA, ENTITY> = ServiceContext(this, rootDTOModel)
    internal val connection: Database get() = connectionClass.connection
    val logger : TaskHandler<*> get()= lastTaskHandler()

    override val identity = asIdentifiableClass("ServiceClass", rootDTOModel.completeName)

    private var running: Boolean = true

    private fun prepareTables(serviceCreateOption: TableCreateMode) {
        val tableList = mutableListOf<IdTable<Long>>()
        rootDTOModel.getAssociatedTables(tableList)
        val dropStatement =  rootDTOModel.config.entityModel.sourceTable.dropStatement()
        when (serviceCreateOption) {
            TableCreateMode.CREATE -> {
                logger.info("Creating tables TableCreateMode.CREATE")
                tableList.forEach {table->
                    if (!table.exists()) {
                        logger.info("Creating table ${table.tableName}")
                        SchemaUtils.create(table)
                        logger.info("${table.tableName} created")
                    }else{
                        logger.info("Table ${table.tableName} skip. Already exists")
                    }
                }
            }
            TableCreateMode.FORCE_RECREATE -> {
                val backwards = tableList.reversed()
                logger.info("Dropping tables  TableCreateMode.FORCE_RECREATE  ${backwards.onEach { "${it.tableName}, " }}")
                SchemaUtils.drop(*backwards.toTypedArray<IdTable<Long>>(), inBatch = true)
                logger.info("Dropped. Recreating")
                tableList.forEach {table->
                    logger.info("Creating table ${table.tableName}")
                    SchemaUtils.create(table)
                    logger.info("${table.tableName} created")
                }
            }
        }
    }

    internal fun deinitializeService(){
        running = false
        rootDTOModel.finalize()
    }

    internal fun initService(rootDTOModel: RootDTO<DTO, DATA, ENTITY>){
        transaction {
            if(running){
                rootDTOModel.initialization(serviceContext)

                prepareTables(serviceCreateOption)
            }
        }
    }


    internal fun runServiceContext(block:  ServiceContext<DTO, DATA, ENTITY>.()->Unit){
        println("Before   ServiceContext invoked (runServiceContext)")
        serviceContext.block()
    }

    internal suspend fun requestEmitter(session: AuthorizedSession): CoroutineEmitter
        = connectionClass.requestEmitter(session)

}