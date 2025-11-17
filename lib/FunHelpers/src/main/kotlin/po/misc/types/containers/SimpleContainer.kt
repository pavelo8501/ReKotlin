package po.misc.types.containers

import po.misc.types.castListOrManaged
import po.misc.types.token.TypeToken


sealed interface SingleOrList<T: Any>{
    enum class CastStatus{
        NotCasted,
        Casted
    }

    fun <T2: Any> listOfType(typeData: TypeToken<T2>): List<T2>
    fun valueAsList(): List<T>
}

open class Single<T: Any>(val value: T): SingleOrList<T>{
    var status: SingleOrList.CastStatus = SingleOrList.CastStatus.NotCasted
        private set

    override fun valueAsList(): List<T>{
        return listOf(value)
    }
    override fun <T2: Any> listOfType(typeData: TypeToken<T2>): List<T2>{
        val list = valueAsList()
        if(status == SingleOrList.CastStatus.Casted){
            @Suppress("UNCHECKED_CAST")
            return list as List<T2>
        }
        val casted = list.castListOrManaged(this, typeData.kClass)
        status = SingleOrList.CastStatus.Casted
        return casted
    }
}

open class Multiple<T: Any>(val values: List<T>): SingleOrList<T>{

    var status: SingleOrList.CastStatus = SingleOrList.CastStatus.NotCasted
        private set

    override fun valueAsList(): List<T>{
        return values
    }
    override fun <T2: Any> listOfType(typeData: TypeToken<T2>): List<T2>{
        val list = valueAsList()
        if(status == SingleOrList.CastStatus.Casted){
            @Suppress("UNCHECKED_CAST")
            return list as List<T2>
        }
        val casted = list.castListOrManaged(this, typeData.kClass)
        status = SingleOrList.CastStatus.Casted
        return casted
    }
}
