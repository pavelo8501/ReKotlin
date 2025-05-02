package po.exposify.dto.components.property_binder.bindings

//
//class ReferencedBindingDepr<DATA, ENTITY>(
//    override val dataProperty: KMutableProperty1<DATA, Long>,
//    override val referencedProperty: KMutableProperty1<ENTITY, out ExposifyEntity>,
//    val dtoClass: DTOClass<out ModelDTO>,
//): PropertyBindingOption<DATA, ENTITY, Long> where DATA: DataModel, ENTITY: ExposifyEntity
//{
//    override val propertyType: PropertyType = PropertyType.REFERENCED
//
//    val castedEntityProperty = referencedProperty.castOrOperationsEx<KMutableProperty1<ENTITY,ExposifyEntity>>(
//        "Cast to KMutableProperty1<ENTITY,ExposifyEntity> failed",
//        ExceptionCode.CAST_FAILURE)
//
//    private fun updateEntityToModel(entity:ENTITY, data :DATA, forced: Boolean){
//
//        val referencedEntity = castedEntityProperty.get(entity)
//        dataProperty.set(data, referencedEntity.id.value)
//    }
//
//    private suspend fun updateModelToEntity(data:DATA, entity: ENTITY, forced: Boolean){
//
//        val castedDtoClass = dtoClass.safeCast<DTOClass<ModelDTO>>().getOrOperationsEx(
//            "Cast to DTOClass<ModelDTO> failed",  ExceptionCode.CAST_FAILURE)
//
//        val referencedId = dataProperty.get(data)
//        if(!dtoClass.initialized){
//            dtoClass.initialization()
//        }
//        val dto = castedDtoClass.pickById(referencedId).getDTO()
//        castedEntityProperty.set(entity, dto.entityDAO)
//
//    }
//
//    suspend fun update(data: DATA, entity : ENTITY, mode: UpdateMode, callback:  (suspend (String, PropertyType, UpdateMode) -> Unit)?){
//        when(mode){
//            UpdateMode.ENTITY_TO_MODEL ->  updateEntityToModel(entity, data, false)
//            UpdateMode.ENTITY_TO_MODEL_FORCED -> updateEntityToModel(entity, data, true)
//            UpdateMode.MODEL_TO_ENTITY -> updateModelToEntity(data, entity,false)
//            UpdateMode.MODEL_TO_ENTITY_FORCED -> updateModelToEntity(data, entity,true)
//        }
//    }
//}