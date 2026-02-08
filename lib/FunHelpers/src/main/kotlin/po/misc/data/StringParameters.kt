package po.misc.data

interface StringParameters{
    val plainLength: Int
    val hasLineBreak: Boolean
    val endsLineBreak: Boolean
}

interface Normalizable{
    fun normalize()
}