package po.misc.data.pretty_print.parts.cells

import po.misc.data.output.output
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.styles.Colour
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty


class CellDelegate<T>(
    val receiverToken: TypeToken<T>,
) {
    val stringToken: TypeToken<Any?> = TypeToken<Any?>()

    val textProperty: (T) ->  String = {receiver->
        receiver.toString()
    }

    fun register(thisRef: RowBuilder<T>,  property: KProperty<*>) {
       textProperty.toElementProvider(receiverToken, stringToken).let {
          // val cell = thisRef.add(it)
         //  "Registered ${cell.keyText}".output(Colour.Magenta)
       }
    }
    fun register(function: Function1<T, String>, thisRef: RowBuilder<T>): CellDelegate<T> {
//        function.toProvider(receiverToken, stringToken).let {
//            val cell = thisRef.add(it)
//            "Registered ${cell.keyText}".output(Colour.Magenta)
//        }
        return this
    }

    operator fun getValue(thisRef: RowBuilder<T>, property: KProperty<*>): String {
        thisRef.let {
            register(it, property)
        }?:run {
            "Nope, value asked before registered".output(Colour.Red)
        }
        return property.name
    }
    operator fun provideDelegate(thisRef: RowBuilder<T>, property: KProperty<*>): CellDelegate<T> {
        register(thisRef, property)
        return this
    }
    operator fun setValue(thisRef: RowBuilder<T>, property: KProperty<*>, value: String) {}
}


inline fun <reified T>  RowBuilder<T>.cellDelete(
    noinline function: Function1<T, String>
):CellDelegate<T> = CellDelegate(TypeToken<T>()).register(function, this)

inline fun <reified T> T.cellDelete():CellDelegate<T> = CellDelegate(TypeToken<T>())
