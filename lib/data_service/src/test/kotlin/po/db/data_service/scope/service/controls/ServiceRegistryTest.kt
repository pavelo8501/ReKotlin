package po.db.data_service.scope.service.controls

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.db.data_service.data.*
import po.db.data_service.scope.service.controls.service_registry.ChildDTOData
import po.db.data_service.scope.service.controls.service_registry.DTOData
import po.db.data_service.scope.service.controls.service_registry.ServiceUniqueKey
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceRegistryTest {

    @Test
    fun `should build ServiceRegistryItem with concrete test DTO and entity`() {

        val testEntity = TestEntity(EntityID(100L, TestPartners))
        val testDTO = TestDTO(TestDataModel())

        val registry = ServiceRegistryBuilder<TestDataModel, TestEntity>()
            .addServiceRegistryItem {
                key = ServiceUniqueKey("TestService")

                metadata {
                    key = ServiceUniqueKey("TestMetadata")

                    service {
                        rootDTOModelData = DTOData(
                            dtoModel = testDTO,
                            daoEntityModel = TestEntity,
                            dataModelClass = TestDataModel::class
                        )
                    }
                }
            }
            .build()

        assertEquals(1, registry.size)
        val item = registry.first()
        assertEquals("TestService", item.key.key)
        assertEquals("TestMetadata", item.metadata.key.key)
        assertEquals(testDTO, item.metadata.service.rootDTOModelData.dtoModel)
    }

    @Test
    fun `should throw exception when rootDTOModelData is missing`() {
        val exception = assertThrows<IllegalArgumentException> {
            ServiceRegistryBuilder<TestDataModel, TestEntity>()
                .addServiceRegistryItem {
                    key = ServiceUniqueKey("TestService")

                    metadata {
                        key = ServiceUniqueKey("TestMetadata")

                        service {
                            // rootDTOModelData is not set
                        }
                    }
                }
                .build()
        }
        assertEquals("ServiceData must have a rootDTOModel", exception.message)
    }

    @Test
    fun `should throw exception when metadata key is missing`() {
        val exception = assertThrows<IllegalArgumentException> {
            ServiceRegistryBuilder<TestDataModel, TestEntity>()
                .addServiceRegistryItem {
                    key = ServiceUniqueKey("TestService")

                    metadata {
                        // key is not set
                        service {
                            rootDTOModelData = DTOData(
                                dtoModel = TestDTO(TestDataModel()),
                                daoEntityModel = TestEntity::class as LongEntityClass<TestEntity>,
                                dataModelClass = TestDataModel::class
                            )
                        }
                    }
                }
                .build()
        }

        assertEquals("ServiceMetadata must have a key", exception.message)
    }

    @Test
    fun `should add child DTOs to service data`() {
        val registry = ServiceRegistryBuilder<TestDataModel, TestEntity>()
            .addServiceRegistryItem {
                key = ServiceUniqueKey("TestService")

                metadata {
                    key = ServiceUniqueKey("TestMetadata")

                    service {
                        rootDTOModelData = DTOData(
                            dtoModel = TestDTO(TestDataModel()),
                            daoEntityModel = TestEntity::class as LongEntityClass<TestEntity>,
                            dataModelClass = TestDataModel::class
                        )

                        childDTOModel<TestChildDataModel , TestChildEntity> {
                            setDTOModel(TestChildDTO(TestChildDataModel()))
                            setEntityModel(TestChildEntity::class as LongEntityClass<TestChildEntity>)
                            setDataModel(TestChildDataModel::class)
                        }

                        childDTOModel<TestChildDataModel, TestChildEntity> {
                            setDTOModel(TestChildDTO(TestChildDataModel()))
                            setEntityModel(TestChildEntity::class as LongEntityClass<TestChildEntity>)
                            setDataModel(TestChildDataModel::class)
                        }
                    }
                }
            }
            .build()

        val serviceData = registry.first().metadata.service
        assertEquals(2, serviceData.childDTOModelsData.size)
    }

    @Test
    fun `should build multiple ServiceRegistryItems`() {
        val registry = ServiceRegistryBuilder<TestDataModel, TestEntity>()
            .addServiceRegistryItem {
                key = ServiceUniqueKey("FirstService")

                metadata {
                    key = ServiceUniqueKey("FirstMetadata")

                    service {
                        rootDTOModelData = DTOData(
                            dtoModel = TestDTO(TestDataModel()),
                            daoEntityModel = TestEntity::class as LongEntityClass<TestEntity>,
                            dataModelClass = TestDataModel::class
                        )
                    }
                }
            }
            .addServiceRegistryItem {
                key = ServiceUniqueKey("SecondService")

                metadata {
                    key = ServiceUniqueKey("SecondMetadata")

                    service {
                        rootDTOModelData = DTOData(
                            dtoModel = TestDTO(TestDataModel()),
                            daoEntityModel = TestEntity::class as LongEntityClass<TestEntity>,
                            dataModelClass = TestDataModel::class
                        )
                    }
                }
            }
            .build()

        assertEquals(2, registry.size)
        assertEquals("FirstService", registry[0].key.key)
        assertEquals("SecondService", registry[1].key.key)
    }

    @Test
    fun `should create empty registry`() {
        val registry = ServiceRegistryBuilder<TestDataModel, TestEntity>().build()
        assertTrue(registry.isEmpty())
    }

    @Test
    fun `should throw exception for duplicate keys`() {
        val exception = assertThrows<IllegalArgumentException> {
            ServiceRegistryBuilder<TestDataModel, TestEntity>()
                .addServiceRegistryItem {
                    key = ServiceUniqueKey("DuplicateKey")

                    metadata {
                        key = ServiceUniqueKey("Metadata1")

                        service {
                            rootDTOModelData = DTOData(
                                dtoModel = TestDTO(TestDataModel()),
                                daoEntityModel = TestEntity::class as LongEntityClass<TestEntity>,
                                dataModelClass = TestDataModel::class
                            )
                        }
                    }
                }
                .addServiceRegistryItem {
                    key = ServiceUniqueKey("DuplicateKey") // Same key as the previous item

                    metadata {
                        key = ServiceUniqueKey("Metadata2")

                        service {
                            rootDTOModelData = DTOData(
                                dtoModel = TestDTO(TestDataModel()),
                                daoEntityModel = TestEntity::class as LongEntityClass<TestEntity>,
                                dataModelClass = TestDataModel::class
                            )
                        }
                    }
                }
                .build()
        }
        assertEquals("Duplicate keys found: [DuplicateKey]", exception.message)
    }

}