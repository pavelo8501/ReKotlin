package po.misc.types.token

import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.data.styles.SpecialChars
import po.misc.types.TokenHolder
import po.misc.types.Tokenized
import po.misc.types.castOrThrow
import po.misc.types.helpers.simpleOrNan
import po.misc.types.helpers.toKeyParams
import po.misc.types.type_data.TypeDataCommon

import kotlin.collections.forEach
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.typeOf



class TypeToken<T: Any>  @PublishedApi internal constructor(
    val kClass: KClass<T>,
    val kType: KType
): Component {

    override var verbosity: Verbosity = Verbosity.Warnings

    override val componentName: String = "TypeToken[$simpleName]"

    private val typeSlotsBacking : MutableList<TypeSlot> = mutableListOf()

    val typeSlots: List<TypeSlot> =  typeSlotsBacking
    val hashCode: Int = kClass.hashCode()

    private val inlinedParameters: List<KClass<*>> get() = typeSlots.mapNotNull { it.ownClass }

    val inlinedParamClasses: List<KClass<*>> get() = inlinedParameters.sortedBy { it.simpleName }
    val inlinedParamsName : String get() = inlinedParameters.joinToString(separator = ", ") { it.simpleOrNan() }


    val simpleName : String get() = kClass.simpleName?:"Unknown"

    val typeName: String get() {
        return if (inlinedParameters.isNotEmpty()) {
            simpleName + inlinedParameters.joinToString(prefix = "<", separator = " ,", postfix = ">") {
                it.simpleName ?: "Unknown"
            }
        } else {
            simpleName
        }
    }

    init {
        typeSlotsBacking.addAll( kClass.typeParameters.map { TypeSlot(it) } )
        tryResolveImmediately()
    }

    private fun tryMapByUpperBounds(slot: TypeSlot, candidate:  KClass<*>): TypeSlot?  {
        for (bound in slot.upperBoundsClass) {
            if (candidate.isSubclassOf(bound)) {
                slot.ownClass =candidate
                return slot
            }
        }
        return null
    }

    fun tryResolveImmediately(){
        kType.arguments.forEachIndexed { index, arg ->
            val slot = typeSlotsBacking.getOrNull(index) ?: return@forEachIndexed
            arg.type?.let { argType ->
                (argType.classifier as? KClass<*>)?.let { klass ->
                    slot.ownClass = klass
                    slot.ownReifiedKType = argType
                }
            }
        }
    }

    fun addReifiedType(type: KType, type2: KType):TypeToken<T>{
        for (param in typeSlots){
            if(resolveReifiedForSlot(param,  type)){
                return this
            }else{
                resolveReifiedForSlot(param,  type2)
                return this
            }
        }
        warn("Provided $type can not be mapped to any type parameter", "addReifiedType")
        return this
    }

    fun addReifiedType(type: KType):TypeToken<T>{
        for (param in typeSlots){
            if(resolveReifiedForSlot(param,  type)){
                return this
            }
        }
        warn("Provided $type can not be mapped to any type parameter", "addReifiedType")
        return this
    }

    internal  fun resolveReifiedForSlot(slot: TypeSlot, reifiedType: KType): Boolean{
        notify("Processing reifiedType <KType> # $reifiedType", "resolveReifiedForSlot", verbosity)
        return  (reifiedType.classifier as? KClass<*>)?.let {paramClass->
            tryMapByUpperBounds(slot, paramClass)?.let {
                it.ownReifiedKType = reifiedType
                true
            }?:run {
                false
            }
        }?:run {
            warn("Not a KClass, while processing reifiedType <KType> # $reifiedType", "resolveReifiedForSlot")
            return false
        }
    }

    fun warnKClassDifferent(other: KClass<*>, methodName: String){
        val line1 = kClass.toKeyParams()
        val line2 = other.toKeyParams()
        val warnMsg = "Comparison failed when comparing own"+ SpecialChars.newLine + "$line1 to " + "$line2"
        warn(warnMsg, methodName)
    }

    override fun hashCode(): Int = kClass.hashCode()

    override fun equals(other: Any?): Boolean {
        if(other != null){
            var result : Boolean = false

            when(other){
                is TypeToken<*>->{
                    result =   kClass == other.kClass
                    if(!result){
                        warnKClassDifferent(other.kClass, "equals")
                    }
                }
                is KClass<*>->{
                    result = kClass == other
                    if(!result){
                        warnKClassDifferent(other, "equals")
                    }
                }
                is TypeDataCommon<*> ->{
                    result = kClass == other.kClass
                    if(!result){
                        warnKClassDifferent(other.kClass, "equals")
                    }
                }
                else -> false
            }
            return result
        }
        return false
    }

    fun stricterEquality(other: KClass<*>, vararg  typeParameters:KClass<*>): Boolean{

        if(kClass != other){
            if(verbosity == Verbosity.Warnings){
                warnKClassDifferent(other, "stricterEquality")
            }
            return false
        }
        for(paramClass in typeParameters.toList()){
            val result = inlinedParamClasses.firstOrNull { it == paramClass }
            if(result == null){
                return false
            }
        }
        return true
    }

    fun parametersDoNotAlign(other: List<KClass<*>>){
        other.forEach {

        }
    }

    fun stricterEquality(other: TypeToken<*>): Boolean {
        if (kClass != other.kClass){
            warnKClassDifferent(other.kClass, "stricterEquality")
            return false
        }

        if(inlinedParamClasses == other.inlinedParamClasses){
            warnKClassDifferent(other.kClass, "stricterEquality")
            return false
        }
        return true
    }

    fun printSlots(){
        typeSlots.joinToString(separator = SpecialChars.newLine) {
            it.toString()
        }
    }

    override fun toString(): String {
        return buildString {
            appendLine("SimpleName:$simpleName")
            appendLine("TypeName: $typeName")
            appendLine("Hash Code: $hashCode")
            appendLine("TypeSlots: "+ printSlots())
        }
    }

    companion object{
        inline fun <reified T: Any> create():TypeToken<T>{
            val newTypeData =  TypeToken(T::class,  typeOf<T>())
            return  newTypeData
        }
    }
}


class NullableTypeToken<T: Any?>  @PublishedApi internal constructor(
    val kClass: KClass<*>,
    val kType: KType
): Component {
    override var verbosity: Verbosity = Verbosity.Warnings

    override val componentName: String = "TypeToken[$simpleName]"

    private val typeSlotsBacking : MutableList<TypeSlot> = mutableListOf()

    val typeSlots: List<TypeSlot> =  typeSlotsBacking
    val hashCode: Int = kClass.hashCode()

    private val inlinedParameters: List<KClass<*>> get() = typeSlots.mapNotNull { it.ownClass }

    val inlinedParamClasses: List<KClass<*>> get() = inlinedParameters.sortedBy { it.simpleName }
    val inlinedParamsName : String get() = inlinedParameters.joinToString(separator = ", ") { it.simpleOrNan() }


    val simpleName : String get() = kClass.simpleName?:"Unknown"

    val typeName: String get() {
        return if (inlinedParameters.isNotEmpty()) {
            simpleName + inlinedParameters.joinToString(prefix = "<", separator = " ,", postfix = ">") {
                it.simpleName ?: "Unknown"
            }
        } else {
            simpleName
        }
    }

    init {
        typeSlotsBacking.addAll( kClass.typeParameters.map { TypeSlot(it) } )
        tryResolveImmediately()
    }

    private fun tryMapByUpperBounds(slot: TypeSlot, candidate:  KClass<*>): TypeSlot?  {
        for (bound in slot.upperBoundsClass) {
            if (candidate.isSubclassOf(bound)) {
                slot.ownClass =candidate
                return slot
            }
        }
        return null
    }

    fun tryResolveImmediately(){
        kType.arguments.forEachIndexed { index, arg ->
            val slot = typeSlotsBacking.getOrNull(index) ?: return@forEachIndexed
            arg.type?.let { argType ->
                (argType.classifier as? KClass<*>)?.let { klass ->
                    slot.ownClass = klass
                    slot.ownReifiedKType = argType
                }
            }
        }
    }

    internal  fun resolveReifiedForSlot(slot: TypeSlot, reifiedType: KType): Boolean{
        notify("Processing reifiedType <KType> # $reifiedType", "resolveReifiedForSlot", verbosity)
        return  (reifiedType.classifier as? KClass<*>)?.let {paramClass->
            tryMapByUpperBounds(slot, paramClass)?.let {
                it.ownReifiedKType = reifiedType
                true
            }?:run {
                false
            }
        }?:run {
            warn("Not a KClass, while processing reifiedType <KType> # $reifiedType", "resolveReifiedForSlot")
            return false
        }
    }

    fun warnKClassDifferent(other: KClass<*>, methodName: String){
        val line1 = kClass.toKeyParams()
        val line2 = other.toKeyParams()
        val warnMsg = "Comparison failed when comparing own"+ SpecialChars.newLine + "$line1 to " + "$line2"
        warn(warnMsg, methodName)
    }

    override fun hashCode(): Int = kClass.hashCode()

    override fun equals(other: Any?): Boolean {
        if(other != null){
            var result : Boolean = false

            when(other){
                is TypeToken<*>->{
                    result =   kClass == other.kClass
                    if(!result){
                        warnKClassDifferent(other.kClass, "equals")
                    }
                }
                is KClass<*>->{
                    result = kClass == other
                    if(!result){
                        warnKClassDifferent(other, "equals")
                    }
                }
                is TypeDataCommon<*> ->{
                    result = kClass == other.kClass
                    if(!result){
                        warnKClassDifferent(other.kClass, "equals")
                    }
                }
                else -> false
            }
            return result
        }
        return false
    }

    fun stricterEquality(other: KClass<*>, vararg  typeParameters:KClass<*>): Boolean{

        if(kClass != other){
            if(verbosity == Verbosity.Warnings){
                warnKClassDifferent(other, "stricterEquality")
            }
            return false
        }
        for(paramClass in typeParameters.toList()){
            val result = inlinedParamClasses.firstOrNull { it == paramClass }
            if(result == null){
                return false
            }
        }
        return true
    }

    fun parametersDoNotAlign(other: List<KClass<*>>){
        other.forEach {

        }
    }

    fun stricterEquality(other: TypeToken<*>): Boolean {
        if (kClass != other.kClass){
            warnKClassDifferent(other.kClass, "stricterEquality")
            return false
        }

        if(inlinedParamClasses == other.inlinedParamClasses){
            warnKClassDifferent(other.kClass, "stricterEquality")
            return false
        }
        return true
    }

    fun printSlots(){
        typeSlots.joinToString(separator = SpecialChars.newLine) {
            it.toString()
        }
    }

    override fun toString(): String {
        return buildString {
            appendLine("SimpleName:$simpleName")
            appendLine("TypeName: $typeName")
            appendLine("Hash Code: $hashCode")
            appendLine("TypeSlots: "+ printSlots())
        }
    }

    companion object{
        inline fun <reified T: Any> create():NullableTypeToken<T>{
            val newTypeData =  NullableTypeToken<T>(T::class as KClass<*>,  typeOf<T>())
            return  newTypeData
        }
    }
}




infix  fun List<KClass<*>>.typeClassesAlign(typeToken : TypeToken<*>): Boolean{
    val thisSorted = sortedBy { it.simpleName }
    val typeTokenClasses = typeToken.inlinedParamClasses

    return thisSorted == typeTokenClasses
}

infix fun TokenHolder.sameBaseClass(other: TokenHolder): Boolean =
    this.typeToken.kClass == other.typeToken.kClass
