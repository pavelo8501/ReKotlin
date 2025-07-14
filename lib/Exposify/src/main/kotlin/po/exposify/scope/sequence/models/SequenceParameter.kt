package po.exposify.scope.sequence.models

import po.misc.functions.containers.LambdaContainer


data class SequenceParameter<P: Any>(
    val  input: LambdaContainer<P>
)
