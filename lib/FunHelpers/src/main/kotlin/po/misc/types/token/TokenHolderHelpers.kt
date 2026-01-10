package po.misc.types.token

import po.misc.data.helpers.firstCharUppercase
import po.misc.data.output.output
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.error
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.safeCast
import kotlin.reflect.KClass


fun <T: TokenHolder, P> TokenHolder.safeCast(
    kClass: KClass<T>,
    typeToken: TypeToken<P>
):T?{
    val casted = this.safeCast<T>(kClass)
    if(casted != null && typeToken == casted.typeToken){
        return casted
    }
    return null
}

data class CastOptions(
    val output:Boolean = false,
    val useProjectedClass:Boolean = true,
)

data class CastLookupParameters(
    val sourceClass :KClass<*>,
    val targetClass:KClass<*>,
    val comparingTo: String = "",
    val receiverType: TypeToken<*>? = null,
    val targetType: TypeToken<*>? = null,
){
    val comparingToUpper: String = comparingTo.firstCharUppercase()?:""
}

@PublishedApi
internal val baseClassMsg : (CastLookupParameters) -> String = {params->
    """
        Type cast failed:
        - Source: ${params.sourceClass.simpleOrAnon}
        - Target: ${params.targetClass.simpleName}
        - Reason: Object does not implement required TokenizedResolver
        """.trimIndent()
}

@PublishedApi
internal val mismatchMsg : (CastLookupParameters) -> String = {params->
    """${params.comparingToUpper} type mismatch while casting TokenizedResolver:
        - Expected ${params.comparingTo}  meter type: ${params.receiverType?.typeName}
        - Actual  ${params.comparingTo} type: ${params.targetType?.typeName}
        - Target resolver: ${params.targetClass.simpleName}
        """.trimIndent()
}

@PublishedApi
internal fun  TokenizedResolver<*,*>.tokenizedParams(
    comparingTo: String,
    targetClass: KClass<*>,
    targetType: TypeToken<*>
):CastLookupParameters{
   return CastLookupParameters(this::class, targetClass, comparingTo, targetType)
}


inline fun <reified T: TokenizedResolver<RT, VT>, RT, VT> TokenizedResolver<*, *>.safeCast(
    sourceType: TypeToken<RT>,
    receiverType:TypeToken<VT>,
    options: CastOptions = CastOptions()
):T? {
    val sourceClass = this::class
    val targetClass = T::class
    val casted = safeCast<T>()
    return if(casted != null) {
        when {
            options.useProjectedClass && !casted.sourceType.effectiveClassIs(sourceType) -> {
                if (options.output) {
                    mismatchMsg(tokenizedParams("source", targetClass, sourceType)).output()
                }
                null
            }
            !options.useProjectedClass && casted.receiverType.effectiveClass != receiverType -> {
                if (options.output) {
                    mismatchMsg(tokenizedParams("receiver", targetClass, sourceType)).output()
                }
                null
            }
            options.useProjectedClass && !casted.receiverType.effectiveClassIs(receiverType)-> {
                if (options.output) {
                    mismatchMsg(tokenizedParams("receiver", targetClass, receiverType)).output()
                }
                null
            }
           !options.useProjectedClass && casted.receiverType.kClass != receiverType.kClass -> {
                if (options.output) {
                    mismatchMsg(tokenizedParams("receiver", targetClass, receiverType)).output()
                }
                null
            }
            else -> casted
        }
    }else {
        if (options.output) { baseClassMsg(CastLookupParameters(sourceClass, targetClass)).output() }
        null
    }
}

inline fun <reified T: TokenizedResolver<RT, *>, RT> TokenizedResolver<*, *>.safeCast(
    sourceType: TypeToken<RT>,
    options: CastOptions = CastOptions()
):T? {
    val sourceClass = this::class
    val targetClass = T::class
    val casted = safeCast<T>()
    return if(casted != null) {
        when {
            options.useProjectedClass && !casted.sourceType.effectiveClassIs(sourceType) -> {
                if (options.output) {
                    mismatchMsg(tokenizedParams("source", targetClass, sourceType)).output()
                }
                null
            }
           !options.useProjectedClass && casted.receiverType.effectiveClass != receiverType -> {
                if (options.output) {
                    mismatchMsg(tokenizedParams("receiver", targetClass, sourceType)).output()
                }
                null
            }
            else -> casted
        }
    }else {
        if(options.output) { baseClassMsg(CastLookupParameters(sourceClass, targetClass)).output() }
        null
    }
}



inline fun <reified T: TokenizedResolver<RT, VT>, RT, VT> TokenizedResolver<*, *>.castOrThrow(
    sourceType: TypeToken<RT>,
    receiverType:TypeToken<VT>
):T{
    val targetClass = T::class
    val baseClassMsg : () -> String = {
        """
            Type cast failed:
            - Source: ${this::class.simpleOrAnon}
            - Target: ${targetClass.simpleName}
            - Reason: Object does not implement required TokenizedResolver
            """.trimIndent()
    }
    val mismatchMsg : (param: String,  castedToken: TypeToken<*>) -> String = {param,  castedType->
        """${param.firstCharUppercase()} type mismatch while casting TokenizedResolver:
        - Expected $param type: $receiverType
        - Actual $param type: $castedType
        - Target resolver: ${targetClass.simpleName}
        """.trimIndent()
    }

  return safeCast<T>()?.let {casted->
       when{
            casted.sourceType != sourceType -> error(mismatchMsg("source", casted.sourceType), TraceOptions.PreviousMethod)
            casted.receiverType != receiverType -> error(mismatchMsg("receiver", casted.receiverType), TraceOptions.PreviousMethod)
            else -> casted
        }
    }?:run {
        error(baseClassMsg(), TraceOptions.PreviousMethod)
    }
}

inline fun <reified T: TokenHolder, P>  Iterable<TokenHolder>.filterTokenized(
    typeToken: TypeToken<P>,
): List<T>{
    return mapNotNull {
        it.safeCast(T::class, typeToken)
    }
}

inline fun <reified T: TokenHolder, reified P>  Iterable<Tokenized<*>>.filterTokenized(): List<T>{
   val casted =  mapNotNull {
        it.safeCast<T>()
   }
  val parameterClass = P::class
  return casted.filter { it.typeToken.effectiveClassIs(parameterClass) }
}

fun <T: TokenHolder, P> Iterable<TokenHolder>.filterTokenized(
    kClass: KClass<T>,
    typeToken: TypeToken<P>,
): List<T>{
    return mapNotNull { it.safeCast(kClass, typeToken) }
}

inline fun <reified T: TokenizedResolver<RT, VT>, RT, VT>  Iterable<TokenizedResolver<*, *>>.filterTokenized(
    receiverType: TypeToken<RT>,
    valueType: TypeToken<VT>,
    options: CastOptions = CastOptions()
): List<T>{
   return mapNotNull {
      it.safeCast<T, RT, VT>(receiverType, valueType, options)
   }
}
