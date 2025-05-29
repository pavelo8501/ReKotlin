package po.test.exposify.setup

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.exposify.dao.classes.JsonColumnList
import po.exposify.dao.classes.jsonColumnList
import po.test.exposify.setup.dtos.ContentBlockDTO
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.UserDTO


fun tables(): List<LongIdTable>{
    return  listOf(Pages, Sections)
}


object TestsItems : LongIdTable("tests_items", "id") {
    val name = varchar("name",128)
}

object Users : LongIdTable("api_users", "id") {
    val name = varchar("name",128)
    val login = varchar("login",128)
    val hashedPassword = varchar("password",128)
    val email = varchar("email",128)
    val updated = datetime("updated").clientDefault{ UserDTO.nowTime()}
    val created = datetime("created").clientDefault{ UserDTO.nowTime()}
}


object  Pages : LongIdTable("pages", "id") {
    val name = varchar("name",128)
    val langId = integer("lang_id").default(1)
    val updatedBy = long("updated_by").default(1)
   // val updatedBy = reference("updated_by", Users)
    val updated = datetime("updated").clientDefault { PageDTO.nowTime() }
}

object Sections : LongIdTable("sections", "id") {
    val name = varchar("name", 128)
    val description = varchar("description", 128).default("")
    val jsonLd = text("json_ld").default("[]")
    val classList = registerColumn("class_list",  jsonColumnList(ClassData.serializer()))
        .default(emptyList())
    val metaTags = registerColumn("meta_tags", jsonColumnList(MetaData.serializer()))
        .default(emptyList())
   /// val updatedBy = reference("updated_by", Users)
    val updatedBy = long("updated_by").default(1)
    val updated = datetime("updated").clientDefault { SectionDTO.nowTime() }
    val langId = integer("lang_id")
    val page = reference("page", Pages, onDelete = ReferenceOption.CASCADE)
}

object ContentBlocks : LongIdTable("content_blocks", "id") {
    val name = varchar("name", 128)
    val content = text("content")
    val tag = varchar("tag", 64)
    val jsonLd = text("json_ld").default("")
    val classList = registerColumn("class_list", jsonColumnList(ClassData.serializer()))
        .default(emptyList())
    val metaTags =  registerColumn("meta_tags", jsonColumnList(MetaData.serializer()))
        .default(emptyList())
    val langId = integer("lang_id")
    val updated = datetime("updated").clientDefault { ContentBlockDTO.nowTime() }
    val section = reference("section", Sections, onDelete = ReferenceOption.CASCADE)
}