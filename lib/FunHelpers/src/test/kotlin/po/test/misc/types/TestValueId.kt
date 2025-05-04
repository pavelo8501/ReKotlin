package po.test.misc.types

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.test.Test

class TestValueId {


    interface IndexedContainer {
        operator fun get(index: Int): Int
    }

    @JvmInline
    value class InlineValue(val value: Int): Comparable<Int>{
        override operator fun compareTo(other: Int): Int{
             return value
        }

        operator fun get(i: Int): Int { return value }
      //  operator fun set(i: Int, value: Int) { this.value = value }

        operator fun<T: Number>  get(index: T): Int {
            return value
        }
    }

  data class DataValue(var value: Int)  : ReadOnlyProperty<Int, Int> {

      operator fun <T> get(value: T): Int {
          return value as Int
      }

      operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        this.value = value
      }

      override fun getValue(thisRef: Int, property: KProperty<*>): Int {
          return value
      }
  }



    @Test
    fun valueType(){



    }

}