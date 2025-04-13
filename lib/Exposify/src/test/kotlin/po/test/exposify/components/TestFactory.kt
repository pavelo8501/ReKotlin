package po.test.exposify.components

import org.junit.jupiter.api.Test

class TestFactory {

    @Test
    fun `initializeBlueprints should initialize data and entity blueprints`() {

//        val mockDataClass = TestPage::class
//        val mockEntityClass = TestPageEntity::class
//        val factory = DTOFactory<TestPage, TestPageEntity>(TestPageDTO, TestPageDTO::class)
//
//        factory.initializeBlueprints(mockDataClass, mockEntityClass)
//
//        assertEquals(mockDataClass, factory.dataModelClass)
//        assertEquals(mockEntityClass, factory.daoEntityClass)
    }

    @Test
    fun `setDataModelConstructor should set custom constructor`() {

//        val factory = DTOFactory<TestSection, TestSectionEntity>(TestSectionDTO, TestSectionDTO::class)
//        val mockConstructor: () -> TestSection = { TestSection("TestSection", "po/exposify/test","", emptyList(), emptyList(), 1, 1) }
//
//        factory.setDataModelConstructor(mockConstructor)
//
//        val dataModel = factory.createDataModel()
//        assertEquals("po/exposify/test", dataModel.name)
    }

    @Test
    fun `extractDataModel should extract child data models`() {

//        val parentData = TestPartnerDataModel().apply {
//            departnemts.addAll(listOf(
//                TestDepartmentDataModel(true, "child1", 12),
//                TestDepartmentDataModel(false, "child2", 24)
//            ))
//        }
//        val factory = Factory(TestDepartmentDTO, TestDepartmentDTO::class)
//        val children = factory.extractDataModel(TestPartnerDataModel::departnemts, parentData)
//
//        assertEquals(2, children.size)
//        assertEquals("child1", children[0].name)
//        assertEquals("child2", children[1].name)
    }

    @Test
    fun `createDataModel should create data model using default constructor`() {
//        val factory = Factory(TestDepartmentDTO, TestDepartmentDTO::class)
//        factory.initializeBlueprints(TestDepartmentDataModel::class, TestDepartmentEntity::class)
//        val dataModel = factory.createDataModel()
//        assertNotNull(dataModel)
    }

}