package po.misc.reflection.classes

import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.types.TypeData

import kotlin.reflect.KClass

class DataProvider<T: CTX, V: Any>(
    val surrogate: KSurrogate<T>,
    val valueClass: KClass<V>,
) {

}

//class Surrogate<T: IdentifiableContext>(
//    surrogate: KSurrogate<T>
//): ContainerBase<KSurrogate<T>>(surrogate)
//



inline fun <reified T: CTX> createSurrogate(
    receiver:T,
    block: KSurrogate<T>.()-> Unit
): KSurrogate<T>{
  // val container = KSurrogate(KSurrogate(receiver, SurrogateHooks()))
   val surrogate =   KSurrogate(receiver, TypeData.create<T>())
   block.invoke(surrogate)
   return surrogate
}


//inline fun <reified T: Any> KSurrogate<*>.createSource(): DataSource<T>{
//    val dataSource =  DataSource(T::class, typeOf<T>())
//    this.registerDataSource(dataSource)
//   // ctx.registerDataSource(dataSource)
//    return dataSource
//}
//
//inline  fun <reified T: Any> KSurrogate<*>.createDataSource(block: DataSource<T>.()-> Unit): DataSource<T>{
//    val dataSource =  DataSource(T::class, typeOf<T>())
//    block.invoke(dataSource)
//    this.registerDataSource(dataSource)
//    return dataSource
//}
//
//inline fun  <T : IdentifiableContext, V : Any>  KSurrogate<T>.createPropertyGroup(
//    sourceProperty: SourcePropertyIO<T, V>,
//    block: PropertyGroup<T, V>.()-> Unit
//): PropertyGroup<T, V> {
//    val group = PropertyGroup(sourceProperty, this)
//    block.invoke(group)
//    this.registerGroup(group)
//    return group
//}
