package po.exposify.dto.components

object DTOPreflightValidator {

//    suspend fun <DTO : ModelDTO> DTOClass<DTO>.preflightCheck() {
//        validateConfiguration()
//        config.propertyBinder.validateBindings()
//        withRelationshipBinder { validateChildren() }
//        println("✅ DTO ${this::class.simpleName} passed preflight check.")
//    }
//
//    fun <DTO : ModelDTO> DTOClass<DTO>.validateConfiguration() {
//        require(initialized) { "DTOClass ${this::class.simpleName} is not initialized. Call setup() first." }
//
//        val config = this._config ?: throw InitException("DTO config missing", ExceptionCode.UNDEFINED)
//        requireNotNull(config.entityModel) { "Entity model not set for ${this::class.simpleName}" }
//        requireNotNull(config.propertyBinder) { "Property binder missing for ${this::class.simpleName}" }
//    }
//
//    fun PropertyBinder<*, *>.validateBindings() {
//        propertyBindings.forEach { binding ->
//            val dataProp = binding.dataProperty
//            val entityProp = binding.entityProperty
//            requireNotNull(dataProp) { "Data property is null in binding ${binding::class.simpleName}" }
//            requireNotNull(entityProp) { "Entity property is null in binding ${binding::class.simpleName}" }
//        }
//    }
//
//    suspend fun RelationshipBinder<*, *, *>.validateChildren() {
//        childBindings.values.forEach { childBinding ->
//            val childClass = childBinding.childClass
//            childClass.initialization()
//            requireNotNull(childClass.repository) {
//                "Child DTO ${childClass::class.simpleName} missing repository"
//            }
//        }
//    }
//
//    suspend fun runPreflightForAllDTOs(vararg dtoClasses: DTOClass<*>) {
//        withTransactionIfNone {
//            dtoClasses.forEach { it.preflightCheck() }
//        }
//    }
}
