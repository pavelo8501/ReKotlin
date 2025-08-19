package po.misc.interfaces

import java.time.Instant

interface ChangeEntry<T: Any>{
    val oldValue:T
    val newValue: T
    val created: Instant
}

interface HasChangeHistory<T:ChangeEntry<*>> {

    val changeHistory: List<T>

    fun getRecords(): List<T> = changeHistory

}