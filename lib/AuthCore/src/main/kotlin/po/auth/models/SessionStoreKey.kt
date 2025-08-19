package po.auth.models

import po.auth.sessions.interfaces.SessionIdentified

class SessionStoreKey(
    override val ip: String,
    override val userAgent: String
): SessionIdentified {

    override fun equals(other: Any?): Boolean {
        var result = false
        if(other is SessionStoreKey){
            if(other.userAgent == userAgent && other.ip == ip ){
                result = true
            }
        }
        return result
    }

    override fun hashCode(): Int {
        var result = ip.hashCode()
        result = 31 * result + userAgent.hashCode()
        return result
    }

    companion object{
        fun createFrom(source: SessionIdentified):SessionStoreKey{
           return SessionStoreKey(source.ip, source.userAgent)
        }
    }

}