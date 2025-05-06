package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.ClassExecutionProvider
import po.exposify.dto.components.RootExecutionProvider
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.misc.collections.CompositeKey


data class RootSequencePack<DTO,  DATA>(
    override val key : CompositeKey<DTOBase<*,*>, SequenceID>,
    val dtoClass: RootDTO<DTO, DATA>,
    private val block : suspend SequenceContext<DTO, DATA, LongEntity>.(RootSequenceHandler<DTO, DATA>) -> Unit,
): SequencePack<DTO, DATA> where DTO: ModelDTO,  DATA : DataModel{

    suspend fun<R> start(handlerConfigFn: suspend RootSequenceHandler<DTO, DATA>.()-> Unit):R{
        val handler = dtoClass.createHandler(key.getEnumParameter())
        handler.handlerConfigFn()
        val execProvider = RootExecutionProvider<DTO, DATA, LongEntity>(dtoClass)
        val context = SequenceContext<DTO, DATA, LongEntity>(execProvider)
        handler.provideContext(context)
        block(context, handler)
        return handler.getDataResult() as R
    }
}

data class ClassSequencePack<DTO,  DATA>(
    override val key : CompositeKey<DTOBase<*,*>, SequenceID>,
    val dtoClass: DTOClass<DTO, DATA>,
    private val block : suspend SequenceContext<DTO, DATA, LongEntity>.(ClassSequenceHandler<DTO, DATA>) -> Unit,
): SequencePack<DTO, DATA> where DTO: ModelDTO,  DATA : DataModel{

    suspend fun<R> start(handlerConfigFn: suspend ClassSequenceHandler<DTO, DATA>.()-> Unit):R{
        val handler = dtoClass.createHandler(key.getEnumParameter())
        handler.handlerConfigFn()
        val execProvider = ClassExecutionProvider<DTO, DATA, LongEntity>(dtoClass)
        val context = SequenceContext<DTO, DATA, LongEntity>(execProvider)
        handler.provideContext(context)
        block(context, handler)
        return handler.getDataResult() as R
    }
}

sealed interface SequencePack<DTO,  DATA>{

    val key : CompositeKey<DTOBase<*,*>, SequenceID>

}
