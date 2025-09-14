package po.misc.reflection.anotations

import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.reflection.properties.testMutability
import po.misc.reflection.properties.updateConverting
import kotlin.reflect.KClassifier
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties


open class AnnotationPropertyPair<T: Any,  A : Annotation>(
    val property:KProperty1<T, Any>,
    val annotation : A,
){
    val name: String = property.name
    var mutable: Boolean = false
    val classifier:  KClassifier? get() =   property.returnType.classifier

}

open class AnnotationContainer<T: Any,  A : Annotation>{

    private val propertyPairsBacking: MutableList<AnnotationPropertyPair<T, A>> = mutableListOf()
    val propertyPairs: List<AnnotationPropertyPair<T, A>> = propertyPairsBacking

    init {
        if(propertyPairs.isNotEmpty()){
            propertyPairsBacking.addAll(propertyPairs)
        }
    }

    fun updateConverting(
        receiver:T,
        nameValuePair: Pair<String, Any>,
        onUpdated:(T.(AnnotationPropertyPair<T, A>)-> Unit)? = null
    ): Boolean{
        propertyPairs.firstOrNull { it.name == nameValuePair.first && it.mutable }?.let {annotatedPair->
            val mutableProperty = annotatedPair.property as KMutableProperty1<T, Any>
           return if(mutableProperty.updateConverting(receiver, nameValuePair.second)){
                "Success".output(Colour.GREEN)
               onUpdated?.invoke(receiver, annotatedPair)
               true
            }else{
                "Update failure".output(Colour.Gray)
               false
            }
        }
        return false
    }


    fun updateConverting(
        receiver:T,
        values: List<Pair<String, Any>>,
        onUpdated:(T.(AnnotationPropertyPair<T, A>)-> Unit)? = null
    ): Boolean{
        var result = false
        values.forEach {nameValuePair->
            result =  updateConverting(receiver, nameValuePair, onUpdated)
        }
        return result
    }

    fun addPair(pair: AnnotationPropertyPair<T, A>, mutable: Boolean):AnnotationContainer<T,A>{
        propertyPairsBacking.add(pair)
        pair.mutable = mutable
        return this
    }

    fun addPair(property:KProperty1<T, Any>, annotation :A):AnnotationContainer<T,A> {
        val pair = AnnotationPropertyPair(property, annotation)
        propertyPairsBacking.add(pair)
        return this
    }

    fun addPair(property: KMutableProperty1<T, Any>, annotation :A):AnnotationContainer<T,A> {
        val pair = AnnotationPropertyPair(property, annotation)
        pair.mutable = true
        propertyPairsBacking.add(pair)
        return this
    }
}

inline fun <reified T: Any, reified A : Annotation> T.annotatedProperties(): AnnotationContainer<T, A> {
    val container = AnnotationContainer<T, A>()
    val kClass = this::class
    val annotated = kClass.memberProperties.filter { it.hasAnnotation<A>() }
    val selected = annotated.filterIsInstance<KProperty1<T, Any>>()
    selected.forEach {prop->
        val ann = prop.findAnnotation<A>()
        if (ann != null){
          prop.testMutability()?.let {
              container.addPair(it, ann)
          }?:run {
              container.addPair(prop, ann)
          }
        }
    }
    return container
}