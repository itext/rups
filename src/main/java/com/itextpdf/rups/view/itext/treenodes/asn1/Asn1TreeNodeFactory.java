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
package com.itextpdf.rups.view.itext.treenodes.asn1;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1T61String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.ASN1UniversalString;
import org.bouncycastle.asn1.ASN1VisibleString;

/**
 * Static factory for building ASN.1 tree nodes.
 */
public final class Asn1TreeNodeFactory {
    private Asn1TreeNodeFactory() {
        // static class
    }

    /**
     * Creates a tree node for the provided ASN.1 primitive.
     *
     * @param object ASN.1 primitive to create a node for.
     *
     * @return Returns a new tree node, which holds the provided object.
     */
    public static AbstractAsn1TreeNode fromPrimitive(ASN1Primitive object) {
        if (object == null) {
            return null;
        }
        if (object instanceof ASN1TaggedObject) {
            return new Asn1TaggedObjectTreeNode((ASN1TaggedObject) object);
        }
        if (object instanceof ASN1Boolean) {
            return new Asn1BooleanTreeNode((ASN1Boolean) object);
        }
        if (object instanceof ASN1Integer) {
            return new Asn1IntegerTreeNode((ASN1Integer) object);
        }
        if (object instanceof ASN1BitString) {
            return new Asn1BitStringTreeNode((ASN1BitString) object);
        }
        if (object instanceof ASN1OctetString) {
            return new Asn1OctetStringTreeNode((ASN1OctetString) object);
        }
        if (object instanceof ASN1Null) {
            return new Asn1NullTreeNode((ASN1Null) object);
        }
        if (object instanceof ASN1ObjectIdentifier) {
            return new Asn1ObjectIdentifierTreeNode((ASN1ObjectIdentifier) object);
        }
        if (object instanceof ASN1Enumerated) {
            return new Asn1EnumeratedTreeNode((ASN1Enumerated) object);
        }
        if (object instanceof ASN1UTF8String) {
            return new Asn1Utf8StringTreeNode((ASN1UTF8String) object);
        }
        if (object instanceof ASN1Sequence) {
            return new Asn1SequenceTreeNode((ASN1Sequence) object);
        }
        if (object instanceof ASN1Set) {
            return new Asn1SetTreeNode((ASN1Set) object);
        }
        if (object instanceof ASN1PrintableString) {
            return new Asn1PrintableStringTreeNode((ASN1PrintableString) object);
        }
        if (object instanceof ASN1T61String) {
            return new Asn1TeletexStringTreeNode((ASN1T61String) object);
        }
        if (object instanceof ASN1IA5String) {
            return new Asn1Ia5StringTreeNode((ASN1IA5String) object);
        }
        if (object instanceof ASN1UTCTime) {
            return new Asn1UtcTimeTreeNode((ASN1UTCTime) object);
        }
        if (object instanceof ASN1GeneralizedTime) {
            return new Asn1GeneralizedTimeTreeNode((ASN1GeneralizedTime) object);
        }
        if (object instanceof ASN1VisibleString) {
            return new Asn1VisibleStringTreeNode((ASN1VisibleString) object);
        }
        if (object instanceof ASN1UniversalString) {
            return new Asn1UniversalStringTreeNode((ASN1UniversalString) object);
        }
        if (object instanceof ASN1BMPString) {
            return new Asn1BmpStringTreeNode((ASN1BMPString) object);
        }
        // In case we haven't covered some type, use this class as default
        return new Asn1UnknownTreeNode(object);
    }

    /**
     * Creates a tree node for the encoded ASN.1 primitive.
     *
     * @param object BER/DER-encoded ASN.1 primitive to create a node for.
     *
     * @return Returns a new tree node, which holds the provided object. Null,
     * if bytes do not represent an ASN.1 primitive.
     */
    public static AbstractAsn1TreeNode fromPrimitive(byte[] object) {
        try {
            /*
             * Because of padding we cannot use ASN1Primitive::fromByteArray, as that one
             * expects there be no leftover data after the object.
             */
            try (final ASN1InputStream stream = new ASN1InputStream(object)) {
                return fromPrimitive(stream.readObject());
            }
        } catch (IOException e) {
            return null;
        }
    }
}
