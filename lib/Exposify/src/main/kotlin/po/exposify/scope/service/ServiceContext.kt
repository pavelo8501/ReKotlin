package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.exposify.common.classes.ExposifyDebugger
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.RootDTO
import po.exposify.dto.components.executioncontext.RootExecutionContext
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.TasksManaged
import po.lognotify.launchers.runTaskBlocking
import po.misc.context.CTXIdentity
import po.misc.context.Identifiable
import po.misc.context.asSubIdentity
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.functions.containers.DeferredContainer
import po.misc.types.token.TypeToken


class ServiceContext<DTO, DATA, ENTITY>(
    @PublishedApi internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): TasksManaged where DTO : ModelDTO, DATA: DataModel,  ENTITY: LongEntity {

    override val identity: CTXIdentity<ServiceContext<DTO, DATA, ENTITY>> get() = asSubIdentity(dtoClass)

    internal val executionProvider: RootExecutionContext<DTO, DATA, ENTITY> get() = dtoClass.executionContext
    private val dbConnection: Database get() = serviceClass.connection

    private val trackingList = mutableMapOf<CTXIdentity<*>, (DTO)-> Unit>()

    internal val dataType: TypeToken<DATA> = dtoClass.commonDTOType.dataType

    val debugger: ExposifyDebugger<ServiceContext<DTO, DATA, ENTITY>, ContextData> = exposifyDebugger(this, ContextData){
        ContextData(it.message)
    }

    internal fun setTracking(identity: CTXIdentity<*>, callback: suspend (DTO)-> Unit){
        executionProvider.setTracking(identity, callback)
    }

    private fun afterUpdated(result:  ResultSingle<DTO, DATA>): ResultSingle<DTO, DATA>{
        result.data?.let {
            if(it is Identifiable<*>){
                "found in result data".output(Colour.Green)
                trackingList[it.identity]?.let {
                    "comparison by identity work with result data".output(Colour.Green)
                }
            }
        }
        return result
    }

    private fun beforeUpdated(dataModel:  DATA): DATA{
        if(dataModel is Identifiable<*>){
           "found".output(Colour.Green)
            trackingList[dataModel.identity]?.let {
                "comparison by identity work".output(Colour.Green)
            }
        }
        return dataModel
    }

    private fun beforeUpdated(dataList:  List<DATA>) = dataList.forEach { beforeUpdated(it) }


    fun truncate(): Unit = runTaskBlocking("Truncate") {
        dtoClass.clearCachedDTOs()
        val tableName = dtoClass.entityClass.table.tableName
        val statement = "TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE"
        newSuspendedTransaction { exec(statement) }
        notify("$statement Executed")
    }.resultOrException()

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA> =
        runTaskBlocking("Pick<SimpleQuery>") {
            executionProvider.pick(conditions)
    }.resultOrException()

    fun pick(conditions: DeferredContainer<WhereQuery<ENTITY>>): ResultSingle<DTO, DATA> =
        runTaskBlocking("Pick<WhereQuery>") {
            executionProvider.pick(conditions.resolve())
        }.resultOrException()

    fun pickById(id: Long): ResultSingle<DTO, DATA> = runTaskBlocking("Pick($id)") {
        executionProvider.pick(id)
    }.resultOrException()

    fun select(): ResultList<DTO, DATA> =
        runTaskBlocking("Select") {
            executionProvider.select()
    }.resultOrException()

    fun select(conditions: DeferredContainer<WhereQuery<ENTITY>>):ResultList<DTO, DATA> = runTaskBlocking("Select(with conditions)") {
        executionProvider.select(conditions.resolve())
    }.resultOrException()

    fun insert(dataModels: List<DATA>): ResultList<DTO, DATA> = runTaskBlocking("Insert") {
        beforeUpdated(dataModels)
        executionProvider.insert(dataModels)
    }.resultOrException()


    fun insert(dataModel: DATA): ResultSingle<DTO, DATA> = runTaskBlocking("Insert"){
        executionProvider.insert(dataModel)
    }.resultOrException()


    fun update(dataModel: DATA): ResultSingle<DTO, DATA> = runTaskBlocking("Update"){
        beforeUpdated(dataModel)
        val result =  executionProvider.update(dataModel)
        afterUpdated(result)
        result
    }.resultOrException()

    fun update(dataModels: List<DATA>): ResultList<DTO, DATA> = runTaskBlocking("Update") {
        beforeUpdated(dataModels)
        executionProvider.update(dataModels)
    }.resultOrException()


    fun delete(toDelete: DATA): ResultSingle<DTO, DATA> = runTaskBlocking("Delete"){
        executionProvider.delete(toDelete)
    }.resultOrException()

}