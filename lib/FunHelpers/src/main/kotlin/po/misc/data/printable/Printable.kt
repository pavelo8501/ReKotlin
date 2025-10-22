package po.misc.data.printable


import kotlin.reflect.KClass


interface Printable {
    val formattedString : String
    val ownClass: KClass<out  Printable>

    fun echo(){
        println(formattedString)
    }
}

