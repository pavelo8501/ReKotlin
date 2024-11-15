package po.plugins.binder.templates

import kotlinx.datetime.LocalDateTime


class DtoClass{}

class EntityClass{}

interface BindPropertyInterface{
    var name: String
    fun update(): Boolean
}

class StringPropertyClass(
    override var name: String,
    var dtoProperty: String?,
    var entityProperty: String?,
):BindPropertyInterface{
    override fun update(): Boolean {
        if(dtoProperty == entityProperty)  return false
        entityProperty = dtoProperty
        return true
    }
}

class LongPropertyClass(
    override var name: String,
    var dtoProperty: Long?,
    var entityProperty: Long?,
):BindPropertyInterface{
    override fun update(): Boolean {
        if(dtoProperty == entityProperty)  return false
        entityProperty = dtoProperty
        return true
    }
}

class IntPropertyClass(
    override var name: String,
    var dtoProperty: Int?,
    var entityProperty: Int?,
):BindPropertyInterface{
    override fun update(): Boolean {
        if(dtoProperty == entityProperty)  return false
        entityProperty = dtoProperty
        return true
    }
}

class DatePropertyClass(
    override var name: String,
    var dtoProperty: LocalDateTime?,
    var entityProperty: LocalDateTime?,
):BindPropertyInterface{
    override fun update(): Boolean {
        if(dtoProperty == entityProperty)  return false
        entityProperty = dtoProperty
        return true
    }
}

