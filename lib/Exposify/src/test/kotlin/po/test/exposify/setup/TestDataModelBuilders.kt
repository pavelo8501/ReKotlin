package po.test.exposify.setup

import po.test.exposify.setup.dtos.TestPage
import po.test.exposify.setup.dtos.TestSection


private fun sectionModel(
    parent : TestPage,
    name: String,
    updatedBy: Long,
    sectionClasses : List<ClassItem> = listOf(ClassItem(1,"class_1"), ClassItem(2,"class_2")),
    metaTags : List<MetaTag> = listOf(MetaTag(1,"key", "value"))
): TestSection
{

    return TestSection(
        id = 0,
        name = "TestSection/$name",
        description =  "TestSection/$name Description",
        jsonLd =  "",
        classList = sectionClasses,
        metaTags = metaTags,
        langId =  1,
        updatedBy =  updatedBy,
        pageId =  parent.id,
      //  page = TestPage(id =  0, name =  "TestPage", langId =  1, updatedById =  updatedBy)
        )
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