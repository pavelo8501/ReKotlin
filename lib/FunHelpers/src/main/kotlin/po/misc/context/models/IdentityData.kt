package po.misc.context.models

import po.misc.data.styles.Colour


data class IdentityData(
    val contextName: String,
    var message:String = "",
    var colour: Colour = Colour.Green
){
    fun printLine(){
        println(this)
    }
    override fun toString(): String  = "$contextName -> $message"
}