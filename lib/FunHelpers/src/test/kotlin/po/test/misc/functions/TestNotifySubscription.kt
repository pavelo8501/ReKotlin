package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.functions.registries.models.TaggedSubscription
import po.misc.functions.registries.taggedRegistryOf
import po.misc.functions.subscribers.NotifySubscription

class TestNotifySubscription {


    internal enum class Events{
        Event1
    }

    @Test
    fun `Delegated subscription work`(){

        val inputValue = "Input"
        var received: String? = null

        val registry = taggedRegistryOf<Events, String>()

        val a = TaggedSubscription<String>(1L, this::class){
            received =it
        }
        val subscription: NotifySubscription<TaggedSubscription<String>, String> = NotifySubscription(a)

        registry.subscribe(Events.Event1, a)



    }

}