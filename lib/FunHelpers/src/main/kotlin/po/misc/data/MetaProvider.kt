package po.misc.data

import po.misc.data.MetaData.BuilderData
import po.misc.types.safeCast
import kotlin.Int
import kotlin.String
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf


interface MetaData{
    class BuilderData<T: MetaData, S>(val builderActionLambda : T.(S)-> T, val sourceType : KType)
    companion object {
        val builderMap : MutableMap<String, BuilderData<*, *>> = mutableMapOf()
        fun <T: MetaData, S: Any> getBuilderData(name: String): BuilderData<T, S>?{
           return builderMap[name]?.safeCast<BuilderData<T, S>>()
        }
        fun buildModel(): MetaDataModel {
            return   MetaDataModel()
        }
    }
}

@MetaProperty
class Integer<T:MetaData>(private val valueProvider: (()-> Int)? = null) : MetaPropertyBase<T, Int>(){
    override fun provideValue(): Int{
        return  valueProvider?.invoke()?:0
    }
}

sealed class MetaPropertyBase<T : MetaData,V>() : ReadOnlyProperty<T,V>{
    abstract fun provideValue():V
    override fun getValue(thisRef: T, property: KProperty<*>): V {
       return provideValue()
    }
}

inline fun <reified T :MetaData, reified S: Any> T.construct(sourceProvider: () -> S):T?{
   return MetaData.Companion.getBuilderData<T, S>(S::class.simpleName.toString())?.let {data->
        val source = sourceProvider()
        data.builderActionLambda.safeCast<T.(S)-> T>()?.invoke(this, source)
    }
}

fun MetaData.collectAnnotatedProperties(): Map<String, Any?> {
    return this::class.memberProperties
        .filter { it.findAnnotation<MetaProperty>() != null }
        .associate { it.name to it.call(this) }
}

class MetaProvider<T: MetaData>(val modelBuilder : ()-> T){

    inline fun <reified S: Any> registerBuilder(noinline builder: T.(S) -> T) {
        MetaData.Companion.builderMap[S::class.simpleName.toString()] = BuilderData(builder, typeOf<S>())
    }
}



