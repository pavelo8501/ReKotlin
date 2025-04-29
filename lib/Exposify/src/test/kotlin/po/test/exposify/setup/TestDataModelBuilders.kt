package po.test.exposify.setup

import po.test.exposify.setup.dtos.TestPage
import po.test.exposify.setup.dtos.TestSection


private fun sectionModel(
    parent : TestPage,
    name: String,
    updatedBy: Long,
    sectionClasses : List<TestClassItem> = listOf(TestClassItem(1,"class_1"), TestClassItem(2,"class_2"))): TestSection
{

    return TestSection(
        id = 0,
        name = "TestSection/$name",
        description =  "TestSection/$name Description",
        jsonLd =  "",
        classList = sectionClasses,
        langId =  1,
        updatedBy =  updatedBy,
        pageId =  parent.id)
}

private fun pageModel(name: String, updatedBy: Long): TestPage{
    val page = TestPage(0, "TestPage/$name", 1, updatedBy)
    return page
}


fun pageModels(pageCount: Int, updatedBy : Long): List<TestPage>{
    val result =  mutableListOf<TestPage>()
    for(index  in 1 .. pageCount){
        result.add(pageModel(index.toString(), updatedBy))
    }
    return  result
}


fun pageModelsWithSections(pageCount: Int, sectionsCount: Int, updatedBy : Long): List<TestPage>{
    val pages =  pageModels(pageCount, updatedBy)
    pages.forEach {
        for(index  in 1 .. sectionsCount){
            it.sections.add(sectionModel(it,index.toString(), updatedBy))
        }
    }
    return  pages
}