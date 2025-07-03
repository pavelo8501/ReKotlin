package po.misc.types

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <reified T: Any> T.getKType(): KType {
    return typeOf<T>()
}

fun KType.asStableString(): String {
    val qualifiedTypeName = classifier?.let {
        (it as? KClass<*>)?.qualifiedName
    } + (if (isMarkedNullable) "?" else "")
    return qualifiedTypeName
}

fun KType.toSimpleNormalizedKey(): String {
    val classifier = this.classifier
    return when (classifier) {
        is KClass<*> -> {
            val args = this.arguments
            if (args.isNotEmpty()) {
                val argClass = args.first().type?.classifier as? KClass<*>
                if (argClass != null) {
                    "List:${argClass.qualifiedName}"
                } else classifier.simpleName.toString()
            } else classifier.simpleName.toString()
        }
        else -> classifier.toString()
    }
}

fun  KClassifier?.simplifiedName(): String{
   return this?.let {
        (it as KClass<*>).simpleName.toString()
    }?:"Null"
}
