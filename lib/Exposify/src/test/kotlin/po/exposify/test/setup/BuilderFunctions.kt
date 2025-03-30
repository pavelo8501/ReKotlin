package po.exposify.test.setup

import po.exposify.test.DatabaseTest

class BuilderFunctions {
}

fun <T: DatabaseTest>  T.sectionModel(name: String): TestSection{
    return  TestSection(name, "$name description", "",  1, 1)
}