package po.misc.data.templates


data class TemplateRule<T: Any>(
    val output: String,
    val condition: T.() -> Boolean,
){
    companion object{
        var fallbackText: String = "No template part matching any condition"
    }
}

fun <T: Any>  T.matchTemplate(vararg rules: TemplateRule<T>): String {
  return  rules.firstOrNull { it: TemplateRule<T> ->
      it.condition.invoke(this)
  }?.output?: TemplateRule.fallbackText
}

fun <T: Any> templateRule(template:  String, condition: T.() -> Boolean): TemplateRule<T> =
    TemplateRule(template, condition)