package po.exposify.test.setup

import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.exposify.extensions.JsonColumnType


fun tables(): List<LongIdTable>{
    return  listOf<LongIdTable>(TestPages, TestSections)
}

object TestUsers : LongIdTable("users", "id") {
    val name = varchar("name",128)
    val login = varchar("login",128)
    val password = varchar("password",128)
    val email = varchar("email",128)
    val updated = datetime("updated").clientDefault{ TestUserDTO.Companion.nowTime()}
    val created = datetime("created").clientDefault{ TestUserDTO.Companion.nowTime()}
}


object  TestPages : LongIdTable("pages", "id") {
    val name = varchar("name",128)
    val langId = integer("lang_id").default(1)
    val pageClasses = registerColumn<List<TestClassItem>>("page_classes", JsonColumnType(ListSerializer(TestClassItem.serializer())))
    val updated = datetime("updated").clientDefault { TestPageDTO.Companion.nowTime() }
}

object TestSections : LongIdTable("sections", "id") {
    val name = varchar("name", 128)
    val description = varchar("description", 128).default("")
    val jsonLd = text("json_ld").default("[]")
    val classList = registerColumn<List<TestClassItem>>("class_list", JsonColumnType(ListSerializer(TestClassItem.serializer())))
        .default(emptyList())
    val metaTags =  registerColumn<List<TestMetaTag>>("meta_tags", JsonColumnType(ListSerializer(TestMetaTag.serializer())))
        .default(emptyList())
 //   val updatedBy = reference("updated_by", TestUsers)
    val updated = datetime("updated").clientDefault { TestSectionDTO.Companion.nowTime() }
  //  val pageId = reference("page_id", TestPages)
    val langId = integer("lang_id")
    val page = reference("page", TestPages, onDelete = ReferenceOption.CASCADE)
}