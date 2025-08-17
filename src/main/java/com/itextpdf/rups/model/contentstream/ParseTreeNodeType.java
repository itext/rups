/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.model.contentstream;

/**
 * Contains content stream parse tree node types.
 *
 * <p>
 * Marker type is a type, which does not have anything to do with PDF, but it
 * is used internally as markers with a special meaning, like the root of the
 * parse tree.
 * </p>
 *
 * <p>
 * Primitive type means, that it is a leaf node and it is defined by its text.
 * For example, {@code NUMERIC} is a primitive type, which has no children and
 * contains text of a number.
 * </p>
 *
 * <p>
 * Composite type means, that this node does not contain text, but is just a
 * container for other primitive nodes. For example, {@code STRING_LITERAL} is
 * a composite type, and its children contain string open markers, string
 * data and string close markers.
 * </p>
 */
public enum ParseTreeNodeType {
    /**
     * Marker type. Root of the parse tree.
     */
    ROOT,
    /**
     * Marker type. A sentinel for a circular linked list of children.
     */
    CHILD_SENTINEL,

    /**
     * Primitive type. Whitespace between tokens.
     */
    WHITESPACE,

    /**
     * Primitive type. End-of-line comment marker with its body. Whitespace
     * at the end is not included.
     */
    COMMENT,

    /**
     * Primitive type. Boolean {@code true} and {@code false} objects.
     */
    BOOLEAN,

    /**
     * Primitive type. Numeric PDF objects.
     */
    NUMERIC,

    /**
     * Composite type. Literal PDF strings, enclosed in parentheses.
     */
    STRING_LITERAL,
    /**
     * Primitive type. Byte sequence within a literal PDF string, excluding
     * left and right parentheses.
     */
    STRING_LITERAL_DATA,
    /**
     * Primitive type. A left parenthesis.
     *
     * <p>
     * First child of a {@code STRING_LITERAL} node will be of this type. One
     * literal node can have multiple open tokens, as they are parsed
     * separately to support parentheses matching.
     * </p>
     *
     * <p>
     * Can also be found outside of a {@code STRING_LITERAL} node as an
     * unexpected token.
     * </p>
     */
    STRING_LITERAL_OPEN,
    /**
     * Primitive type. A right parenthesis. This will be the first child of a
     * {@code STRING_LITERAL} node.
     *
     * <p>
     * Should be the last child of a {@code STRING_LITERAL} node, if it has
     * been finished and closed properly. One literal node can have multiple
     * close tokens, as they are parsed separately to support parentheses
     * matching.
     * </p>
     *
     * <p>
     * In contrast to {@code STRING_LITERAL_OPEN}, these should only be found
     * withing a {@code STRING_LITERAL} node.
     * </p>
     */
    STRING_LITERAL_CLOSE,

    /**
     * Composite type. Hexadecimal PDF strings, enclosed in &lt;&gt;.
     */
    STRING_HEX,
    /**
     * Primitive type. Byte sequence within a hexadecimal PDF string,
     * excluding &lt; and &gt;.
     */
    STRING_HEX_DATA,
    /**
     * Primitive type. &lt; symbol.
     *
     * <p>
     * First child of a {@code STRING_HEX} node will be of this type. Compared
     * to literal strings, there can only be one in each string. But they are
     * still parsed separately to support begin/end matching.
     * </p>
     *
     * <p>
     * These should only be found withing a {@code STRING_HEX} node.
     * </p>
     */
    STRING_HEX_OPEN,
    /**
     * Primitive type. &gt; symbol.
     *
     * <p>
     * Should be the last child of a {@code STRING_HEX} node, if it has
     * been finished and closed properly. Compared to literal strings, there
     * can only be one in each string. But they are still parsed separately to
     * support begin/end matching.
     * </p>
     *
     * <p>
     * These should only be found withing a {@code STRING_HEX} node.
     * </p>
     */
    STRING_HEX_CLOSE,

    /**
     * Primitive type. Name PDF objects.
     */
    NAME,

    /**
     * Composite type. PDF arrays, enclosed in square brackets.
     */
    ARRAY,
    /**
     * Primitive type. A left square bracket.
     *
     * <p>
     * First child of an {@code ARRAY} node will be of this type. Compared to
     * literal strings, there can only be one in each array. But they are
     * still parsed separately to support begin/end matching.
     * </p>
     *
     * <p>
     * These should only be found withing an {@code ARRAY} node.
     * </p>
     */
    ARRAY_OPEN,
    /**
     * Primitive type. A right square bracket.
     *
     * <p>
     * Should be the last child of an {@code ARRAY} node, if it has
     * been finished and closed properly. Compared to literal strings, there
     * can only be one in each array. But they are still parsed separately to
     * support begin/end matching.
     * </p>
     *
     * <p>
     * These should only be found withing an {@code ARRAY} node.
     * </p>
     */
    ARRAY_CLOSE,

    /**
     * Composite type. PDF dictionaries, enclosed in &lt;&lt; &gt;&gt;.
     */
    DICTIONARY,
    /**
     * Primitive type. A &lt;&lt; token.
     *
     * <p>
     * First child of a {@code DICTIONARY} node will be of this type. Compared
     * to literal strings, there can only be one in each dictionary. But they
     * are still parsed separately to support begin/end matching.
     * </p>
     *
     * <p>
     * These should only be found withing a {@code DICTIONARY} node.
     * </p>
     */
    DICTIONARY_OPEN,
    /**
     * Primitive type. A &gt;&gt; token.
     *
     * <p>
     * Should be the last child of a {@code DICTIONARY} node, if it has
     * been finished and closed properly. Compared to literal strings, there
     * can only be one in each dictionary. But they are still parsed
     * separately to support begin/end matching.
     * </p>
     *
     * <p>
     * These should only be found withing an {@code DICTIONARY} node.
     * </p>
     */
    DICTIONARY_CLOSE,

    /**
     * Primitive type. {@code null} objects.
     */
    NULL,

    /**
     * Primitive type. PDF content stream operator.
     */
    OPERATOR,

    /**
     * Primitive type. A byte sequence, which should not be rendered as text.
     * An example would be a body of an inline image.
     */
    BINARY_DATA,

    /**
     * Primitive type. A byte sequence, which is unexpected and has not been
     * covered by any of the concrete types.
     */
    UNKNOWN;

    /**
     * Returns whether this is a marker type or not.
     *
     * @return Whether this is a marker type or not.
     */
    public final boolean isMarker() {
        switch (this) {
            case ROOT:
            case CHILD_SENTINEL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns whether this is a primitive type or not.
     *
     * @return Whether this is a primitive type or not.
     */
    public final boolean isPrimitive() {
        switch (this) {
            case WHITESPACE:
            case COMMENT:
            case BOOLEAN:
            case NUMERIC:
            case STRING_LITERAL_DATA:
            case STRING_LITERAL_OPEN:
            case STRING_LITERAL_CLOSE:
            case STRING_HEX_DATA:
            case STRING_HEX_OPEN:
            case STRING_HEX_CLOSE:
            case NAME:
            case ARRAY_OPEN:
            case ARRAY_CLOSE:
            case DICTIONARY_OPEN:
            case DICTIONARY_CLOSE:
            case NULL:
            case OPERATOR:
            case BINARY_DATA:
            case UNKNOWN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns whether this is a composite type or not.
     *
     * @return Whether this is a composite type or not.
     */
    public final boolean isComposite() {
        switch (this) {
            case STRING_LITERAL:
            case STRING_HEX:
            case ARRAY:
            case DICTIONARY:
                return true;
            default:
                return false;
        }
    }
}
