package po.test.exposify.setup

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.exposify.entity.classes.ExposifyEntity


class TestUserEntity  (id: EntityID<Long>) : ExposifyEntity(id){
    companion object : LongEntityClass<TestUserEntity>(TestUsers)
    var name by TestUsers.name
    var login by TestUsers.login
    var hashedPassword by TestUsers.hashedPassword
    var email by TestUsers.email
    var created by TestUsers.created
    var updated by TestUsers.updated
}

class TestPageEntity  (id: EntityID<Long>) : ExposifyEntity(id){
    companion object : LongEntityClass<TestPageEntity>(TestPages)
    var name by TestPages.name
    var langId by TestPages.langId
    var updated by TestPages.updated
    var updatedBy by TestUserEntity referencedOn TestPages.updatedBy
    val sections by TestSectionEntity referrersOn TestSections.page
}


class TestSectionEntity  (id: EntityID<Long>) : ExposifyEntity(id){
    companion object : LongEntityClass<TestSectionEntity>(TestSections)
    var name by TestSections.name
    var description by TestSections.description
    var jsonLd by TestSections.jsonLd
    var classList by TestSections.classList
    var metaTags by TestSections.metaTags
    var updated by TestSections.updated
    var langId by TestSections.langId
    var updatedBy by TestUserEntity referencedOn TestSections.updatedBy
    var page by TestPageEntity referencedOn TestSections.page
    val sectionItems by TestSectionItemEntity referrersOn TestSectionItems.section
}

class TestSectionItemEntity  (id: EntityID<Long>) : ExposifyEntity(id){
    companion object : LongEntityClass<TestSectionItemEntity>(TestSectionItems)
    var name by TestSectionItems.name
    var content by TestSectionItems.content
    var tag by TestSectionItems.tag
    var jsonLd by TestSectionItems.jsonLd
    var classList by TestSectionItems.classList
    var metaTags by TestSectionItems.metaTags
    var updated by TestSectionItems.updated
    var langId by TestSectionItems.langId
    var section by TestSectionEntity referencedOn TestSectionItems.section
}