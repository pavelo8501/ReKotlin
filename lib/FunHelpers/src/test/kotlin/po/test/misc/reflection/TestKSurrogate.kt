package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.data.delegates.propertyBinding
import po.misc.context.Identifiable
import po.misc.context.asIdentity
import po.misc.reflection.classes.KSurrogate
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

class TestKSurrogate {

    data class Source(
        var field1: Int = 10,
        var field2: String = "Source",
        var field3: Boolean = false
    )

    data class SecondSource(
        var int: Int = 50,
        var str: String = "SecondSource",
        var bool: Boolean = true
    )

    data class SourceContainer(
        val  kClass: KClass<Source> = Source::class,
        val  field1: KMutableProperty1<Source, Int> = Source::field1,
        val  field2: KMutableProperty1<Source, String> = Source::field2,
        val  field3: KMutableProperty1<Source, Boolean> = Source::field3
    )

    class MainObject(): CTX {

        override val identity: CTXIdentity<out CTX> = asIdentity()

        override val contextName: String get() = "MainObject"

        val source: Source = Source()
        val second : SecondSource = SecondSource()

        fun updateSource(data: Source){

        }
        fun updateSecond(data: SecondSource){

        }


       // val surrogate: KSurrogate<MainObject> = KSurrogate<MainObject>(this)


            //secondBacking = createSource<SecondSource>()
            //dataSourceBacking = createSource<Source>()




//            onPropertyProvided = { sourceProperty ->
//                println("ctx.onPropertyProvided  invoked. Old variant")
//                createPropertyGroup(sourceProperty) {
//                    listOf(source, second).forEach {dataSource->
//                        dataSource.provideGroup(this)
//                    }
//                }
//            }
//        val intAuxArray = arrayOf(propertySlot(source, Source::field1), propertySlot(second, SecondSource::intNameDifferent))
//
//        val strAuxArray = arrayOf(propertySlot(source, Source::field2), propertySlot(second, SecondSource::strNameDifferent))
//
//        var intParam: Int by propertyBinding(surrogate, Int::class)
//        var strParam: String by propertyBinding(surrogate, String::class)
//        var boolParam: Boolean by propertyBinding(surrogate, Boolean::class)
    }

    @Test
    fun `Surrogate DSL type configuration`(){


//
//        val managed = MainObject()
//        val surrogate = managed.surrogate
//
//        val group = assertNotNull(surrogate.getGroup<Int>("intParam"))
//
//        assertEquals(2, group.propertySlots.size, "Aux data sources not registered")
//        assertEquals(2, managed.source.propertyGroups.size, "DataSource did not received group")
//        assertEquals(2, managed.source.propertyMap.size)
//        val fromGroup = group.propertySlots.values.first().property
//        val fromUpdateSource = managed.source.propertyMap.values.first()
//        assertSame(fromGroup as PropertyIO<*, *>, fromUpdateSource as PropertyIO<*, *>)
    }

    @Test
    fun `Source properties are updated as expected`(){


//
//        val managed = MainObject()
//        val surrogate = managed.surrogate
//
//        val update = Source(1, "Source")
//        val second = SecondSource(2, "SecondSource")
//
//        managed.source.provideData(update)
//        assertEquals(update.field1, managed.intParam)
//        assertEquals(update.field2, managed.strParam)
//
//        managed.second.provideData(second)
//        assertEquals(second.intNameDifferent, managed.intParam)
//        assertEquals(second.strNameDifferent, managed.strParam)



    }

}