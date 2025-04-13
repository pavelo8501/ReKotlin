package po.test.exposify.constructors

import po.exposify.common.classes.ConstructorBuilder

class TestBuilder : ConstructorBuilder()


//class TestConstructorBuilder {
//
//    @Test
//    fun `initialize sets effective constructor`() {
//        val dtoBlueprint = DTOBlueprint<TestPartnerDataModel, TestPartnerEntity>(TestPartnerDTO::class)
//        dtoBlueprint.initialize(TestBuilder())
//        val constructor   = dtoBlueprint.getConstructor()
//        assertNotNull(constructor, "Constructor should be initialized")
//        assertEquals(
//            TestPartnerDTO::class.primaryConstructor as  KFunction<EntityDTO<TestPartnerDataModel, TestPartnerEntity>>,
//            constructor)
//    }
//
//    @Test
//    fun `getArgsForConstructor generates correct arguments`() {
//        val dtoBlueprint = DTOBlueprint<TestPartnerDataModel, TestPartnerEntity>(TestPartnerDTO::class)
//        dtoBlueprint.initialize(TestBuilder())
//        val args = dtoBlueprint.getArgsForConstructor()
//        val constructor = dtoBlueprint.getConstructor()
//        assertNotNull(args, "Args should not be null")
//        assertEquals(constructor.parameters.size, args.size, "Args size should match constructor parameters size")
//    }
//
//    @Test
//    fun `getConstructor throws exception when not initialized`() {
//        val dtoBlueprint = DTOBlueprint(TestPartnerDTO::class)
//        val exception = assertThrows<OperationsException> {
//            dtoBlueprint.getConstructor()
//        }
//        assertEquals("Effective constructor not set", exception.message)
//    }
//
//    @Test
//    fun `addAsArg adds constructor arguments`() {
//        val dtoBlueprint = DTOBlueprint(TestPartnerDTO::class)
//        dtoBlueprint.initialize(TestBuilder())
//        val constructor = dtoBlueprint.getConstructor()
//        constructor.parameters.forEach { dtoBlueprint.addAsArg(it) }
//        val args = dtoBlueprint.getArgsForConstructor()
//        assertEquals(constructor.parameters.size, args.size, "All constructor parameters should be added as args")
//    }
//
//}