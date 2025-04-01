package po.exposify.test.setup

import po.exposify.test.DatabaseTest

fun pageModel(name: String): TestPage{
    return  TestPage("TestPage/$name", 1)
}

fun sectionModel(parent : TestPage,  name: String): TestSection{
    return  TestSection("TestSection/$name", "TestSection/$name Description", "",  1, parent.id)
}