package po.misc.types.type_data

import po.misc.data.styles.SpecialChars
import po.misc.types.helpers.filterByType
import po.misc.types.token.TokenHolder
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KType

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
        genericTypes =  token.inlinedParameters.map { it.simpleName?:"" }
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



fun <T : Any> MutableMap<KClass<out T>, MutableList<T>>.add(value: T) {
    val kClass = value::class
    getOrPut(kClass) { mutableListOf() }.add(value)
}

fun <T> MutableMap<KClass<out T>, MutableList<T>>.select(typeData: TypeToken<T>): List<T> where T : Any, T: TokenHolder{
    val result = this[typeData.kClass]?.let {
        it.filterByType(typeData)
       val filterResult = it.filterByType(typeData)
        filterResult
    } ?: run {
        emptyList<T>()
    }
    return result
}