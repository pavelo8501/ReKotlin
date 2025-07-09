package po.exposify.scope.sequence.models

import po.misc.functions.containers.PromiseResultContainer


data class SequenceParameter<P: Any>(
    val  inputValue: PromiseResultContainer<P>
)
