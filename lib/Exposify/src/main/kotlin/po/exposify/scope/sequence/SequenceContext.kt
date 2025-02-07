package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.models.CrudResult
import po.exposify.scope.dto.DTOContext

class SequenceContext<DATA, ENTITY>(
    val connection: Database,
    val hostDto : DTOClass<DATA,ENTITY>
) where  DATA : DataModel, ENTITY : LongEntity
{

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    private var lastResult : CrudResult<DATA, ENTITY>? = null

    private fun dtos(): List<CommonDTO<DATA,ENTITY>>{
        val result =   mutableListOf<CommonDTO<DATA,ENTITY>>()
        lastResult?.rootDTOs?.forEach{  result.add(it) }
        return result
    }

    fun List<CommonDTO<DATA, ENTITY>>.checkout(block: DTOContext<DATA, ENTITY>.()-> Unit){
        DTOContext<DATA, ENTITY>(CrudResult<DATA, ENTITY>(this,null)).block()
    }

    fun <SWITCH_DATA: DataModel, SWITCH_ENTITY : LongEntity> DTOClass<SWITCH_DATA, SWITCH_ENTITY>.switch(
        block:  SequenceContext<SWITCH_DATA, SWITCH_ENTITY>.(dtos: List<CommonDTO<SWITCH_DATA, SWITCH_ENTITY>>)->Unit ){

        val list = dtos().map { it.getChildren<SWITCH_DATA, SWITCH_ENTITY>(this) }.flatten()
        val result =  CrudResult<SWITCH_DATA, SWITCH_ENTITY>(list as List<CommonDTO<SWITCH_DATA, SWITCH_ENTITY>>, null )
        val newSequenceContext =  SequenceContext<SWITCH_DATA, SWITCH_ENTITY>(connection, this)
        newSequenceContext.block(list)
    }

    fun select(block: SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA,ENTITY>>)-> Unit){
        val result by lazy {
            dbQuery { lastResult = hostDto.select() }
            dtos()
        }
        this.block(result)
    }

    fun update(
        dataModels: List<DATA>,
        block: SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA,ENTITY>>)-> Unit)
    {
        val result by lazy {
            dbQuery { lastResult = hostDto.update<DATA, ENTITY>(dataModels) }
            dtos()
        }
        this.block(result)
    }

    @JvmName("UpdateDtos")
    fun update(
        dtos: List<CommonDTO<DATA,ENTITY>>,
        block: SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA,ENTITY>>)-> Unit)
    {
        val result by lazy {
            dbQuery { lastResult = hostDto.update<DATA, ENTITY>(dtos.map { it.getInjectedModel() }) }
            dtos()
        }
        this.block(result)
    }
}