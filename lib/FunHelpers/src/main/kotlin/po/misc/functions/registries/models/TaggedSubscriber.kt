package po.misc.functions.registries.models

import po.misc.functions.registries.RegistryKey
import kotlin.reflect.KClass


class TaggedSubscriber<E: Enum<E>>(
    val enumTag:E,
    val kClass: KClass<*>,
    override val requireOnce: Boolean
): RegistryKey{

    var subscriberID: Long = 0

    fun setID(id: Long):TaggedSubscriber<E>{
        subscriberID = id
        return this
    }

    override fun matchesWildcard(other: RegistryKey): Boolean {
        return if (other is TaggedSubscriber<*>){
            enumTag == other.enumTag
                    && kClass == other.kClass
                    && (subscriberID == other.subscriberID || subscriberID == 0L || other.subscriberID == 0L)
        }else{
            false
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is TaggedSubscriber<*> &&
                enumTag == other.enumTag &&
                kClass == other.kClass &&
                subscriberID == other.subscriberID
    }
    override fun hashCode(): Int {
        var result = enumTag.hashCode()
        result = 31 * result + kClass.hashCode()
        result = 31 * result + subscriberID.hashCode()
        return result
    }

    override fun toString(): String {
        return "Subscription[tag=${enumTag.name}, kClass=${kClass.simpleName?:"N/A"}, requireOnce=$requireOnce]"
    }
}