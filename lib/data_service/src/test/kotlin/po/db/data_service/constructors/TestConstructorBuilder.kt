package po.db.data_service.constructors

import org.junit.jupiter.api.assertThrows
import po.db.data_service.data.TestDTO
import po.db.data_service.data.TestDataModel
import po.db.data_service.data.TestEntity
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestBuilder : ConstructorBuilder() {}

fun <T : Any> getCovariantBlueprint(container: CovariantClassBlueprintBase<T>): CovariantClassBlueprintBase<T> {
        container.clazz.primaryConstructor?.let { constructor ->
            container.setConstructor(constructor)
        }
        return container
    }

    fun getDefaultForType(kType: KType): Any? {
        return when (kType.classifier) {
            String::class -> "default"
            Int::class -> 0
            Boolean::class -> false
            else -> null
        }
    }

class TestConstructorBuilder {

    @Test
    fun `initialize sets effective constructor`() {
        val dtoBlueprint = DTOBlueprint<TestDataModel, TestEntity>(TestDTO::class)
        dtoBlueprint.initialize(TestBuilder())
        val constructor   = dtoBlueprint.getConstructor()
        assertNotNull(constructor, "Constructor should be initialized")
        assertEquals(TestDTO::class.primaryConstructor as  KFunction<EntityDTO<TestDataModel, TestEntity>> , constructor)
    }

    @Test
    fun `getArgsForConstructor generates correct arguments`() {
        val dtoBlueprint = DTOBlueprint<TestDataModel, TestEntity>(TestDTO::class)
        dtoBlueprint.initialize(TestBuilder())
        val args = dtoBlueprint.getArgsForConstructor()
        val constructor = dtoBlueprint.getConstructor()
        assertNotNull(args, "Args should not be null")
        assertEquals(constructor.parameters.size, args.size, "Args size should match constructor parameters size")
    }

    @Test
    fun `getConstructor throws exception when not initialized`() {
        val dtoBlueprint = DTOBlueprint(TestDTO::class)

        val exception = assertThrows<OperationsException> {
            dtoBlueprint.getConstructor()
        }
        assertEquals("Effective constructor not set", exception.message)
    }

    @Test
    fun `setParams correctly sets constructor parameters`() {
        val dtoBlueprint = DTOBlueprint(TestDTO::class)
        dtoBlueprint.initialize(TestBuilder())
        val constructor = dtoBlueprint.getConstructor()

        val params = constructor.parameters.associateWith { "testValue" }
        dtoBlueprint.setParams(params)

        val args = dtoBlueprint.getArgsForConstructor()
        assertEquals("testValue", args[constructor.parameters[0]], "Params should match set values")
    }

    @Test
    fun `addAsArg adds constructor arguments`() {
        val dtoBlueprint = DTOBlueprint(TestDTO::class)
        dtoBlueprint.initialize(TestBuilder())
        val constructor = dtoBlueprint.getConstructor()
        constructor.parameters.forEach { dtoBlueprint.addAsArg(it) }
        val args = dtoBlueprint.getArgsForConstructor()
        assertEquals(constructor.parameters.size, args.size, "All constructor parameters should be added as args")
    }

}