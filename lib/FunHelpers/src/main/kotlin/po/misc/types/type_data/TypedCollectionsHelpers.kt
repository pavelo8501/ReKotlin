package po.misc.types.type_data

import po.misc.data.helpers.output
import po.misc.data.styles.SpecialChars
import po.misc.types.safeCast
import po.misc.types.token.TokenHolder
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class CompareResult(
   val kClass: KClass<*>,
   val kType: KType?
){
    val testClassName: String = kClass.qualifiedName?:""
    val testedKTypeHash: Int ? = kType?.hashCode()

    val records = mutableListOf<CompareToRecord>()

    fun addResult(result: Boolean,  comparedTo: TypeToken<*>): Boolean {
         val  record: CompareToRecord = CompareToRecord(result)
        record.fromTypeToken(comparedTo)
        records.add(record)
        return result
    }
    override fun toString(): String {
        val results = records.joinToString(separator = SpecialChars.NEW_LINE) {record->
            "CompareTo : ${record.compareToClassName} " +
                    "Generic Params: ${record.genericsToString()}" +
                    "KTypeHash: ${record.compareToKTypeHash} Result: ${record.result} " + record.toString()

        }
      return  buildString {
            appendLine("Comparing ${testClassName}  KTypeHash: $testedKTypeHash")
            appendLine(results)
        }
    }
}

data class CompareToRecord(
    val result: Boolean,
){
    var compareToClassName: String = ""
    var compareToKTypeHash: Int = 0
    var genericTypes: List<String> = emptyList()

    var token: TypeToken<*>? = null


    fun fromTypeToken(token: TypeToken<*>){
        compareToClassName = token.kClass.qualifiedName?:""
        compareToKTypeHash = token.kClass.hashCode()
        genericTypes =  token.inlinedParamClasses.map { it.simpleName?:"" }
        this.token = token
    }


    fun genericsToString(): String{
       return genericTypes.joinToString(separator = ",") {
            it
        }
    }
    override fun toString(): String {
       return token.toString()
    }
}

@Deprecated("Change to filterByType")
fun <T>  List<TokenHolder>.typedFilter(
    compareTo: TypeToken<T>,
    predicate: ((T)-> Boolean)? = null
): List<T> where T: Any {
    val result = mutableListOf<T>()

    val filteredList = filter { typed ->
        typed.typeToken.kType == compareTo.kType
    }
    for (filteredItem in filteredList) {
        filteredItem.safeCast<T>(compareTo.kClass)?.let {
            result.add(it)
        }
    }
    predicate?.let {
        result.filter(it)
    }
    return result
}

@Deprecated("Change to filterByType")
inline fun <reified T>  List<TokenHolder>.typedFilter(
    vararg typeParameterClasses : KClass<*>
): List<T> where T: TokenHolder {

    val kType =  typeOf<T>()
    val kClass = T::class

    val additional = typeParameterClasses.toList().sortedBy { it.simpleName }
    val compareResults =  CompareResult(kClass, kType)

    val castedList = mapNotNull { it.safeCast<T>() }

    val filteredList =  castedList.filter { typed ->
      val result =  if(additional.isNotEmpty()){
            typed.typeToken.stricterEquality(kClass, *typeParameterClasses)
        }else{
            typed.typeToken.kClass == kClass
        }
        compareResults.addResult(result, typed.typeToken)
    }

    val selected =  compareResults.records.any { it.result }
    if(!selected){
        compareResults.output()
    }
    return filteredList
}

@Deprecated("Change to filterByType")
inline fun <reified T> List<TokenHolder>.typedFilter(
    predicate: (T)-> Boolean,
): List<T> where T: Any,  T: TokenHolder {
   return typedFilter<T>().filter(predicate)
}

fun <T> List <TokenHolder>.typedFilter(
    typeToken: TypeToken<T>
): List<T>  where  T: TokenHolder{

    return filter { it.typeToken == typeToken}.mapNotNull {
        it.safeCast(typeToken.kClass)
    }
}

@Deprecated("Change to filterByType")
fun <T : Any>  List<Any>.findByTypeFirstOrNull(
    typeData: TypeData<T>,
):T? {
    return firstNotNullOfOrNull { it.safeCast(typeData.kClass) }
}

fun <T : Any> MutableMap<KClass<out T>, MutableList<T>>.add(value: T) {
    val kClass = value::class
    getOrPut(kClass) { mutableListOf() }.add(value)
}


fun <T> MutableMap<KClass<out T>, MutableList<T>>.select(typeData: TypeToken<T>): List<T> where T : Any, T: TokenHolder{
    val result = this[typeData.kClass]?.let {
        it.typedFilter(typeData)
       val filterResult = it.typedFilter(typeData)
        filterResult
    } ?: run {
        emptyList<T>()
    }
    return result
}