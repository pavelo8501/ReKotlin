package po.misc.interfaces

import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.ManagedException
import po.misc.types.TypeData
import po.misc.types.Typed
import po.misc.types.safeCast
import kotlin.reflect.KClass

interface IdentifiableContext {
    val contextName: String
}

interface CtxId:IdentifiableContext {
    override val contextName: String

    data class IdentityMessage(
        val contextName: String,
        var message:String = "",
        var colour: Colour = Colour.GREEN
    ){

        init {
            message = """
                ╔═══════════════════════════════════════
                ║ 🧠 In Context [$contextName]
                ╟───────────────────────────────────────
                ║ ${message.colorize(colour)}
                ╚═══════════════════════════════════════
                """.trimIndent()
        }

        internal fun updateMessage(msg: String, optionalColour: Colour?){
            if(optionalColour != null){
                colour = optionalColour
            }
        }

        fun printLine(){
            println(this)
        }
        override fun toString(): String {
           return message
        }
    }


    fun echo(message: String,  block: IdentityMessage.()-> Unit){
        IdentityMessage(this.contextName, message).block()
    }

    fun <T : Any> getResolved(kClass: KClass<T>, block: T.() -> Unit) {
        safeCast(kClass)?.let {
            block.invoke(it)
        }
    }
}

interface CTX: CtxId

inline fun <reified T: Any> CtxId.getResolved(block:T.()-> Unit){
    safeCast<T>()?.let {
        block.invoke(it)
    }
}



interface Identifiable: CTX{
    var sourceName: String
    val completeName: String get()= "$contextName[$sourceName]"

}


interface TypedContext<T: Any>: CTX{
    val typeData: TypeData<T>
    var sourceName: String
    val completeName: String get() = "$contextName[$sourceName]"
}



interface IdentifiableClass : CTX {

    val identity: ClassIdentity

    @Suppress("UNNECESSARY_SAFE_CALL")
    override  val contextName: String get() = identity?.completeName?:"Default"
    @Suppress("UNNECESSARY_SAFE_CALL")
    val completeName: String get() = identity?.completeName?:"Default"
}

interface ObservedContext: CTX{
    val sourceName: String
    val completeName: String get()= "$contextName[$sourceName]"
    val exceptionOutput: ((ManagedException)-> Unit)?

}




