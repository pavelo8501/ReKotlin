package po.test.exposify.setup

import kotlinx.serialization.json.Json
import po.test.exposify.setup.dtos.Section

fun sectionsPreSaved(pageId: Long): MutableList<Section>{

    val sections = """[
  {
    "name": "about_section",
    "description": "",
    "json_ld": "",
    "class_list":[{"key":0,"value":"text-block"}],
    "meta_tags": [{"type" :1, "key":"", "value":"text-block"}],
    "lang_id": 1,
    "page_id": $pageId,
    "updated_by": 1,
    "content_blocks": [
      {
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
  },
  {
    "name": "moto_section",
    "description": "",
    "json_ld": "",
    "class_list": [{"key":0,"value":"text-block"}],
    "meta_tags": [{"type" : 1, "key": "","value":"text-block"}],
    "lang_id": 1,
    "page_id": $pageId,
    "updated_by": 1,
    "content_blocks": [
      {
       "id": 0,
       "section_id": 0,
        "name": "moto_header",
        "content": "Enhance Workplace Safety with Medprof",
        "tag": "h2",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [{"key":0,"value":"text-block"}],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      },
      {
       "section_id": 0,
        "name": "moto_text",
        "content": "Mūsu visaptverošās veselības pārbaudes nodrošina, ka jūsu uzņēmums atbilst visiem veselības noteikumiem, aizsargājot darbinieku labklājību. Mēs piedāvājam detalizētus pārskatus un praktiskus ieteikumus, lai savlaicīgi novērstu jebkādas veselības problēmas un veicinātu veselīgāku darba vidi.",
        "tag": "p",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [{"key":0,"value":"text-block"}],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      }
    ]
  },
  {
    "name": "action_call_section",
    "description": "",
    "json_ld": "",
    "class_list": [{"key":0,"value":"text-block"}],
    "meta_tags": [{"type" : 1, "key": "","value":"text-block"}],
    "lang_id": 1,
    "page_id": $pageId,
    "updated_by": 1,
    "content_blocks": [
      {
       "section_id": 0,
        "name": "action_call_header",
        "content": "<h2 class=\"header-block bold-text\">Ensure a <span class=\"text-blue-800\">Healthier </span> Workplace Today</h2>",
        "tag": "h2",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      },
      {
       "section_id": 0,
        "name": "action_call_text",
        "content": "Medprof specializējas visaptverošās veselības pārbaudēs, kas pielāgotas korporatīvajiem klientiem. Mūsu pieredzējusī komanda nodrošina atbilstību veselības noteikumiem, aizsargājot jūsu darbinieku labklājību un uzturot drošu darba vidi.",
        "tag": "p",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      }
    ]
  },
  {
    "name": "mission_section",
    "description": "",
    "json_ld": "",
    "class_list": [{"key":0,"value":"text-block"}],
    "meta_tags": [{"type" : 1, "key": "","value":"text-block"}],
    "lang_id": 1,
    "page_id": $pageId,
    "updated_by": 1,
    "content_blocks": [
      {
       "section_id": 0,
        "name": "mission_header",
        "content": "<h2>Our Commitment to Health</h2>",
        "tag": "h2",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      },
      {
       "section_id": 0,
        "name": "mission_text",
        "content": "<p>\n            Medprof ir apņēmies uzlabot darba drošību, veicot visaptverošas veselības pārbaudes, kas pielāgotas korporatīvajiem klientiem. Dibināts 2015. gadā, mūsu misija ir nodrošināt uzticamus novērtējumus, kuros galvenā prioritāte ir darbinieku labklājība. Mēs ievērojam godīguma, izcilības un klientu orientētas pieejas principus, nodrošinot, ka mūsu pakalpojumi atbilst augstākajiem standartiem. Mūsu pieredzējusī komanda, kuru vada Dr. Sāra Tompsone, veic rūpīgas pārbaudes un sniedz praktiskus ieteikumus, lai veicinātu drošu darba vidi. Sadarbojieties ar mums, lai izveidotu veselīgāku darba vietu, kas atbilst visiem veselības noteikumiem.\n        </p>",
        "tag": "p",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      }
    ]
  },
  {
    "name": "services_section",
    "description": "",
    "json_ld": "",
    "class_list": [{"key":0,"value":"text-block"}],
    "meta_tags": [{"type" : 1, "key": "","value":"text-block"}],
    "lang_id": 1,
    "page_id": $pageId,
    "updated_by": 1,
    "content_blocks": [
      {
        "section_id": 0,
        "name": "services_header",
        "content": "<h2>Explore Our Comprehensive Health Inspection Services</h2>",
        "tag": "p",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      },
      {
       "section_id": 0,
        "name": "services_text",
        "content": "<p>Medprof piedāvā virkni obligāto veselības pārbaužu, kas pielāgotas korporatīvo klientu unikālajām vajadzībām. Mūsu pieredzējusī komanda nodrošina atbilstību veselības noteikumiem, aizsargājot darbinieku labklājību un uzturot drošu darba vidi. Uzziniet, kā mūsu pakalpojumi var palīdzēt jūsu uzņēmumam un veicināt veselīgāku darba vidi.</p>",
        "tag": "p",
        "json_ld": "",
        "lang_id": 1,
        "class_list": [],
        "meta_tags": [{"type" : 1, "key": "","value":"text-block"}]
      }
    ]
  }
]
"""

    val parsed = Json.decodeFromString<MutableList<Section>>(sections)
    return parsed

}