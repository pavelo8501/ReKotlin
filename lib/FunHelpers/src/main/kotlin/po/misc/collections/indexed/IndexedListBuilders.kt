package po.misc.collections.indexed


fun <T: Indexed> indexedListOf(): IndexedList<T>{
   return IndexedList<T>()
}

fun <T: Indexed> indexedListOf(indexed:T, vararg indexedElements: T): IndexedList<T>{
    val list = buildList {
        add(indexed)
        addAll(indexedElements.toList())
    }
    val indexed = IndexedList<T>()
    indexed.addAll(list)
    return indexed
}

