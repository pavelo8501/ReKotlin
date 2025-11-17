package po.misc.dsl.configurator

import po.misc.data.HasNameValue

enum class ConfigPriority(override val value: Int) : HasNameValue {

    Top(1),
    Default(2);

    companion object{
        fun priority(value: Int):ConfigPriority {
            return ConfigPriority.entries.firstOrNull { it.value == value } ?: Default
        }
    }
}