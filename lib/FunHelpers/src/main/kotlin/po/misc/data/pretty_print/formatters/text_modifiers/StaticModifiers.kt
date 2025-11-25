package po.misc.data.pretty_print.formatters.text_modifiers

import kotlin.reflect.KClass


class StaticModifiers(){
    internal val modifiersBacking: MutableList<TextModifier> = mutableListOf()
    val modifiers : List<TextModifier> get() = modifiersBacking

    fun addModifiers(textModifiers: List<TextModifier>){
        modifiersBacking.clear()
        val sorted = textModifiers.sortedBy { it.priority }
        modifiersBacking.addAll( sorted )
    }

    fun addModifier(textModifier: TextModifier){
        modifiersBacking.add(textModifier)
    }
    fun isModifierPresent(modifierClass: KClass<out  TextModifier>): Boolean{
        return modifierClass in modifiers.map { it::class }
    }
    fun getModifier(modifierClass: KClass<out  TextModifier>):TextModifier?{
        return modifiers.firstOrNull { it::class == modifierClass }
    }
    fun modify(content: String): String{
        var modified = content
        modifiers.forEach {
            modified = it.modify(modified)
        }
        return modified
    }

    fun clear(){
        modifiersBacking.clear()
    }
}