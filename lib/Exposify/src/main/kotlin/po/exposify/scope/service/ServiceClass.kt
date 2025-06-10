package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsClass
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ExposifyModule
import po.exposify.dto.models.ModuleType
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.lastTaskHandler
import po.lognotify.logNotify
import po.misc.interfaces.IdentifiableModule

class ServiceClass<DTO, DATA, ENTITY>(
    private val rootDTOModel: RootDTO<DTO, DATA, ENTITY>,
    internal val connectionClass : ConnectionClass,
    private val serviceCreateOption: TableCreateMode = TableCreateMode.CREATE,
    val moduleType: ExposifyModule = ExposifyModule(ModuleType.ServiceClass, rootDTOModel.component)
): IdentifiableModule by moduleType,  AsClass<DATA, ENTITY>, TasksManaged  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    private val serviceContext: ServiceContext<DTO, DATA, ENTITY> = ServiceContext(this, rootDTOModel)
    internal val connection: Database get() = connectionClass.connection
    val logger : TaskHandler<*> get()= lastTaskHandler()


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

    internal fun initService(rootDTOModel: RootDTO<DTO, DATA, ENTITY>){
        transaction {
            rootDTOModel.initialization(serviceContext)
            prepareTables(serviceCreateOption)
        }
    }

    internal fun runServiceContext(block:  ServiceContext<DTO, DATA, ENTITY>.()->Unit){
        println("Before   ServiceContext invoked (runServiceContext)")
        serviceContext.block()
    }

    internal suspend fun requestEmitter(session: AuthorizedSession): CoroutineEmitter
        = connectionClass.requestEmitter(session)

}