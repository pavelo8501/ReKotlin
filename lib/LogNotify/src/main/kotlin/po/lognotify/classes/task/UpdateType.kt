package po.lognotify.classes.task

import po.misc.callbacks.ValueBasedEnum


sealed class UpdateType(override val value: Int): ValueBasedEnum {
   override fun byValue(intValue:Int):UpdateType {
        return values().first { it.value == intValue }
   }
    data object OnStart : UpdateType(1)
    data object OnComplete : UpdateType(2)


    companion object{
        inline fun <reified T> fromValue(value: Int): T where T : ValueBasedEnum, T : UpdateType {
            return T::class.sealedSubclasses
                .mapNotNull { it.objectInstance }
                .firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown value: $value")
        }
        fun values(): Array<UpdateType> {
            return arrayOf(OnStart, OnComplete)
        }
        fun valueOf(value: String): UpdateType {
            return when (value) {
                "OnStart" -> OnStart
                "OnComplete" -> OnComplete
                else -> throw IllegalArgumentException("No object po.lognotify.classes.task.UpdateType.$value")
            }
        }
    }
}