package po.exposify.scope.sequence.classes

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.dto.DTOContext
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.ServiceClass
import kotlin.reflect.KProperty1



/**
 * Represents a sealed interface for handling sequence execution and result callbacks.
 * @param T The type of data processed by the sequence handler.
 */
sealed interface SequenceHandlerInterface<T: DataModel> {
    val dtoClass : DTOClass<T, *>
    /**
     * The unique name of the sequence handler.
     */
    val name: String

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to be passed to the result callback.
     */
   // suspend fun submitResult(result: List<T>)
}

/**
 * An abstract class representing a sequence handler, responsible for managing
 * the execution of sequences and handling their result callbacks.
 *
 * @param T The type of data processed by the sequence handler. Must be a list of [DataModel].
 * @property name The unique name of the sequence handler.
 * @property resultCallback An optional function that is invoked when a sequence result is available.
 */
abstract class SequenceHandler<T>(
    override val dtoClass: DTOClass<T, *>,
    override val name: String
) : SequenceHandlerInterface<T> where  T: DataModel {

    internal val sequences =
        mutableMapOf<String, SequencePack<T, *>>()

    internal fun getStockSequence(): SequencePack<T,*>{
        val sequence = sequences[name]
        if(sequence != null){
            return  sequence
        }else{
            throw OperationsException("Unable to find sequence for a given handler ${this.name}", ExceptionCodes.KEY_NOT_FOUND )
        }
    }

    suspend fun execute(params: Map<String, String> ): Deferred<List<T>>{
        getStockSequence().let {
            it.saveParams(params)
            it.saveInputList(emptyList())
            return it.serviceClass.launchSequence(it)

        }
    }

    suspend fun execute(inputList : List<T>): Deferred<List<T>> {
        getStockSequence().let {
            it.saveInputList(inputList)
            it.saveParams(emptyMap())
            return it.serviceClass.launchSequence(it)
        }
    }

    suspend fun execute(params: Map<String, String>, inputList : List<T>): Deferred<List<T>>{
        getStockSequence().let {
            it.saveParams(params)
            it.saveInputList(inputList)
            return it.serviceClass.launchSequence(it)
        }
    }
    suspend fun execute(): Deferred<List<T>>{
        getStockSequence().let {
            it.saveParams(emptyMap())
            it.saveInputList(emptyList())
            return it.serviceClass.launchSequence(it)
        }
    }

}