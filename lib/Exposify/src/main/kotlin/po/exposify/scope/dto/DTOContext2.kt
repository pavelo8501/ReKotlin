package po.exposify.scope.dto

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.WhereCondition
import po.exposify.dto.components.CrudResult


class DTOContext2<DTO, DATA>(
    private val dtoClass: DTOClass<DTO>,
    private var crudResult : CrudResult<DTO, DATA>? = null,
    private val resultCallback: ((List<DataModel> )-> Unit)? = null
) where DTO : ModelDTO, DATA : DataModel{
    public var condition:  WhereCondition<*> ? = null
    init {
        resultCallback?.let{callbackOnResult(resultCallback)}
    }
    operator fun invoke(op:  WhereCondition<*>): DTOContext2<DTO, DATA> {
        condition = op
        return this
    }
    fun <T: IdTable<Long>> withConditions(conditions :  WhereCondition<T>): DTOContext2<DTO, DATA> {
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

    private fun asDataModels(crud: CrudResult<DTO, DATA>): List<DataModel>{
       // return  crud.rootDTOs.map { it.compileDataModel() }
      return  emptyList<DataModel>()
    }

    fun getData(): List<DataModel>{
        return  asDataModels(crudResult!!)
    }

//    fun getStats(): Event?{
//        crudResult!!.event?.print()
//        return crudResult!!.event
//    }

    fun callbackOnResult(callback : (List<DataModel>)->Unit ){
        callback.invoke(asDataModels(crudResult!!))
    }
}