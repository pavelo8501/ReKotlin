package po.misc.dsl.configurator

import po.misc.interfaces.named.NameValue


enum class ConfigPriority(override val value: Int) : NameValue {

    Top(1),
    Default(2);

    companion object{
        fun priority(value: Int):ConfigPriority {
            return ConfigPriority.entries.firstOrNull { it.value == value } ?: Default
        }
    }
}