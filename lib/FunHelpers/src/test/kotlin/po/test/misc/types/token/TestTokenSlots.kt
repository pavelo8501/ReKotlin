package po.test.misc.types.token

import po.misc.data.output.output
import po.misc.types.token.TypeToken
import po.misc.types.token.asListType
import kotlin.reflect.KProperty1
import kotlin.test.Test

class TestTokenSlots {

    val listProperty: List<FakeClass> = listOf(FakeClass())
    val property: FakeClass = FakeClass()
    class FakeClass{ val param: String = "test" }

    class FakeUpperContainer<T, V>(
        val property: KProperty1<T, V>,
        val classToken : TypeToken<FakeContainer<T, V>>,
        val valueToken : TypeToken<V>
    ){
        inline fun <V, reified T2> buildContainer(property: KProperty1<V, T2>, builder: FakeContainer<V, T2>.() -> Unit){
            builder.invoke(FakeContainer(property,  TypeToken<FakeContainer<V, T2>>()))
        }
    }
    class FakeContainer<T, V>(
        val property: KProperty1<T, V>,
        val fakeClassToken : TypeToken<FakeContainer<T, V>>
    )
    inline fun <T, reified V> scopeProperty(property: KProperty1<T, V>, scope: FakeUpperContainer<T, V>.() -> Unit){
         val container = FakeUpperContainer(property, TypeToken<FakeContainer<T, V>>(), TypeToken<V>())
         scope.invoke(container)
    }

    @Test
    fun `Test type inference`(){
        scopeProperty(TestTokenSlots::listProperty){

            classToken.output("classToken")
            valueToken.output("valueToken")
            buildContainer(FakeClass::param){
                fakeClassToken.output("fakeClassToken")
            }
        }
    }


    @Test
    fun `Token as list`(){
        val token = TypeToken<FakeContainer<TestTokenSlots, FakeClass>>()
        val lisToken  = token.asListType()
        lisToken.typeSlots.forEach { slot ->
            slot.output()
            slot.typeLitera.output("typeLitera")
            slot.parameterName.output("parameterName")
        }
    }

}