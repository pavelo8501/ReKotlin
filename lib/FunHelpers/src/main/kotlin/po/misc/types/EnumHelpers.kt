package po.misc.types


fun <E: Enum<E>> enumToConstants(javaClass: Class<E>): Array<E>{
    val result = javaClass.enumConstants?: emptyArray()
   return result
}

fun <E: Enum<E>, R> enumBasedBuilder(javaClass: Class<E>, builder: (E)-> R): Collection<R>{
    return  enumToConstants(javaClass).map { builder.invoke(it) }
}


