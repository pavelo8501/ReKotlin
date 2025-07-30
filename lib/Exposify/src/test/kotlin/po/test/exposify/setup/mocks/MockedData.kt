package po.test.exposify.setup.mocks

import po.auth.extensions.generatePassword
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.User

val mockedUser : User get() {
    return User(
        id = 0,
        login = "some_login",
        hashedPassword = generatePassword("password"),
        name = "name",
        email = "nomail@void.null"
    )
}


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

