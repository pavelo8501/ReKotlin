package po.test.exposify.setup.mocks

import po.auth.extensions.generatePassword
import po.misc.containers.Containable
import po.misc.containers.ReceiverContainer
import po.misc.functions.dsl.DSLBuildingBlock
import po.test.exposify.setup.ClassData
import po.test.exposify.setup.MetaData
import po.test.exposify.setup.dtos.ContentBlock
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.User
import javax.sound.midi.Receiver


val mockedUser : User  = User(0, "some_login", generatePassword("password"), "user_mock", "nomail@void.null")

val mockedPage: Page = mockPage("Page Mock", 1)

fun mockPage(name: String, updatedBy : Long): Page{
   return Page(name = name, langId = 1, updatedBy = updatedBy)
}

fun mockPages(updatedBy: Long, quantity: Int, builder:(Page.(Int)-> Unit)? = null): List<Page>{
    val result: MutableList<Page> = mutableListOf()
    for (i in 1..quantity) {
        val page = mockPage("page_$i",  updatedBy = updatedBy)
        builder?.invoke(page, i)
        result.add(page)
    }
    return result
}

val mockedSection: Section = mockSection(0, "Section Mock", "Section Mock", 1)

fun mockSection(pageId: Long, name: String, description: String, updatedBy : Long):Section{
   return Section(
        name = name,
        description = description,
        jsonLd = "",
        langId = 1,
        updatedBy = updatedBy,
        pageId = pageId
    )
}


fun mockSections(page: Page, count: Int, builder:Section.(String)-> Unit): List<Section> {

    val result: MutableList<Section> = mutableListOf()
    for (i in 1..count) {
        val section = Section(description = "", langId = 1, updatedBy = page.updatedBy, pageId = page.id)
        builder.invoke(section, "$i")
        result.add(section)
    }
    return result
}


fun mockSection(page: Page, name: String, description: String):Section {
    val section = Section(name = name, description = description, langId = 1, updatedBy = page.updatedBy, pageId = page.id)
    return section
}

fun Page.withSections(
    quantity: Int,
    classList: List<ClassData> = emptyList(),
    metaTags: List<MetaData> = emptyList(),

    block: (Section.(Int)-> Unit)? = null){
    for (i in 1..quantity) {
        val section = Section(
            0,
            "section_${i}_${name}",
            "description_${i}_${name}",
            jsonLd = "",
            classList = classList,
            metaTags =  metaTags,
            pageId = this.id,
            langId = 1,
            updatedBy = updatedBy
        )
        block?.invoke(section, i)
        sections.add(section)
    }
}

fun Section.withContentBlocks(
    quantity: Int,
    classList: List<ClassData> = emptyList(),
    metaTags: List<MetaData> = emptyList(),
    block: (ContentBlock.(Int)-> Unit)? = null
){
    for (i in 1..quantity) {
        val contentBlock =  mockContentBlock(
            section = this,
            name = "content_block_$i",
            content = "content_block_content_${i}_${this.name}",
            classList = classList,
            metaTags = metaTags,
        )
        block?.invoke(contentBlock, i)
        contentBlocks.add(contentBlock)
    }
}

fun mockContentBlock(
    section: Section,
    name: String,
    content: String,
    classList: List<ClassData> = emptyList(),
    metaTags: List<MetaData> = emptyList()
):ContentBlock {
    val contentBlock = ContentBlock(
        name = name,
        content = content,
        langId = section.langId,
        sectionId = section.id,
        tag = "",
        jsonLd = "",
        classList = classList,
        metaTags = metaTags
    )
    return contentBlock
}




