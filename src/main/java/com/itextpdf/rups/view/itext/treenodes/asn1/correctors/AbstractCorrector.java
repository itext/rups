/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;

import java.math.BigInteger;
import java.util.function.BiFunction;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <p>An abstract class for correcting a context-less generic ASN.1 object
 * tree with the use of information from ASN.1 definitions inside
 * specifications.</p>
 *
 * <p>With how ASN.1 is encoded, you cannot 100% accurately represent display
 * the object to the user without knowing the object definition.</p>
 *
 * <p>Some of the things a corrector can do:</p>
 * <ul>
 *     <li>Provide descriptive names to generic ASN.1 objects. Since
 *     definitions contain field names, we could show them to the user to make
 *     it easier to read the object.</li>
 *
 *     <li>Specify correct base type for types, which have context-specific
 *     tags. If in the specification there is an IMPLICIT tag, then the actual
 *     UNIVERSAL ASN.1 type of the object is not encoded in the result, only
 *     that tag. For example, if there is a "[0] IMPLICIT UTCTime" field in
 *     the spec, there is no way to get the "UTCTime" part from the data.
 *     Since a corrector "knows" the specification, it can provide the correct
 *     UNIVERSAL type.</li>
 *
 *     <li>Provide descriptive names for some of the values. For example,
 *     INTEGER and ENUMERATED values are predefined and often have descriptive
 *     names, which are, obviously, not included in the data.</li>
 *
 *     <li>Expand encoded BER/DER ASN.1 object fields. In some cases an ASN.1
 *     object may contain another ASN.1 object, which is BER/DER encoded and
 *     stored as an OCTET STRING. One such example would be extension
 *     attributes in X.509 certificates. A corrector can expand such OCTET
 *     STRING into a tree.</li>
 *
 *     <li>Expand BIT STRINGS, which store bit flags. Since flags have names,
 *     we can expand such a string into a named tree of booleans.</li>
 * </ul>
 */
public abstract class AbstractCorrector {
    /**
     * Returns the default variable name for the root node of the handled tree.
     *
     * @return The default variable name.
     */
    public abstract String getDefaultVariableName();

    /**
     * <p>Corrects the provided ASN.1 object tree in relation to the provided
     * ASN.1 object. If the type of the provided object is correct, it will
     * set the node name to the provided variableName.</p>
     *
     * <p>In the majority of cases you would want to just use
     * {@link #correct(AbstractAsn1TreeNode, String)} instead. But if you have
     * a node, which covers for multiple ASN1Primitive classes, like
     * {@link Asn1TaggedObjectTreeNode}, you need to use this method and pass
     * the <i>base</i> object as the second parameter. You can also use this
     * method, when you encounter multiple nested tags in one node.</p>
     *
     * <p>See the class documentation for more information.</p>
     *
     * @param node         ASN.1 object tree to correct.
     * @param obj          ASN.1 object to process within the tree node.
     * @param variableName RFC field name to use for the root node.
     */
    public abstract void correct(AbstractAsn1TreeNode node, ASN1Primitive obj, String variableName);

    /**
     * <p>Corrects the provided ASN.1 object tree in relation to the provided
     * ASN.1 object. If the type of the provided object is correct, it will
     * set the node name to the correct-provided default name.</p>
     *
     * <p>In the majority of cases you would want to just use
     * {@link #correct(AbstractAsn1TreeNode, String)} instead. But if you have
     * a node, which covers for multiple ASN1Primitive classes, like
     * {@link Asn1TaggedObjectTreeNode}, you need to use this method and pass
     * the <i>base</i> object as the second parameter. You can also use this
     * method, when you encounter multiple nested tags in one node.</p>
     *
     * <p>See the class documentation for more information.</p>
     *
     * @param node ASN.1 object tree to correct.
     * @param obj  ASN.1 object to process within the tree node.
     */
    public void correct(AbstractAsn1TreeNode node, ASN1Primitive obj) {
        correct(node, obj, getDefaultVariableName());
    }

    /**
     * Corrects the provided ASN.1 object tree. If the type of the root node
     * is correct, it will set its name to the provided variableName. See the
     * class documentation for more information.
     *
     * @param node         ASN.1 object tree to correct.
     * @param variableName RFC field name to use for the root node.
     */
    public void correct(AbstractAsn1TreeNode node, String variableName) {
        correct(node, node.getAsn1Primitive(), variableName);
    }

    /**
     * Corrects the provided ASN.1 object tree. A corrector-provided default
     * name will be used for the root. See the class documentation for more
     * information.
     *
     * @param node ASN.1 object tree to correct.
     */
    public void correct(AbstractAsn1TreeNode node) {
        correct(node, getDefaultVariableName());
    }

    /**
     * Returns true, if the object has a UNIVERSAL tag (i.e. is a predefined
     * ASN.1 type).
     *
     * @param obj ASN.1 primitive to check.
     *
     * @return True, if object has a UNIVERSAL tag, otherwise false.
     */
    protected static boolean isUniversalType(ASN1Primitive obj) {
        return !(obj instanceof ASN1TaggedObject);
    }

    /**
     * Returns true, if the tree node contains an object with a UNIVERSAL tag
     * (i.e. is a predefined ASN.1 type).
     *
     * @param node ASN.1 tree node, which contains the ASN.1 object to check.
     *
     * @return True, if node contains an object with a UNIVERSAL tag,
     * otherwise false.
     */
    protected static boolean isUniversalType(AbstractAsn1TreeNode node) {
        return isUniversalType(node.getAsn1Primitive());
    }

    /**
     * Returns true, if the object has a specific UNIVERSAL tag (i.e. is a
     * predefined ASN.1 type).
     *
     * @param obj  ASN.1 primitive to check.
     * @param type Expected ASN.1 primitive type.
     *
     * @return True, if object has a specific UNIVERSAL tag, otherwise false.
     */
    protected static boolean isUniversalType(ASN1Primitive obj, Class<? extends ASN1Primitive> type) {
        return type.isInstance(obj);
    }

    /**
     * Returns true, if the tree node contains an object with a specific
     * UNIVERSAL tag (i.e. is a predefined ASN.1 type).
     *
     * @param node ASN.1 tree node, which contains the ASN.1 object to check.
     * @param type Expected ASN.1 primitive type.
     *
     * @return True, if node contains an object with a specific UNIVERSAL tag,
     * otherwise false.
     */
    protected static boolean isUniversalType(AbstractAsn1TreeNode node, Class<? extends ASN1Primitive> type) {
        return isUniversalType(node.getAsn1Primitive(), type);
    }

    /**
     * Returns true, if the object has a specified IMPLICIT context-specific
     * tag.
     *
     * @param obj   ASN.1 primitive to check.
     * @param tagNo Expected tag number.
     *
     * @return {@code true}, if object has a specified IMPLICIT
     * context-specific tag, otherwise {@code false}.
     */
    protected static boolean isImplicitContextSpecificType(ASN1Primitive obj, int tagNo) {
        if (!(obj instanceof ASN1TaggedObject)) {
            return false;
        }
        /*
         * Since the existence of IMPLICIT tags makes parsing ambiguous, we
         * can have objects, that should be parsed as IMPLICIT, but are parsed
         * as EXPLICIT by Bouncy Castle. Because of this we cannot use
         * isExplicit method to filter out non-implicit tags. So we are only
         * checking the number and rely on the caller to fix the explicitness
         * and base object themselves.
         */
        return ((ASN1TaggedObject) obj).hasContextTag(tagNo);
    }

    /**
     * Returns true, if the tree node contains an object with a specified
     * IMPLICIT context-specific tag.
     *
     * @param node  ASN.1 tree node, which contains the ASN.1 object to check.
     * @param tagNo Expected tag number.
     *
     * @return {@code true}, if node contains an object with a specified
     * IMPLICIT context-specific tag, otherwise {@code false}.
     */
    protected static boolean isImplicitContextSpecificType(AbstractAsn1TreeNode node, int tagNo) {
        return isImplicitContextSpecificType(node.getAsn1Primitive(), tagNo);
    }

    /**
     * Returns true, if the object has a specified EXPLICIT context-specific
     * tag.
     *
     * @param obj   ASN.1 primitive to check.
     * @param tagNo Expected tag number.
     *
     * @return {@code true}, if object has a specified EXPLICIT
     * context-specific tag, otherwise {@code false}.
     */
    protected static boolean isExplicitContextSpecificType(ASN1Primitive obj, int tagNo) {
        if (!(obj instanceof ASN1TaggedObject)) {
            return false;
        }
        final ASN1TaggedObject tagged = (ASN1TaggedObject) obj;
        /*
         * Since the existence of IMPLICIT tags makes parsing ambiguous, we
         * can have objects, that should be parsed as IMPLICIT, but are parsed
         * as EXPLICIT by Bouncy Castle. But there are cases, where you can be
         * 100%, that this is not an EXPLICIT tag, because DER encoding
         * EXPLICIT tags have the constructed bit set. So this check is still
         * useful in the EXPLICIT case.
         */
        if (!tagged.isExplicit()) {
            return false;
        }
        return tagged.hasContextTag(tagNo);
    }

    /**
     * Returns true, if the tree node contains an object with a specified
     * EXPLICIT context-specific tag.
     *
     * @param node  ASN.1 tree node, which contains the ASN.1 object to check.
     * @param tagNo Expected tag number.
     *
     * @return {@code true}, if node contains an object with a specified
     * EXPLICIT context-specific tag, otherwise {@code false}.
     */
    protected static boolean isExplicitContextSpecificType(AbstractAsn1TreeNode node, int tagNo) {
        return isExplicitContextSpecificType(node.getAsn1Primitive(), tagNo);
    }

    /**
     * Returns {@code true}, if the object has a specified EXPLICIT
     * context-specific tag with a base object of a specific type behind that
     * tag.
     *
     * @param obj   ASN.1 primitive to check.
     * @param tagNo Expected tag number.
     * @param type  Expected ASN.1 primitive base object type.
     *
     * @return {@code true}, if object has a specified EXPLICIT
     * context-specific tag with a base object of a specific type behind that
     * tag, otherwise {@code false}.
     */
    protected static boolean isExplicitContextSpecificType(
            ASN1Primitive obj,
            int tagNo,
            Class<? extends ASN1Primitive> type
    ) {
        if (!isExplicitContextSpecificType(obj, tagNo)) {
            return false;
        }
        return type.isInstance(((ASN1TaggedObject) obj).getBaseObject().toASN1Primitive());
    }

    /**
     * Returns {@code true}, if the tree node contains an object with a
     * specified EXPLICIT context-specific tag and a base object of a specific
     * type behind that tag.
     *
     * @param node  ASN.1 tree node, which contains the ASN.1 object to check.
     * @param tagNo Expected tag number.
     * @param type  Expected ASN.1 primitive base object type.
     *
     * @return {@code true}, if the tree node contains an object with a
     * specified EXPLICIT context-specific tag and a base object of a specific
     * type behind that tag, otherwise {@code false}.
     */
    protected static boolean isExplicitContextSpecificType(
            AbstractAsn1TreeNode node,
            int tagNo,
            Class<? extends ASN1Primitive> type
    ) {
        return isExplicitContextSpecificType(node.getAsn1Primitive(), tagNo, type);
    }

    /**
     * Returns the base object of an {@link ASN1TaggedObject}.
     *
     * @param obj ASN.1 tagged object to get the base object for.
     *
     * @return The base object for an {@link ASN1TaggedObject}.
     */
    protected static ASN1Primitive getBaseObject(ASN1TaggedObject obj) {
        return obj.getBaseObject().toASN1Primitive();
    }

    /**
     * Returns the base object of an {@link ASN1TaggedObject} behind a
     * {@link Asn1TaggedObjectTreeNode}.
     *
     * @param node ASN.1 tagged object tree node, which contains the ASN.1
     *             tagged object to get the base object for.
     *
     * @return The base object behind {@link Asn1TaggedObjectTreeNode}.
     */
    protected static ASN1Primitive getBaseObject(Asn1TaggedObjectTreeNode node) {
        return getBaseObject(node.getAsn1Primitive());
    }

    /**
     * Returns the base object of an {@link ASN1TaggedObject}.
     *
     * <p>This is just a convenient wrapper to call
     * {@link ASN1TaggedObject#getBaseObject()} with the necessary casts on an
     * {@link ASN1Primitive}. Using this function on types different from
     * {@link ASN1TaggedObject} is undefined behavior.</p>
     *
     * @param obj ASN.1 tagged object to get the base object for.
     *
     * @return The base object for an {@link ASN1TaggedObject}.
     */
    protected static ASN1Primitive getBaseObjectUnchecked(ASN1Primitive obj) {
        assert obj instanceof ASN1TaggedObject;
        return ((ASN1TaggedObject) obj).getBaseObject().toASN1Primitive();
    }

    /**
     * Returns the base object of an {@link ASN1TaggedObject} behind a
     * {@link Asn1TaggedObjectTreeNode}.
     *
     * <p>This is just a convenient wrapper to call
     * {@link ASN1TaggedObject#getBaseObject()} with the necessary casts on an
     * {@link ASN1Primitive} and {@link AbstractAsn1TreeNode}. Using this
     * function on types different from {@link Asn1TaggedObjectTreeNode} is
     * undefined behavior.</p>
     *
     * @param node ASN.1 tagged object tree node, which contains the ASN.1
     *             tagged object to get the base object for.
     *
     * @return The base object behind {@link Asn1TaggedObjectTreeNode}.
     */
    protected static ASN1Primitive getBaseObjectUnchecked(AbstractAsn1TreeNode node) {
        assert node instanceof Asn1TaggedObjectTreeNode;
        return getBaseObjectUnchecked(node.getAsn1Primitive());
    }

    /**
     * Set the RFC field name for a node, if it is backed by an ASN.1 object
     * with a UNIVERSAL tag of the specified primitive type.
     *
     * @param node         ASN.1 tree node to correct.
     * @param obj          ASN.1 object to process within the tree node.
     * @param type         Expected ASN.1 primitive type.
     * @param variableName RFC field name to use for the node.
     */
    protected static void correctPrimitiveUniversalType(
            AbstractAsn1TreeNode node,
            ASN1Primitive obj,
            Class<? extends ASN1Primitive> type,
            String variableName
    ) {
        if (!isUniversalType(obj, type)) {
            return;
        }
        node.setRfcFieldName(variableName);
    }

    /**
     * Set the RFC field name for a node, if it is backed by an ASN.1 object
     * with a UNIVERSAL tag of the specified primitive type.
     *
     * @param node         ASN.1 tree node to correct.
     * @param type         Expected ASN.1 primitive type.
     * @param variableName RFC field name to use for the node.
     */
    protected static void correctPrimitiveUniversalType(
            AbstractAsn1TreeNode node,
            Class<? extends ASN1Primitive> type,
            String variableName
    ) {
        correctPrimitiveUniversalType(node, node.getAsn1Primitive(), type, variableName);
    }

    /**
     * Sets the RFC field name for a node, if it is backed by an ASN.1 object
     * with a UNIVERSAL OBJECT IDENTIFIER tag. If this is the case, will also
     * return the identifier string.
     *
     * @param node         ASN.1 tree node to correct.
     * @param variableName RFC field name to use for the node.
     *
     * @return OBJECT IDENTIFIER string, if the node matches, otherwise {@code null}.
     */
    protected static String correctUniversalObjectIdentifier(AbstractAsn1TreeNode node, String variableName) {
        if (!isUniversalType(node, ASN1ObjectIdentifier.class)) {
            return null;
        }
        node.setRfcFieldName(variableName);
        return ((ASN1ObjectIdentifier) node.getAsn1Primitive()).getId();
    }

    /**
     * Fixes explicitness and the base object type of the provided object,
     * which is handled by {@code node}.
     *
     * <p>With how DER-encoding works, there is no way for the parser to know,
     * what type is hiding behind an IMPLICIT tag. Bouncy Castle parsing works
     * on the following assumptions.</p>
     *
     * <ul>
     *      <li>If tag has a primitive type, per DER-encoding rules it can
     *      only be an IMPLICIT tag with a primitive type behind it. So Bouncy
     *      Castle parses such cases as an IMPLICIT OCTET STRING.</li>
     *      <li>If tag has a constructed type and there are multiple values
     *      within its value, per DER-encoding rules it can only be an
     *      IMPLICIT tag with a constructed type behind it. So Bouncy Castle
     *      parses such cases as an IMPLICIT SEQUENCE.</li>
     *      <li>If tag has a constructed type and there is only one value
     *      within its value, per DER-encoding rules it there can be two cases.
     *      It is either an IMPLICIT tag with a SEQUENCE or SET behind it,
     *      which contains only one element. Or it is an EXPLICIT with that
     *      only value behind it. Bouncy Castle assumes the latter and this is
     *      the case, where an implicitly tagged type is marked as explicit.
     *      </li>
     * </ul>
     *
     * <p>With that in mind, Bouncy Castle should handle EXPLICIT tags
     * correctly all the time. But since it has no idea about IMPLICIT tags,
     * you need to help it by specifying the type manually. This is what this
     * function is for. It takes an {@link Asn1TaggedObjectTreeNode}, one of
     * the {@link ASN1TaggedObject} it is responsible for (there can be a tag
     * behind a tag up to infinity) and one of the type-specific Bouncy
     * Castle <tt>getInstance</tt> function. If it is possible to reinterpret
     * the object as the desired type, it gets replace in the tree node and
     * the fixed object is returned. If it is not possible or the type was
     * already correct, the old object is returned.</p>
     *
     * <p>To figure out whether the fix was successful or not, check whether
     * the type of the base object in the return value matches the expected
     * type.</p>
     *
     * @param node            ASN.1 tagged object node, which contains the
     *                        object.
     * @param oldObj          ASN.1 tagged object, which needs fixing. Will
     *                        be returned, if fix is not possible, or if it
     *                        was already correct.
     * @param getInstanceFunc Bouncy Castle type-specific <tt>getInstance</tt>
     *                        function.
     *
     * @return The object, that is now stored in place of <tt>oldObj</tt>. If
     * fix is not possible, or if it is already correct, <tt>oldObj</tt> will
     * be returned.
     */
    protected static ASN1TaggedObject fixImplicitContextSpecificObject(
            Asn1TaggedObjectTreeNode node,
            ASN1TaggedObject oldObj,
            BiFunction<ASN1TaggedObject, Boolean, ASN1Encodable> getInstanceFunc
    ) {
        final ASN1Encodable newBaseObj;
        try {
            newBaseObj = getInstanceFunc.apply(oldObj, Boolean.FALSE);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return oldObj;
        }
        /*
         * If there is nothing to change (i.e. it is already IMPLICIT and
         * getInstance returned the same base object back), then there is no
         * need to waste time rebuilding the whole subtree.
         */
        if (!oldObj.isExplicit() && (newBaseObj == oldObj.getBaseObject())) {
            return oldObj;
        }
        final DERTaggedObject newObj = new DERTaggedObject(false, oldObj.getTagNo(), newBaseObj);

        /*
         * This should not fail, as it failing means, that oldObj was not
         * present in the node, so it is a programmer error.
         */
        final boolean replaceSuccess = node.replace(oldObj, newObj);
        assert replaceSuccess;

        return newObj;
    }

    /**
     * Fixes explicitness and the base object type of the provided object,
     * which is handled by {@code node}.
     *
     * <p>See {@link #fixImplicitContextSpecificObject(Asn1TaggedObjectTreeNode, ASN1TaggedObject, BiFunction)}
     * for additional information.</p>
     *
     * @param node            ASN.1 tagged object node, which contains the
     *                        object.
     * @param getInstanceFunc Bouncy Castle type-specific <tt>getInstance</tt>
     *                        function.
     *
     * @return The object, that is now stored inside <tt>node</tt>. If fix it
     * not possible, or if it was already correct, the old object will be
     * returned.
     */
    protected static ASN1TaggedObject fixImplicitContextSpecificObject(
            Asn1TaggedObjectTreeNode node,
            BiFunction<ASN1TaggedObject, Boolean, ASN1Encodable> getInstanceFunc
    ) {
        return fixImplicitContextSpecificObject(node, node.getAsn1Primitive(), getInstanceFunc);
    }

    /**
     * Returns whether a bit was set in a bit string.
     *
     * @param flags Bit string representation as an array of bytes.
     * @param index Bit index to check the flag at.
     *
     * @return {@code true}, if the bit is present and set, {@code false}
     * otherwise.
     */
    protected static boolean hasFlag(byte[] flags, int index) {
        assert flags != null;
        assert index >= 0;

        final int byteIndex = index >>> 3;
        if (byteIndex >= flags.length) {
            return false;
        }
        final int mask = 0x80 >>> (index & 7);
        return (flags[byteIndex] & mask) != 0;
    }

    /**
     * Returns whether the number is in a <tt>[0; end)</tt> range.
     *
     * @param value Number to test.
     * @param end   Range end value, exclusive.
     *
     * @return {@code true}, if number is in a <tt>[0; end)</tt> range,
     * {@code false} otherwise.
     */
    protected static boolean isNumberInRange(BigInteger value, int end) {
        assert value != null;
        assert end > 0;

        try {
            final int v = value.intValueExact();
            return (0 <= v) && (v < end);
        } catch (ArithmeticException e) {
            return false;
        }
    }
}
