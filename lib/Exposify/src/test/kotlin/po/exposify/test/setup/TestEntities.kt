package po.exposify.test.setup

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.exposify.entity.classes.ExposifyEntityBase


class TestUserEntity  (id: EntityID<Long>) : ExposifyEntityBase(id){
    companion object : LongEntityClass<TestUserEntity>(TestUsers)
    var name by TestUsers.name
    var login by TestUsers.login
    var password by TestUsers.password
    var email by TestUsers.email
    var created by TestUsers.created
    var updated by TestUsers.updated
}


class TestPageEntity  (id: EntityID<Long>) : ExposifyEntityBase(id){
    companion object : LongEntityClass<TestPageEntity>(TestPages)
    var name by TestPages.name
    var langId by TestPages.langId
    var pageClasses by TestPages.pageClasses
    var updated by TestPages.updated
    var updatedBy by TestUserEntity referencedOn TestPages.updatedBy
    val pageId get() = updatedBy.id.value.toLong()
    val sections by TestSectionEntity.Companion referrersOn TestSections.page
}


class TestSectionEntity  (id: EntityID<Long>) : ExposifyEntityBase(id){
    companion object : LongEntityClass<TestSectionEntity>(TestSections)
    var name by TestSections.name
    var description by TestSections.description
    var jsonLd by TestSections.jsonLd
    var sectionClasses by TestSections.sectionClasses
    var sectionMetaTags by TestSections.sectionMetaTags
    var updated by TestSections.updated
    var langId by TestSections.langId
    var updatedBy by TestUserEntity referencedOn TestSections.updatedBy
    val updatedById get() = updatedBy.id.value.toLong()
    var page by TestPageEntity referencedOn TestSections.page
    val pageId get() = page.id.value.toLong()
}