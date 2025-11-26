package po.misc.data.strings

import po.misc.data.styles.Colour
import kotlin.reflect.KProperty1

internal fun <T: Any> recursiveStringify(
    receiver: T,
    property: KProperty1<T, Collection<T>>,
    parentFormated:  FormatedEntry,
    colour: Colour?
){
    val entries : Collection<T> = property.get(receiver)
    entries.forEach { entry->
        val formatted = StringFormatter.formatKnownTypes2(entry)
        parentFormated.addFormated(formatted)
        recursiveStringify(entry, property, formatted, colour)
    }
}


fun <T: Any> T.stringify(
    property: KProperty1<T, Collection<T>>,
    colour: Colour? = null
): FormatedEntry{
    val formatedReceiver = StringFormatter.formatKnownTypes2(this)
    val records = property.get(this)
    records.forEach { record ->
        val formatedEntry =  StringFormatter.formatKnownTypes2(record)
        formatedEntry.colour(colour)
        formatedReceiver.addFormated(formatedEntry)
        recursiveStringify(record, property, formatedEntry, colour)
    }
    return  formatedReceiver
}
