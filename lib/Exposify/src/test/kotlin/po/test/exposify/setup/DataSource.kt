package po.test.exposify.setup

import kotlinx.serialization.json.Json
import po.test.exposify.setup.dtos.TestSection


fun sectionsPreSaved(pageId: Long): MutableList<TestSection>{
    val sections = """[
  {
    "id": 0,
    "name": "about_section",
    "description": "",
    "json_ld": "",
    "class_list":[{"key":0,"value":"text-block"}],
    "meta_tags": [{"type" :1, "key":"", "value":"text-block"}],
    "lang_id": 1,
    "page_id": $pageId,
    "updated_by": 1,
    "section_items": [
      {
        "id": 0,
        "section_id": 0,
        "name": "about_text",
        "content": "Some Text About Medprof",
        "tag": "p",
        "json_ld": "",
        "lang_id": 1,
        "class_list":[{"key":0,"value":"text-block"}],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      },
      {
        "id": 0,
        "section_id": 0,
        "name": "about_header",
        "content": "About Medprof",
        "tag": "h2",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [{"key":0,"value":"header-block"}],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      }
    ]
  }] """

    val parsed = Json.decodeFromString<MutableList<TestSection>>(sections)
    return parsed
}