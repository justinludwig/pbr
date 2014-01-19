package org.c02e.plugin.pbr.run

/**
 * Helper utilities for renderers.
 */
interface RendererTools {

    /**
     * Returns a string of formatted, encoded html attributes
     * for the specified map of attribute name,value pairs.
     * When the value is a collection, flattens the collection,
     * and chooses the first non-null/blank item in the collection
     * (except for class/style attrs, in which case
     * it concatenates all the non-null/blank values).
     * Skips attributes with a null/blank/empty name/value.
     * <p>For example, <code>[title:['', 'AT&amp;T', 'Cingular'], 
     * width:'', class:[null, 'red', 'green']]</code> returns:
     * <code>' width="AT&amp;amp;T" class="red green"'</code>
     */
    String attrs(Map m)

    /**
     * Returns a string escaped for HTML element content.
     * @param s String to escape.
     * @return Escaped string.
     */
    String text(s)

    /**
     * Returns a string with illegal HTML attribute-name characters removed.
     * @param s String to clean.
     * @return Cleaned string.
     */
    String attrName(s)

    /**
     * Returns a string escaped for an HTML attribute value.
     * @param s String to escape.
     * @return Escaped string.
     */
    String attrValue(s)

}
