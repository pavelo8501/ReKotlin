package po.misc.callbacks.loop.models

import po.misc.callbacks.loop.ModifiedOutput


data class OutputItem(
    val id: Long,
    val value: String
)

data class OutputGroup(
    val id: Long,
    val output: List<OutputItem>
)

data class DefaultOutput(
    val groups: List<OutputGroup>
) : ModifiedOutput {

    override val size: Int
        get() = groups.sumOf { it.output.size }
}
