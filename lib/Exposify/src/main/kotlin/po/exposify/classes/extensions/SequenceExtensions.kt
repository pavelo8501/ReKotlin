package po.exposify.classes.extensions

import po.auth.authentication.exceptions.ErrorCodes
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.lognotify.extensions.castOrException
import po.lognotify.extensions.startTask
import po.lognotify.extensions.startTaskAsync
import kotlin.coroutines.CoroutineContext




suspend fun <DTO : ModelDTO, DATA: DataModel>  DTOClass<DTO>.runSequence(
    sequenceId: Int,
    context:  CoroutineContext,
    handlerBlock: (suspend SequenceHandler<DTO, DataModel>.()-> Unit)? = null):List<DATA>
        = startTask("Run Sequence", context,  personalName) {
    withTransactionIfNone {

        val serviceContext = serviceContextOwned.getOrOperationsEx(
            "Unable to run sequence id: $sequenceId on DTOClass. DTOClass is not a hierarchy root",
            ExceptionCode.UNDEFINED
        )
        val handler = serviceContext.serviceClass().getSequenceHandler(sequenceId, this)
        handlerBlock?.invoke(handler)
        val key =  handler.thisKey
        val result = serviceContext.serviceClass().runSequence(key).castOrOperationsEx<List<DATA>>()

        result
    }
}.resultOrException()



suspend fun <DTO : ModelDTO , DATA: DataModel> DTOClass<DTO>.runSequence(
    sequenceID: SequenceID,
    context:  CoroutineContext,
    handlerBlock: (suspend SequenceHandler<DTO, DataModel>.()-> Unit)? = null):List<DATA>
        = runSequence(sequenceID.value, context,  handlerBlock)


fun <DTO : ModelDTO, DATA: DataModel>  DTOClass<DTO>.runSequence(
    sequenceId: Int,
    handlerBlock: (suspend SequenceHandler<DTO, DataModel>.()-> Unit)? = null):List<DATA>
        = startTaskAsync("Run Sequence",  personalName) {
    withTransactionIfNone {

        val serviceContext = serviceContextOwned.getOrOperationsEx(
            "Unable to run sequence id: $sequenceId on DTOClass. DTOClass is not a hierarchy root",
            ExceptionCode.UNDEFINED
        )
        val handler = serviceContext.serviceClass().getSequenceHandler(sequenceId, this)
        handlerBlock?.invoke(handler)
        val key =  handler.thisKey
        val result = serviceContext.serviceClass().runSequence(key).castOrOperationsEx<List<DATA>>()
        result
    }
}.resultOrException()



fun <DTO : ModelDTO , DATA: DataModel> DTOClass<DTO>.runSequence(
    sequenceID: SequenceID,
    handlerBlock: (suspend SequenceHandler<DTO, DataModel>.()-> Unit)?= null):List<DATA>
        = runSequence(sequenceID.value, handlerBlock)