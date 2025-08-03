package po.test.exposify.setup.mocks

import po.auth.extensions.generatePassword
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.User

val mockedUser : User
    = User(login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "user_mock",
            email = "nomail@void.null")


val mockedPage: Page = mockPage("Page Mock", 0)

fun mockPage(name: String, updatedBy : Long): Page{
   return Page(name = name, langId = 1, updatedBy = updatedBy)
}

fun mockPages(quantity: Int, builder: (index: Int)-> Page): List<Page>{
    val resultingList: MutableList<Page> = mutableListOf()
    for(i in 1.. quantity){
       val page = builder.invoke(i)
        resultingList.add(page)
    }
    return resultingList
}

val mockedSection: Section = mockSection(0, "Section Mock", "Section Mock", 1)

fun mockSection( pageId: Long, name: String, description: String, updatedBy : Long):Section{
   return Section(
        name = name,
        description = description,
        jsonLd = "",
        langId = 1,
        updatedBy = updatedBy,
        pageId = pageId
    )
}



fun mockSections(pageId: Long, updatedBy: Long, pageCount: Int, builder:Section.(String)-> Unit): List<Section> {
    val result: MutableList<Section> = mutableListOf()
    for (i in 1..pageCount) {
        val section = Section(description = "", langId = 1, updatedBy = updatedBy, pageId = pageId)
        builder.invoke(section, "$i")
        result.add(section)
    }
    return result
}

