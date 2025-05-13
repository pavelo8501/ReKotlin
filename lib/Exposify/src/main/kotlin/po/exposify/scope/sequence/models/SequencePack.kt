package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.RootExecutionProvider
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.misc.collections.CompositeKey


data class RootSequencePack<DTO, DATA, ENTITY>(
    override val key : CompositeKey<RootDTO<*, *, *>, SequenceID>,
    val dtoClass: RootDTO<DTO, DATA, ENTITY>,
    private val block : suspend SequenceContext<DTO, DATA, ENTITY>.(RootSequenceHandler<DTO, DATA, ENTITY>) -> Unit,
): SequencePack<DTO, DATA, ENTITY> where DTO : ModelDTO,   DATA : DataModel, ENTITY : LongEntity{

    suspend fun<R> start(handlerConfigFn: suspend RootSequenceHandler<DTO, DATA, ENTITY>.()-> Unit):R{
        val handler = dtoClass.createHandler(key.getEnumParameter())
        handler.handlerConfigFn()
        val execProvider = RootExecutionProvider(dtoClass)
        val context = SequenceContext(handler, execProvider)
        handler.provideContext(context)
        block(context, handler)
        return handler.getDataResult() as R
    }


    suspend fun<F_DTO: ModelDTO, FD: DataModel, FE: LongEntity, R> start(
        childClass: DTOClass<F_DTO, FD, FE>,
        handlerConfigFn: suspend RootSequenceHandler<DTO, DATA, ENTITY>.()-> Unit):R
    {
        val handler = dtoClass.createHandler(key.getEnumParameter())
        handler.handlerConfigFn()
        val execProvider = RootExecutionProvider(dtoClass)
        val context = SequenceContext(handler, execProvider)
        handler.provideContext(context)
        block(context, handler)
        val switchParam =   handler.switchParameters.firstOrNull { it.childClass ==  childClass}
            .getOrOperationsEx("Switch parameters for class ${childClass.qualifiedName} not found", ExceptionCode.VALUE_IS_NULL)

        val  switchContext =  switchParam.sequenceContext.getOrOperationsEx()
        return switchContext.sequenceHandler.getDataResult() as R
    }

}

//data class ClassSequencePack<DTO, DATA,  ENTITY>(
//    val switchQuery :  SwitchQuery<*, *>,
//    override val key : CompositeKey<DTOClass<*, *, *>, SequenceID>,
//    val dtoClass: DTOClass<DTO, DATA, ENTITY>,
//    private val block : suspend SequenceContext<DTO, DATA, ENTITY>.(ClassSequenceHandler<DTO, DATA, ENTITY>) -> Unit,
//): SequencePack<DTO, DATA, ENTITY> where DTO: ModelDTO,  DATA : DataModel, ENTITY : LongEntity{
//
//
//
//    suspend fun<R> start(handlerConfigFn: suspend ClassSequenceHandler<DTO,  DATA, ENTITY>.()-> Unit):R{
//        val handler = dtoClass.createHandler(key.getEnumParameter())
//        handler.handlerConfigFn()
//        val execProvider = ClassExecutionProvider(dtoClass)
//        val context = SequenceContext(handler, execProvider)
//        handler.provideContext(context)
//        block(context, handler)
//        return handler.getDataResult() as R
//    }
//}

sealed interface SequencePack<DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity>{
    val key : CompositeKey<*, SequenceID>
}
