package com.gtbluesky.guard.res.data

object DataType {
    // The 'data' is either 0 or 1, specifying this resource is either
    // undefined or empty, respectively.
    const val TYPE_NULL = 0x00
    // The 'data' holds a ResTable_ref, a reference to another resource
    // table entry.
    const val TYPE_REFERENCE = 0x01
    // The 'data' holds an attribute resource identifier.
    const val TYPE_ATTRIBUTE = 0x02
    // The 'data' holds an index into the containing resource table's
    // global value string pool.
    const val TYPE_STRING = 0x03
    // The 'data' holds a single-precision floating point number.
    const val TYPE_FLOAT = 0x04
    // The 'data' holds a complex number encoding a dimension value,
    // such as "100in".
    const val TYPE_DIMENSION = 0x05
    // The 'data' holds a complex number encoding a fraction of a
    // container.
    const val TYPE_FRACTION = 0x06
    // The 'data' holds a dynamic ResTable_ref, which needs to be
    // resolved before it can be used like a TYPE_REFERENCE.
    const val TYPE_DYNAMIC_REFERENCE = 0x07
    // The 'data' holds an attribute resource identifier, which needs to be resolved
    // before it can be used like a TYPE_ATTRIBUTE.
    const val TYPE_DYNAMIC_ATTRIBUTE = 0x08

    // Beginning of integer flavors...
    const val TYPE_FIRST_INT = 0x10

    // The 'data' is a raw integer value of the form n..n.
    const val TYPE_INT_DEC = 0x10
    // The 'data' is a raw integer value of the form 0xn..n.
    const val TYPE_INT_HEX = 0x11
    // The 'data' is either 0 or 1, for input "false" or "true" respectively.
    const val TYPE_INT_BOOLEAN = 0x12

    // Beginning of color integer flavors...
    const val TYPE_FIRST_COLOR_INT = 0x1c

    // The 'data' is a raw integer value of the form #aarrggbb.
    const val TYPE_INT_COLOR_ARGB8 = 0x1c
    // The 'data' is a raw integer value of the form #rrggbb.
    const val TYPE_INT_COLOR_RGB8 = 0x1d
    // The 'data' is a raw integer value of the form #argb.
    const val TYPE_INT_COLOR_ARGB4 = 0x1e
    // The 'data' is a raw integer value of the form #rgb.
    const val TYPE_INT_COLOR_RGB4 = 0x1f

    // ...end of integer flavors.
    const val TYPE_LAST_COLOR_INT = 0x1f

    // ...end of integer flavors.
    const val TYPE_LAST_INT = 0x1f
}