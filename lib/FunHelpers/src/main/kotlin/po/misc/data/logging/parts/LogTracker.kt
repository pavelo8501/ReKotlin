package po.misc.data.logging.parts

import po.misc.data.HasValue

enum class LogTracker(override val value: Int): HasValue{
    Disabled(0),
    Enabled(1)
}