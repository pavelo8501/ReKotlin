package po.exposify.entity

import po.exposify.entity.classes.ExposifyEntityBase


fun <T: ExposifyEntityBase>  T.exposifyEntity(): ExposifyEntityBase{
    return this
}