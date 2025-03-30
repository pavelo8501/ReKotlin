package po.exposify.test.setup

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID


class TestUserEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<TestUserEntity>(TestUsers)
    var name by TestUsers.name
    var login by TestUsers.login
    var password by TestUsers.password
    var email by TestUsers.email
    var created by TestUsers.created
    var updated by TestUsers.updated
}


class TestPageEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<TestPageEntity>(TestPages)
    var name by TestPages.name
    var langId by TestPages.langId
    var updated by TestPages.updated
    val sections by TestSectionEntity.Companion referrersOn TestSections.page
}


class TestSectionEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<TestSectionEntity>(TestSections)
    var name by TestSections.name
    var description by TestSections.description
    var jsonLd by TestSections.jsonLd
//    var classList by TestSections.classList
//    var metaTags by TestSections.metaTags
    var updated by TestSections.updated
    var updatedBy by TestUserEntity referencedOn TestSections.updatedBy
    var pageId by TestSections.pageId
    val pageIdValue get() = pageId.value.toLong()
    var langId by TestSections.langId
    var page by TestPageEntity referencedOn TestSections.page
}