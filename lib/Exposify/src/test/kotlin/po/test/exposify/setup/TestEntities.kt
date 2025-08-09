package po.test.exposify.setup


import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import po.exposify.dao.classes.ExposifyEntityClass


class TestsItemEntity (id: EntityID<Long>) : LongEntity(id){
    companion object : ExposifyEntityClass<TestsItemEntity>(TestsItems)
    var name by Users.name
}

class UserEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : ExposifyEntityClass<UserEntity>(Users)
    var name by Users.name
    var login by Users.login
    var hashedPassword by Users.hashedPassword
    var email by Users.email
    var created by Users.created
    var updated by Users.updated
}


class PageEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : ExposifyEntityClass<PageEntity>(Pages)
    var name by Pages.name
    var langId by Pages.langId
    var updated by Pages.updated
    var updatedBy by Pages.updatedBy
    val sections by SectionEntity referrersOn Sections.page
}

class SectionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : ExposifyEntityClass<SectionEntity>(Sections)

    var name by Sections.name
    var description by Sections.description
    var jsonLd by Sections.jsonLd
    var classList by Sections.classList
    var metaTags by Sections.metaTags
    var updated by Sections.updated
    var langId by Sections.langId
    var updatedBy by Sections.updatedBy
   // var updatedBy by UserEntity referencedOn Sections.updatedBy
    var page by PageEntity referencedOn Sections.page
    val contentBlocks by ContentBlockEntity referrersOn ContentBlocks.section
}

class ContentBlockEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : ExposifyEntityClass<ContentBlockEntity>(ContentBlocks)

    var name by ContentBlocks.name
    var content by ContentBlocks.content
    var tag by ContentBlocks.tag
    var jsonLd by ContentBlocks.jsonLd
    var classList by ContentBlocks.classList
    var metaTags by ContentBlocks.metaTags
    var updated by ContentBlocks.updated
    var langId by ContentBlocks.langId
    var section by SectionEntity referencedOn ContentBlocks.section
}