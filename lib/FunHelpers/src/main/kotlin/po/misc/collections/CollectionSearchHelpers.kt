package po.misc.collections


fun <C : MutableCollection<in R>, R> Iterable<*>.selectToInstance(destination: C, instance:R): C {
    forEach {
        if(it  !== instance){
            @Suppress("UNCHECKED_CAST")
            destination.add(it as R)
        }else{
            return destination
        }
    }
    return destination
}

fun <T : Any> Iterable<T>.selectUntil(predicate:(T)-> Boolean): List<T> {
    val resultingList = mutableListOf<T>()
    for (element in this) {
        resultingList.add(element)
        if (predicate(element)) {
            return resultingList
        }
    }
    return emptyList()
}

fun <T> Array<out T>.takeFromMatch(count: Int, predicate: (T) -> Boolean): List<T> {
    return this.dropWhile { !predicate(it) }
        .take(count)
}

fun <T> Array<T>.takeFromLastMatching(count: Int, predicate: (T) -> Boolean): List<T> {
    val lastIndex = (this.size - 1 downTo 0).firstOrNull { predicate(this[it]) } ?: return emptyList()
    val endIndexExclusive = (lastIndex + count).coerceAtMost(this.size)
    return this.slice(lastIndex until endIndexExclusive)
}



fun <T> List<T>.takeFromMatch(count: Int, predicate: (T) -> Boolean): List<T> {
    return this.dropWhile { !predicate(it) }
        .take(count)
}
