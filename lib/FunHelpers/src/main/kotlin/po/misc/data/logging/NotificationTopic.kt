package po.misc.data.logging

import po.misc.data.styles.Colour
import po.misc.data.styles.colorize


enum class Topic(val value: Int): Comparable<Topic>{
    Debug(0),
    Info(1),
    Warning(2),
    Exception(3);
}





