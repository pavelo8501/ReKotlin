package po.misc.properties

interface PropertyReadWrite<T: Any>{
    fun write(value:T)
    fun read():T
}


class PropertySurrogate<T: Any>(val value:T):PropertyReadWrite<T>{

    private val property: (T)-> T = {

        it
    }

    override fun write(value: T) {
        property(value)
    }

    override fun read(): T {
        TODO("Not yet implemented")
    }


}