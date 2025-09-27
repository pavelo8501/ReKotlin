package po.misc.types




interface Typed<T: Any>{
    val parameter1: TypeData<T>
}


interface TypedObject{
    val types: List<TypeData<*>>
}


interface DoubleTyped<T1: Any, T2: Any>{
   val parameter1: TypeData<T1>
   val parameter2: TypeData<T2>
}









