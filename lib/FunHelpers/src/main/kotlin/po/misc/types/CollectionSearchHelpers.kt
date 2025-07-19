package po.misc.types


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