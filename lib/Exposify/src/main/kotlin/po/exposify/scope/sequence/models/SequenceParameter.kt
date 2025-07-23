package po.exposify.scope.sequence.models

import po.misc.functions.containers.LambdaHolder


data class SequenceParameter<P: Any>(
    val  input: LambdaHolder<P>
)
