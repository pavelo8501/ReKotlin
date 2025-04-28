package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.delegates.parentReference
import po.test.exposify.setup.TestPageEntity
import po.test.exposify.setup.TestSectionEntity


@Serializable
data class TestPage(
    var name: String,
    @SerialName("lang_id")
    var langId: Int,

    ): DataModel{
    override var id: Long = 0
    var updated: LocalDateTime = TestPageDTO.nowTime()
    var sections: MutableList<TestSection> = mutableListOf<TestSection>()
    @SerialName("updated_by")
    var updatedById: Long = 1
}

class TestPageDTO(
    override var dataModel: TestPage
): CommonDTO<TestPageDTO, TestPage, TestPageEntity>(TestPageDTO) {

    var updatedById : Long by parentReference{
        dataModelProperty(TestPage::updatedById)
        referencedEntityModel(TestPageEntity)
    }

    companion object: DTOClass<TestPageDTO>(){
        override suspend fun setup() {
            configuration<TestPageDTO, TestPage, TestPageEntity>(TestPageEntity){
                propertyBindings(
                    SyncedBinding(TestPage::name, TestPageEntity::name),
                    SyncedBinding(TestPage::langId,  TestPageEntity::langId),
                    SyncedBinding(TestPage::updated, TestPageEntity::updated),
                )
                childBindings{
                    many<TestSectionDTO>(
                        childModel = TestSectionDTO,
                        ownDataModels = TestPage::sections,
                        ownEntities = TestPageEntity ::sections,
                        foreignEntity = TestSectionEntity::page
                    )
                }
            }
        }
    }
}