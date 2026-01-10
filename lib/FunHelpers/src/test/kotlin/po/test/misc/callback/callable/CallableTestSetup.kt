package po.test.misc.callback.callable

import po.misc.callbacks.callable.CallableRepositoryBase
import po.misc.callbacks.callable.PropertyCallable
import po.misc.callbacks.callable.PropertyCallable.Companion.invoke
import po.misc.callbacks.callable.ReceiverCallable
import po.misc.data.Named
import po.misc.functions.CallableKey
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1


//
//class RepoInheritor<T, V>(
//    override val receiverType: TypeToken<T>,
//    override val valueType: TypeToken<V>,
//    val callable: ReceiverCallable<T, V>
//): CallableRepositoryBase<T,V>(callable.receiverType, callable.valueType), Named {
//
//    init {
//        add(callable)
//    }
//    private val repositoryName:String = "RepoInheritor"
//    override val name: String = "$repositoryName<${receiverType.typeName}, ${valueType.typeName}>"
//
//    private val storageData get() = buildString {
//        append(name)
//        append("[${CallableKey.Property}: $hasProperty ")
//        append("${CallableKey.Resolver}: $hasResolver]")
//        append("${CallableKey.Provider}: $hasProvider]")
//    }
//    companion object : TokenFactory {
//        inline operator fun <reified T, reified V> invoke(
//            property: KProperty1<T, V>,
//        ): RepoInheritor<T, V> {
//            val receiverType = tokenOf<T>()
//            val valueToken = tokenOf<V>()
//            return RepoInheritor(receiverType, valueToken, PropertyCallable<T, V>(property))
//        }
//    }
//}
//
//class RepoInheritorList<T, V>(
//    receiverType: TypeToken<T>,
//    valueType: TypeToken<List<V>>,
//    val callable: ReceiverCallable<T, List<V>>
//): CallableRepositoryBase<T, List<V>>(receiverType, valueType), Named {
//
//    init {
//        add(callable)
//    }
//
//    private val repositoryName:String = "RepoInheritorList"
//    override val name: String = "$repositoryName<${receiverType.typeName}, ${valueType.typeName}>"
//    private val storageData get() = buildString {
//        append(name)
//        append("[${CallableKey.Property}: $hasProperty ")
//        append("${CallableKey.Resolver}: $hasResolver]")
//        append("${CallableKey.Provider}: $hasProvider]")
//    }
//

//}