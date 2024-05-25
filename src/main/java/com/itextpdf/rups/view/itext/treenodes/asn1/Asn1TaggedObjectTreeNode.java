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
package com.itextpdf.rups.view.itext.treenodes.asn1;

import org.bouncycastle.asn1.ASN1ApplicationSpecific;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * Tree node for showing ASN.1 objects with context-specific tags.
 */
public final class Asn1TaggedObjectTreeNode extends AbstractAsn1TreeNode {
    /**
     * Tree node, which represent the actual type behind the ASN.1 tag. Display
     * methods are delegated to this object to avoid code duplication, as
     * otherwise this class would need to reimplement support for all the
     * possible universal ASN.1 types.
     */
    private AbstractAsn1TreeNode baseObjectNode;

    /**
     * Creates a new tree node for an ASN.1 object with a non-UNIVERSAL tag.
     *
     * @param object ASN.1 object with a non-UNIVERSAL tag.
     */
    public Asn1TaggedObjectTreeNode(ASN1ApplicationSpecific object) {
        /*
         * For some reason, even though ASN1ApplicationSpecific is deprecated,
         * ASN1Primitive.fromByteArray can still return it... I don't think
         * there was any spec, when an APPLICATION tag was used, so it should
         * not be a problem, if it pops-up somewhere and there is no special
         * code for it. But we still want to display it in a tagged object
         * node.
         */
        this(object.getTaggedObject());
    }

    /**
     * Creates a new tree node for an ASN.1 object with a non-UNIVERSAL tag.
     *
     * @param object ASN.1 object with a non-UNIVERSAL tag.
     */
    public Asn1TaggedObjectTreeNode(ASN1TaggedObject object) {
        super(null, object);
        reload();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsn1Type() {
        // Need full type, in case there is a nested tag
        return baseObjectNode.getAsn1FullType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsn1DisplayTag() {
        final ASN1TaggedObject obj = getAsn1Primitive();
        /*
         * Practically speaking, "[99] EXPLICIT APPLICATION" is out worst case
         * for length, which is 25.
         */
        final StringBuilder sb = new StringBuilder(25);
        sb.append('[').append(obj.getTagNo()).append(']');
        /*
         * Here we will add tag class information.
         *
         * BERTags.CONTEXT_SPECIFIC is the most common case, so we are not
         * going to add anything for it, as it is the default. This is also how
         * it usually looks in the spec documents.
         *
         * BERTags.UNIVERSAL case should be impossible, since Bouncy Castle
         * uses ASN.1 primitive type classes for this instead of
         * ASN1TaggedObject.
         *
         * All other values are impossible, as tag class is stored in 2 bits,
         * so there are only 4 possibilities. Unless Bouncy Castle does
         * something very wrong...
         */
        if (obj.getTagClass() == BERTags.APPLICATION) {
            sb.append(" APPLICATION");
        } else if (obj.getTagClass() == BERTags.PRIVATE) {
            sb.append(" PRIVATE");
        }
        // isExplicit can produce false-positives (i.e. show EXPLICIT, when it
        // was IMPLICIT), if the object was parsed and no turnInto* functions
        // were called
        if (obj.isExplicit()) {
            sb.append(" EXPLICIT");
        } else {
            sb.append(" IMPLICIT");
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsn1DisplayValue() {
        return baseObjectNode.getAsn1DisplayValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ASN1TaggedObject getAsn1Primitive() {
        return (ASN1TaggedObject) super.getAsn1Primitive();
    }

    /**
     * Replaces {@code oldObj} in the base object sequence with {@code newObj}.
     * Automatically reloads the tree.
     *
     * @param oldObj Base object to replace.
     * @param newObj Base object replacement.
     *
     * @return {@code true}, if replacement happened, {@code false} otherwise.
     */
    public boolean replace(ASN1TaggedObject oldObj, ASN1TaggedObject newObj) {
        final ASN1TaggedObject newRootObj = replaceInternal(getAsn1Primitive(), oldObj, newObj);
        if (newRootObj == null) {
            return false;
        }
        replace(newRootObj);
        return true;
    }

    /**
     * Replaces the underlying ASN.1 tagged object with {@code newObj}.
     * Automatically reloads the tree.
     *
     * @param newObj Replacement ASN.1 tagged object.
     */
    public void replace(ASN1TaggedObject newObj) {
        setUserObject(newObj);
        reload();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reload() {
        super.reload();
        /*
         * To show tagging, instead of making a child, we will flatten it and
         * "consume" the appropriate base object node.
         */
        baseObjectNode = Asn1TreeNodeFactory.fromPrimitive(
                getAsn1Primitive().getBaseObject().toASN1Primitive()
        );
        icon = baseObjectNode.getIcon();
        setRfcFieldName(baseObjectNode.getRfcFieldName());
        setValueExplanation(baseObjectNode.getValueExplanation());
        removeAllChildren();
        // Cannot use Enumeration here, as `add` removes the node from the old parent...
        while (baseObjectNode.getChildCount() > 0) {
            add(baseObjectNode.getChildAt(0));
        }
    }

    /**
     * Returns a new {@code root} object, where {@code oldObj} was replaced
     * with {@code newObj}. If {@code oldObj} was not present in the
     * {@code root} object base object chain, returns null.
     *
     * @param root   Object for which the replacement will be built.
     * @param oldObj Base object to replace.
     * @param newObj Base object replacement.
     *
     * @return A new {@code root} object, where {@code oldObj} was replaced
     * with {@code newObj}. If {@code oldObj} was not present in the
     * {@code root} object base object chain, returns null.
     */
    private static ASN1TaggedObject replaceInternal(
            ASN1TaggedObject root,
            ASN1TaggedObject oldObj,
            ASN1TaggedObject newObj
    ) {
        /*
         * Object reference comparison here is intended, as we expect oldObj
         * to be taken directly from root, so there is no need to waste
         * performance with equals.
         */
        if (root == oldObj) {
            return newObj;
        }
        final ASN1Primitive baseObj = root.getBaseObject().toASN1Primitive();
        if (!(baseObj instanceof ASN1TaggedObject)) {
            return null;
        }
        return new DERTaggedObject(
                root.isExplicit(),
                root.getTagClass(),
                root.getTagNo(),
                replaceInternal((ASN1TaggedObject) baseObj, oldObj, newObj)
        );
    }
}
