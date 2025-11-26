package po.test.misc.collections.indexed

import org.junit.jupiter.api.Test
import po.misc.collections.indexed.Indexed
import po.misc.collections.indexed.IndexedList
import po.misc.collections.indexed.indexedListOf
import po.misc.collections.repeatBuild
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestIndexedList {

    private class IndexedClass : Indexed {
        var myIndex: Int = 0
            private set
        var totalCount: Int = 0
            private set
        val isFirst: Boolean get() = myIndex == 0
        val isLast: Boolean get() = myIndex == totalCount - 1

        override fun setIndex(index: Int, ofSize: Int) {
            myIndex = index
            totalCount = ofSize
        }
    }

    private val indexedList = IndexedList<IndexedClass>()

    @Test
    fun `Indexing work as intended`() {

        4.repeatBuild {
            val indexed = IndexedClass()
            indexedList.add(indexed)
            indexed
        }

        assertEquals(4, indexedList.size)
        val first =  indexedList.first()
        assertEquals(0, first.myIndex)
        assertEquals(4, first.totalCount)
        assertTrue { first.isFirst }

        val last =  indexedList.last()
        assertEquals(3, last.myIndex)
        assertEquals(4, last.totalCount)
        assertTrue { last.isLast }
    }

    @Test
    fun `Builder work as intended`() {
        val result = 4.repeatBuild {
           IndexedClass()
        }
        val list = indexedListOf<IndexedClass>()
        list.addAll(result)

        assertEquals(4, list.size)
        val first =  list.first()
        assertEquals(0, first.myIndex)
        assertEquals(4, first.totalCount)
        assertTrue { first.isFirst }

        val last =  list.last()
        assertEquals(3, last.myIndex)
        assertEquals(4, last.totalCount)
        assertTrue { last.isLast }
    }

    @Test
    fun `Builder by initial list work as intended`() {
        val list = indexedListOf(IndexedClass(), IndexedClass())
        assertEquals(2, list.size)
        val first =  list.first()
        assertEquals(0, first.myIndex)
        assertEquals(2, first.totalCount)
        assertTrue { first.isFirst }

        val last =  list.last()
        assertEquals(1, last.myIndex)
        assertEquals(2, last.totalCount)
        assertTrue { last.isLast }
    }
}