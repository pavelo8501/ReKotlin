package po.exposify.scope.sequence.classes

import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO

/**
 * Default implementation of [SequenceHandler] for cases where a sequence is identified by name
 * instead of a predefined handler object.
 *
 * This class is internal because it is meant for framework-level sequence handling
 * and should not be exposed to external consumers.
 *
 * @param T The type of data processed by the sequence handler. Must be a list of [DataModel].
 * @param name The unique name of the sequence.
 */
internal class DefaultSequenceHandler<T: DataModel>(dtoModel : DTOClass<T,*> , name: String)
    : SequenceHandler<T>(dtoModel, name)