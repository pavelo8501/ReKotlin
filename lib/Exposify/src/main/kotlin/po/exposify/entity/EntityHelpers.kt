package po.exposify.entity

import po.exposify.entity.classes.ExposifyEntity


fun <T: ExposifyEntity>  T.exposifyEntity(): ExposifyEntity{
    return this
}