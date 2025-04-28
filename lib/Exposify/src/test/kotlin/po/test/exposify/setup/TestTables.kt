package po.test.exposify.setup

import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.exposify.extensions.JsonColumnType
import po.test.exposify.setup.dtos.TestPageDTO
import po.test.exposify.setup.dtos.TestSectionDTO
import po.test.exposify.setup.dtos.TestUserDTO


fun tables(): List<LongIdTable>{
    return  listOf(TestPages, TestSections)
}

object TestUsers : LongIdTable("users", "id") {
    val name = varchar("name",128)
    val login = varchar("login",128)
    val hashedPassword = varchar("hashedPassword",128)
    val email = varchar("email",128)
    val updated = datetime("updated").clientDefault{ TestUserDTO.nowTime()}
    val created = datetime("created").clientDefault{ TestUserDTO.nowTime()}
}


object  TestPages : LongIdTable("pages", "id") {
    val name = varchar("name",128)
    val langId = integer("lang_id").default(1)
    val updatedBy = reference("updated_by", TestUsers)
    val updated = datetime("updated").clientDefault { TestPageDTO.nowTime() }
}

object TestSections : LongIdTable("sections", "id") {
    val name = varchar("name", 128)
    val description = varchar("description", 128).default("")
    val jsonLd = text("json_ld").default("[]")
    val sectionClasses = registerColumn("section_classes", JsonColumnType(ListSerializer(TestClassItem.serializer())))
        .default(emptyList())
    val updatedBy = reference("updated_by", TestUsers)
    val updated = datetime("updated").clientDefault { TestSectionDTO.nowTime() }
    val langId = integer("lang_id")
    val page = reference("page", TestPages, onDelete = ReferenceOption.CASCADE)
}