package po.misc.functions.registries.models

import po.misc.functions.registries.RegistryKey
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass


class SimpleSubscriber(
    val kClass: KClass<*>,
    override val requireOnce: Boolean
): RegistryKey{

    var subscriberID: Long = 0

    fun setID(id: Long):SimpleSubscriber{
        subscriberID = id
        return this
    }

    private fun kClassMatch(otherKClass: KClass<*>): Boolean{
        return kClass == otherKClass
    }

    private fun idMatch(otherSubscriberID: Long): Boolean{
        return subscriberID == otherSubscriberID
    }

    private fun otherId0(otherSubscriberID: Long): Boolean{
        return 0L == otherSubscriberID
    }

    override fun matchesWildcard(other: RegistryKey): Boolean {

        var result = false
        if(other is SimpleSubscriber){
            if(kClassMatch(other.kClass) && (idMatch(other.subscriberID) || subscriberID == 0L || otherId0(other.subscriberID))){
                result = true
            }
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return other is SimpleSubscriber &&
                kClass == other.kClass &&
                subscriberID == other.subscriberID
    }
    override fun hashCode(): Int {
        var result = 31 * kClass.hashCode()
        result = 31 * result + subscriberID.hashCode()
        return result
    }

    override fun toString(): String = "SimpleSubscriber[kClass=${kClass.simpleOrAnon}, requireOnce=$requireOnce]"
}