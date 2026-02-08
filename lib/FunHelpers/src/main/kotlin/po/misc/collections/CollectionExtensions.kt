package po.misc.collections


inline fun <reified E> List<E>.toArray(): Array<E> {
    val elements = this
    return Array(size){
        elements[it]
    }
}