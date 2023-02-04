package com.pascalnb.dbwrapper

/**
 * Interface that specifies a tuple with string values. The values are to be retrieved
 * by name of the column/attribute or by index of the value.
 */
interface Tuple {
    /**
     * Returns the value located at the position of the given attribute name.
     *
     * @param attributeName the attribute name
     * @return the value
     */
    operator fun get(attributeName: String): String?

    /**
     * Returns the value located at the given index.
     *
     * @param index the index
     * @return the value
     */
    operator fun get(index: Int): String?
}