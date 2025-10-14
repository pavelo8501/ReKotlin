package po.misc.collections

import po.misc.context.CTX
import po.misc.context.ContextInfo
import po.misc.context.ContextualHelper
import po.misc.data.PrettyPrint
import po.misc.data.logging.EmittableClass
import po.misc.data.logging.LogEmitterClass
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.reflection.TypeDefaults
import po.misc.reflection.defaultForType
import po.misc.types.type_data.TypeData
import po.misc.types.getOrManaged
import po.misc.types.helpers.simpleOrNan
import po.misc.types.token.TypeToken
import java.time.Instant


enum class BufferAction{
    Buffer,
    Commit,
    Ignore
}

enum class BufferItemStatus{
    Commited,
    SameAsRecent,
    Buffered,
    Unresolved
}

open class BufferItem<T, P>(
    val value:T,
    val providedBy: String,
    val providedByHash: Int,
    val parameter:P? = null
):PrettyPrint{

    var itemStatus: BufferItemStatus = BufferItemStatus.Buffered
        internal set
    val created: Instant = Instant.now()

    override val formattedString: String get() {
        val colorizedType = when(itemStatus){
            Commited-> { itemStatus.name.colorize(Colour.Green) }
            SameAsRecent->{ itemStatus.name.colorize(Colour.Magenta) }
            Buffered->{ itemStatus.name.colorize(Colour.Yellow) }
            Unresolved ->{ itemStatus.name.colorize(Colour.Red) }
        }
        return "Value: $value Item type: $colorizedType"
    }

    internal fun changeStatus(status:BufferItemStatus):BufferItem<T, P>{
        itemStatus = status
        return this
    }

    override fun toString(): String = "$itemStatus Value: ${value} Item type: $itemStatus"
}



interface BufferClass<T, P>{
    val value: T?
    fun add(data: T, parameter: P? = null)
    fun onValueReceived(callback:(BufferItem<T, P>)-> BufferAction)
    fun onCommit(callback: (T)-> Unit)
    fun onSameAsRecent(callback: (BufferItem<T, P>)-> BufferAction)
    fun getValue(callingContext: Any):T?
    fun flush()
}

open class SlidingBuffer<T: Any, P: Any>(
    private val host: Any,
    val capacity: Int = 5,
    val typeData: TypeToken<T>,
    private val emitter: LogEmitterClass = LogEmitterClass(host),
    val defaultValueProvider: (()-> T)?
): EmittableClass by emitter, ContextualHelper, PrettyPrint, TypeDefaults, BufferClass<T, P>  {

    val hostName: String get() = when(host){
        is CTX-> host.identifiedByName
        else -> host::class.simpleOrNan()
    }
    val typeName: String by lazy {
        "<${typeData.typeName}>".colorize(Colour.BlueBright)
    }

    protected val identifiedAs: String by lazy {
        "SlidingBuffer<${typeData.typeName}>[${hostName}]>"
    }

    override val formattedString: String get() {
        val items =  buffer.joinToString(prefix = "->", separator =  "${SpecialChars.newLine}->") {
            "$it @ ${it.created} by ${it.providedBy} with hash: ${it.providedByHash}"
        }
        return "$identifiedAs ${SpecialChars.newLine}$items"
    }

    private val buffer = mutableListOf<BufferItem<T, P>>()

    val recentItem:BufferItem<T, P>? get() = buffer.firstOrNull()
    val size: Int get() = buffer.size
    override val value: T? get() = recentItem?.value

    private var valueReceived: ((BufferItem<T, P>)-> BufferAction)? = null
    private var commitCallback: ((T)-> Unit)? = null
    private var sameAsRecent: ((BufferItem<T, P>)-> BufferAction)? = null

    protected open val noCommitFunctionMsg: (BufferItem<T, P>) -> String ={
        "Received Commit command but no onCommit function provided while processing: $it" + SpecialChars.newLine +
        "Stored as ${it.itemStatus.name}"
    }

    protected open val resultDemandedBeforeInitMsg: (TypeToken<T>, Boolean) -> String = {type, result->
        val resultStr = if(result){
             "Providing default value available for type ${type.typeName}"
        }else{ "Unable to provide default for type${type.typeName}" }
        "Result demanded before its initialization."+ SpecialChars.newLine + resultStr
    }

    private fun removeIfExceeded(){
        if (buffer.size >= capacity) {
            buffer.removeLast()
        }
    }
    private fun addChecking(item:  BufferItem<T, P>){
        removeIfExceeded()
        buffer.add(0, item)
    }
    private fun checkIfSame(newValue:T):BufferItemStatus{
      return  if(newValue === value){
          BufferItemStatus.SameAsRecent
        }else{
          BufferItemStatus.Buffered
        }
    }
    private fun processSame(item:  BufferItem<T, P>){
        sameAsRecent?.let { sameCallback ->
            if (sameCallback.invoke(item) == BufferAction.Commit) {
                commitCallback?.let { commitCallback ->
                    commitCallback.invoke(item.value)
                    item.changeStatus(BufferItemStatus.Commited)
                    addChecking(item)
                }?:run {
                    item.changeStatus(BufferItemStatus.Unresolved)
                    addChecking(item)
                    warn(noCommitFunctionMsg(item))
                }
            }else{
                item.changeStatus(BufferItemStatus.Buffered)
                addChecking(item)
            }
        }?:run {
            item.changeStatus(BufferItemStatus.Buffered)
            addChecking(item)
        }
    }
    private fun processDifferent(item:  BufferItem<T, P>){
        if (valueReceived?.invoke(item) == BufferAction.Commit) {
            commitCallback?.let {
                it.invoke(item.value)
                item.changeStatus(BufferItemStatus.Commited)
            }?:run {
                item.changeStatus(BufferItemStatus.Unresolved)
                addChecking(item)
                warn(noCommitFunctionMsg(item))
            }
        }
        addChecking(item)
    }

    override fun onValueReceived(callback:(BufferItem<T, P>)-> BufferAction){
        valueReceived = callback
    }
    override fun onCommit(callback: (T)-> Unit){
        commitCallback = callback
    }
    override fun onSameAsRecent(callback: (BufferItem<T, P>)-> BufferAction){
        sameAsRecent = callback
    }

    protected open fun constructBufferItem(
        value:T,
        info:  ContextInfo,
        parameter:P?
    ):BufferItem<T, P>{
        return BufferItem(
            value,
            info.contextName,
            info.contextHash,
            parameter
        ).changeStatus(checkIfSame(value))
    }

    internal fun addWithCalling(
        callingContext: Any,
        data: T,
        parameter:P? = null
    ) {
        val item = constructBufferItem(data, callingName(callingContext), parameter)
        if (item.itemStatus == BufferItemStatus.SameAsRecent) {
            processSame(item)
        }else{
            processDifferent(item)
        }
    }

    override fun getValue(callingContext: Any):T{
        return value?:run {
           val default =  defaultForType(typeData.kClass)
            if(default == null){
               warn(resultDemandedBeforeInitMsg(typeData, false))
            }else{
                info(resultDemandedBeforeInitMsg(typeData, true))
            }
            default.getOrManaged(callingContext, Any::class)
        }
    }
    override fun add(data: T, parameter:P?) {
        addWithCalling(host, data)
    }

    fun listAsInBuffer(): List<BufferItem<T, P>> = buffer
    fun listOldestFirst(): List<BufferItem<T, P>>{
        val reversed = buffer.toMutableList()
        reversed.reverse()
        return reversed
    }

    override fun flush() {
        recentItem?.let {
            if (it.itemStatus == BufferItemStatus.Buffered) {
                commitCallback?.let { commitCallback ->
                    commitCallback.invoke(it.value)
                    it.changeStatus(BufferItemStatus.Commited)
                }?: run {
                    it.changeStatus(BufferItemStatus.Unresolved)
                    addChecking(it)
                    warn(noCommitFunctionMsg(it))
                }
            }
        }
    }

    fun clear(): Unit = buffer.clear()

    operator fun get(index: Int):BufferItem<T, P>? {
        return buffer.getOrNull(index)
    }

    override fun toString(): String {
      val items =  buffer.joinToString(prefix = "->", separator =  "${SpecialChars.newLine}->") {
          "$it @ ${it.created} by ${it.providedBy} with hash: ${it.providedByHash}"
      }
       return "$identifiedAs ${SpecialChars.newLine}$items"
    }

    companion object{
        inline operator fun <reified T : Any, P: Any> invoke(
            host: Any,
            capacity: Int = 5,
            noinline defaultValueProvider: (()-> T)? = null
        ): SlidingBuffer<T, P> {
            return SlidingBuffer(host, capacity, TypeToken.create<T>(), defaultValueProvider =  defaultValueProvider)
        }

        operator fun <T : Any, P: Any> invoke(
            host: Any,
            typeData: TypeToken<T>,
            capacity: Int = 5,
            defaultValueProvider: (()-> T)? = null
        ): SlidingBuffer<T, P> {
            return SlidingBuffer(host, capacity, typeData, defaultValueProvider =  defaultValueProvider)
        }
    }
}



open class Buffer<T: Any?, P: Any>(
    private val host: Any,
    val capacity: Int = 5,
    private val emitter: LogEmitterClass = LogEmitterClass(host)
): EmittableClass by emitter, ContextualHelper, PrettyPrint, TypeDefaults, BufferClass<T, P> {

    val hostName: String get() = when(host){
        is CTX-> host.identifiedByName
        else -> host::class.simpleOrNan()
    }

    protected val identifiedAs: String by lazy {
        "Buffer[${hostName}]>"
    }

    override val formattedString: String get() {
        val items =  buffer.joinToString(prefix = "->", separator =  "${SpecialChars.newLine}->") {
            "$it @ ${it.created} by ${it.providedBy} with hash: ${it.providedByHash}"
        }
        return "$identifiedAs ${SpecialChars.newLine}$items"
    }

    private val buffer = mutableListOf<BufferItem<T, P>>()

    val recentItem:BufferItem<T, P>? get() = buffer.firstOrNull()
    val size: Int get() = buffer.size
    override val value: T? get() = recentItem?.value

    private var valueReceived: ((BufferItem<T, P>)-> BufferAction)? = null
    private var commitCallback: ((T)-> Unit)? = null
    private var sameAsRecent: ((BufferItem<T, P>)-> BufferAction)? = null

    protected open val noCommitFunctionMsg: (BufferItem<T, P>) -> String ={
        "Received Commit command but no onCommit function provided while processing: $it" + SpecialChars.newLine +
                "Stored as ${it.itemStatus.name}"
    }


    private fun removeIfExceeded(){
        if (buffer.size >= capacity) {
            buffer.removeLast()
        }
    }
    private fun addChecking(item:  BufferItem<T, P>){
        removeIfExceeded()
        buffer.add(0, item)
    }
    private fun checkIfSame(newValue:T):BufferItemStatus{
        return  if(newValue === value){
            BufferItemStatus.SameAsRecent
        }else{
            BufferItemStatus.Buffered
        }
    }
    private fun processSame(item:  BufferItem<T, P>){
        sameAsRecent?.let { sameCallback ->
            if (sameCallback.invoke(item) == BufferAction.Commit) {
                commitCallback?.let { commitCallback ->
                    commitCallback.invoke(item.value)
                    item.changeStatus(BufferItemStatus.Commited)
                    addChecking(item)
                }?:run {
                    item.changeStatus(BufferItemStatus.Unresolved)
                    addChecking(item)
                    warn(noCommitFunctionMsg(item))
                }
            }else{
                item.changeStatus(BufferItemStatus.Buffered)
                addChecking(item)
            }
        }?:run {
            item.changeStatus(BufferItemStatus.Buffered)
            addChecking(item)
        }
    }
    private fun processDifferent(item:  BufferItem<T, P>){
        if (valueReceived?.invoke(item) == BufferAction.Commit) {
            commitCallback?.let {
                it.invoke(item.value)
                item.changeStatus(BufferItemStatus.Commited)
            }?:run {
                item.changeStatus(BufferItemStatus.Unresolved)
                addChecking(item)
                warn(noCommitFunctionMsg(item))
            }
        }
        addChecking(item)
    }

    override fun onValueReceived(callback:(BufferItem<T, P>)-> BufferAction){
        valueReceived = callback
    }
    override fun onCommit(callback: (T)-> Unit){
        commitCallback = callback
    }
    override fun onSameAsRecent(callback: (BufferItem<T, P>)-> BufferAction){
        sameAsRecent = callback
    }

    protected open fun constructBufferItem(
        value:T,
        info:  ContextInfo,
        parameter:P?
    ):BufferItem<T, P>{
        return BufferItem(
            value,
            info.contextName,
            info.contextHash,
            parameter
        ).changeStatus(checkIfSame(value))
    }

    internal fun addWithCalling(
        callingContext: Any,
        data: T,
        parameter:P? = null
    ) {
        val item = constructBufferItem(data, callingName(callingContext), parameter)
        if (item.itemStatus == BufferItemStatus.SameAsRecent) {
            processSame(item)
        }else{
            processDifferent(item)
        }
    }

    override fun getValue(callingContext: Any):T?{
        return value
    }

    override fun add(data: T, parameter:P?) {
        addWithCalling(host, data)
    }

    fun listAsInBuffer(): List<BufferItem<T, P>> = buffer
    fun listOldestFirst(): List<BufferItem<T, P>>{
        val reversed = buffer.toMutableList()
        reversed.reverse()
        return reversed
    }

    override fun flush() {
        recentItem?.let {
            if (it.itemStatus == BufferItemStatus.Buffered) {
                commitCallback?.let { commitCallback ->
                    commitCallback.invoke(it.value)
                    it.changeStatus(BufferItemStatus.Commited)
                }?: run {
                    it.changeStatus(BufferItemStatus.Unresolved)
                    addChecking(it)
                    warn(noCommitFunctionMsg(it))
                }
            }
        }
    }

    fun clear(): Unit = buffer.clear()

    operator fun get(index: Int):BufferItem<T, P>? {
        return buffer.getOrNull(index)
    }

    override fun toString(): String {
        val items =  buffer.joinToString(prefix = "->", separator =  "${SpecialChars.newLine}->") {
            "$it @ ${it.created} by ${it.providedBy} with hash: ${it.providedByHash}"
        }
        return "$identifiedAs ${SpecialChars.newLine}$items"
    }

    companion object{
        inline operator fun <reified T : Any, P: Any> invoke(
            host: Any,
            capacity: Int = 5,
            noinline defaultValueProvider: (()-> T)? = null
        ): SlidingBuffer<T, P> {
            return SlidingBuffer(host, capacity, TypeToken.create<T>(), defaultValueProvider =  defaultValueProvider)
        }

        operator fun <T : Any, P: Any> invoke(
            host: Any,
            typeData: TypeToken<T>,
            capacity: Int = 5,
            defaultValueProvider: (()-> T)? = null
        ): SlidingBuffer<T, P> {
            return SlidingBuffer(host, capacity, typeData, defaultValueProvider =  defaultValueProvider)
        }
    }
}


fun <T: Any, P: Any> Any.addToBuffer(
    buffer:SlidingBuffer<T, P>,
    value:T?,
    parameter:P? = null
):T?{
    if(value != null){
        buffer.addWithCalling(this, value, parameter)
    }
   return value
}

