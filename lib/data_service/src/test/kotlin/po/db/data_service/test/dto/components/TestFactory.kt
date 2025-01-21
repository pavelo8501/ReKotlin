package po.db.data_service.test.dto.components

import org.junit.jupiter.api.Test
import po.db.data_service.classes.components.Factory
import po.db.data_service.test.data.TestDepartmentDTO
import po.db.data_service.test.data.TestDepartmentDataModel
import po.db.data_service.test.data.TestDepartmentEntity
import po.db.data_service.test.data.TestPartnerDataModel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestFactory {

    @Test
    fun `initializeBlueprints should initialize data and entity blueprints`() {

        val mockDataClass = TestDepartmentDataModel::class
        val mockEntityClass = TestDepartmentEntity::class
        val factory = Factory<TestDepartmentDataModel, TestDepartmentEntity>(
            TestDepartmentDTO,
            TestDepartmentDTO::class)

        factory.initializeBlueprints(mockDataClass, mockEntityClass)

        assertEquals(mockDataClass, factory.dataModelClass)
        assertEquals(mockEntityClass, factory.daoEntityClass)
    }

    @Test
    fun `setDataModelConstructor should set custom constructor`() {

        val factory = Factory(TestDepartmentDTO, TestDepartmentDTO::class)
        val mockConstructor: () -> TestDepartmentDataModel = { TestDepartmentDataModel(false, "test", 32) }

        factory.setDataModelConstructor(mockConstructor)

        val dataModel = factory.createDataModel()
        assertEquals("test", dataModel.name)
    }

    @Test
    fun `extractDataModel should extract child data models`() {

        val parentData = TestPartnerDataModel().apply {
            departnemts.addAll(listOf(
                TestDepartmentDataModel(true, "child1", 12),
                TestDepartmentDataModel(false, "child2", 24)
            ))
        }
        val factory = Factory(TestDepartmentDTO, TestDepartmentDTO::class)
        val children = factory.extractDataModel(TestPartnerDataModel::departnemts, parentData)

        assertEquals(2, children.size)
        assertEquals("child1", children[0].name)
        assertEquals("child2", children[1].name)
    }

    @Test
    fun `createDataModel should create data model using default constructor`() {
        val factory = Factory(TestDepartmentDTO, TestDepartmentDTO::class)
        factory.initializeBlueprints(TestDepartmentDataModel::class, TestDepartmentEntity::class)
        val dataModel = factory.createDataModel()
        assertNotNull(dataModel)
    }

}