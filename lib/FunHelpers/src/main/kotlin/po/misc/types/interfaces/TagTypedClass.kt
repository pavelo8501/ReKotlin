package po.misc.types.interfaces

import po.misc.collections.ComparableType
import po.misc.types.TaggedType

/**
 * Represents a typed class that is associated with an enum-based tag.
 *
 * This interface is useful for marking any class that wraps or refers to a [TaggedType],
 * combining both type safety ([T]) and tagging semantics ([E]).
 *
 * It extends [ComparableType] to ensure compatibility with systems that compare type metadata,
 * allowing implementors to participate in type comparisons.
 *
 * @param T The Kotlin type being tagged
 * @param E The enum type used as a tag
 *
 * @property tagType The [TaggedType] instance combining the type and the enum tag
 *
 * Example usage:
 * ```
 * class MyTaggedContainer : TagTypedClass<MyDTO, MyEnum> {
 *     override val tagType = TaggedType.create(MyDTO::class, MyEnum.SELECT)
 * }
 * ```
 */
interface TagTypedClass<T: Any, E: Enum<E>>{

    /**
     * The strongly typed and tagged descriptor for this class.
     */
    val tagType: TaggedType<T, E>
}