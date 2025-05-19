package po.test.exposify.setup

import po.test.exposify.setup.dtos.ContentBlock
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.Section


private fun contentBlockModel(section : Section): ContentBlock{
  return  ContentBlock(
        id =  0,
        name =  "undefined",
        content =  "undefined",
        tag =  "no_tag",
        jsonLd =  "undefined",
        classList =  emptyList(),
        metaTags =  emptyList(),
        langId =  section.langId,
        sectionId =  section.id)
}

private fun sectionModel(
    parent : Page,
    name: String,
    updatedBy: Long,
    sectionClasses : List<ClassData> = emptyList(),
    metaTags : List<MetaData> = emptyList()
): Section
{

    return Section(
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

private fun pageModel(name: String, updatedBy: Long): Page{
    val page = Page(0, "TestPage/$name", 1, updatedBy)
    return page
}


fun pageModels(pageCount: Int, updatedBy : Long): List<Page>{
    val result =  mutableListOf<Page>()
    for(index  in 1 .. pageCount){
        result.add(pageModel(index.toString(), updatedBy))
    }
    return  result
}


fun pageModelsWithSections(
    pageCount: Int,
    sectionsCount: Int,
    updatedBy : Long,
    classes: List<ClassData>? = null,
    metaTags: List<MetaData>? = null
): List<Page>{

    val pages =  pageModels(pageCount, updatedBy)
    pages.forEach {
        for(index  in 1 .. sectionsCount){
            val section =  sectionModel(it,index.toString(), updatedBy, classes?:emptyList() , metaTags?:emptyList())
            it.sections.add(section)
        }
    }
    return  pages
}

fun pagesSectionsContentBlocks(pageCount: Int, sectionsCount: Int, contentBlocksCount: Int, updatedBy : Long): List<Page>{

    val pages = pageModelsWithSections(pageCount, sectionsCount, updatedBy)

    pages.forEach {page->
        page.sections.forEach { section->
            for(index  in 1 .. contentBlocksCount) {
                section.contentBlocks.add(contentBlockModel(section))
            }
        }
    }
    return  pages
}