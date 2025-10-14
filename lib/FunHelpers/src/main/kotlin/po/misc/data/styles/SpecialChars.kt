package po.misc.data.styles

import po.misc.data.HasValue

//enum class SpecialChars(override val value: String): HasValue{
//    Empty(""),
//    NewLine("\n");
//
//    override fun toString(): String{
//        return value
//    }
//
//    fun getChar(): String{
//        return value
//    }
//
//    operator fun get(char: SpecialChars): String{
//        return char.value
//    }
//}


object SpecialChars{
    const val empty: String = ""
    const val newLine: String = "\n"
}


