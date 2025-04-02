package po.exposify.scope.dto

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.WhereCondition
import po.exposify.common.models.CrudResult2
import po.lognotify.eventhandler.models.Event


class DTOContext2<DTO>(
    private val dtoClass: DTOClass<DTO>,
    private var crudResult : CrudResult2<DTO>? = null,
    private val resultCallback: ((List<DataModel> )-> Unit)? = null
) where DTO : ModelDTO{

    public var condition:  WhereCondition<*> ? = null

    init {
        resultCallback?.let{callbackOnResult(resultCallback)}
    }


    operator fun invoke(op:  WhereCondition<*>): DTOContext2<DTO> {
        condition = op
        return this
    }

    fun <T: IdTable<Long>> withConditions(conditions :  WhereCondition<T>): DTOContext2<DTO> {
        condition = conditions
        return this
    }

    suspend fun select() {
//        crudResult =  if (condition != null) {
//            dtoClass.select<DTO, DATA, >()
//            rootDTOClass.select(condition!!)
//        }else{
//            rootDTOClass.select()
//        }
    }

    private fun asDataModels(crud: CrudResult2<DTO>): List<DataModel>{
        return  crud.rootDTOs.map { it.compileDataModel() }
    }

    fun getData(): List<DataModel>{
        return  asDataModels(crudResult!!)
    }

    fun getStats(): Event?{
        crudResult!!.event?.print()
        return crudResult!!.event
    }

    fun callbackOnResult(callback : (List<DataModel>)->Unit ){
        callback.invoke(asDataModels(crudResult!!))
    }
}