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

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERT61String;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERUniversalString;
import org.bouncycastle.asn1.DERVideotexString;
import org.bouncycastle.asn1.DERVisibleString;
import org.bouncycastle.asn1.DLSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class Asn1TreeNodeFactoryTest {
    @Test
    void fromPrimitive_JavaNull() {
        Assertions.assertNull(Asn1TreeNodeFactory.fromPrimitive((ASN1Primitive) null));
    }

    @Test
    void fromPrimitive_BitString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERBitString((byte) 0b10101000, 3)
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1BitStringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "BIT STRING: 0b10101", node);
    }

    @Test
    void fromPrimitive_BmpString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERBMPString("AZ")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1BmpStringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "BMPString: AZ", node);
    }

    @Test
    void fromPrimitive_Boolean() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                ASN1Boolean.getInstance(false)
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1BooleanTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "BOOLEAN: FALSE", node);
    }

    @Test
    void fromPrimitive_Enumerated() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Enumerated(42)
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1EnumeratedTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 42", node);
    }

    @Test
    void fromPrimitive_GeneralizedTime() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1GeneralizedTime("202405011337")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1GeneralizedTimeTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "GeneralizedTime: 202405011337 (2024-05-01T13:37:00Z)", node);
    }

    @Test
    void fromPrimitive_Ia5String() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERIA5String("AZ")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1Ia5StringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "IA5String: AZ", node);
    }

    @Test
    void fromPrimitive_Integer() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1IntegerTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node);
    }

    @Test
    void fromPrimitive_Null() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                DERNull.INSTANCE
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1NullTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node);
    }

    @Test
    void fromPrimitive_ObjectIdentifier() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1ObjectIdentifier("1.2")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1ObjectIdentifierTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "OBJECT IDENTIFIER: 1.2", node);
    }

    @Test
    void fromPrimitive_OctetString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DEROctetString(new byte[] {0x01, 0x02})
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1OctetStringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "OCTET STRING: 0x0102", node);
    }

    @Test
    void fromPrimitive_PrintableString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERPrintableString("AZ")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1PrintableStringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: AZ", node);
    }

    @Test
    void fromPrimitive_Sequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        DERNull.INSTANCE,
                        new DERPrintableString("AZ"),
                })
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1SequenceTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(2, "SEQUENCE", node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: AZ", node.getChildAt(1));
    }

    @Test
    void fromPrimitive_Set() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DLSet(new ASN1Encodable[] {
                        DERNull.INSTANCE,
                        new DERPrintableString("AZ"),
                })
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1SetTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(2, "SET", node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: AZ", node.getChildAt(1));
    }

    @Test
    void fromPrimitive_TaggedObject() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(
                        false,
                        1,
                        new DERSequence(new ASN1Encodable[] {
                                DERNull.INSTANCE,
                                new DERPrintableString("AZ"),
                        })
                )
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1TaggedObjectTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(2, "[1] IMPLICIT SEQUENCE", node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: AZ", node.getChildAt(1));
    }

    @Test
    void fromPrimitive_TeletexString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERT61String("AZ")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1TeletexStringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "TeletexString: AZ", node);
    }

    @Test
    void fromPrimitive_UniversalString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERUniversalString(new byte[0])
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1UniversalStringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "UniversalString: #1C00", node);
    }

    @Test
    void fromPrimitive_UtcTime() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1UTCTime("240501133700")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1UtcTimeTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "UTCTime: 240501133700 (2024-05-01T13:37:00Z)", node);
    }

    @Test
    void fromPrimitive_Utf8String() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERUTF8String("AZ")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1Utf8StringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "UTF8String: AZ", node);
    }

    @Test
    void fromPrimitive_VisibleString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERVisibleString("AZ")
        );
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1VisibleStringTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "VisibleString: AZ", node);
    }

    @Test
    void fromPrimitive_UnsupportedType() {
        final ASN1Primitive obj = new DERVideotexString(new byte[] {0x01});
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(AbstractAsn1TreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(0, "UNKNOWN: " + obj, node);
    }

    @Test
    void fromPrimitive_ValidBytes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new byte[] {
                0x31,               // Tag: SET
                0x02,               // Value length: 2
                0x05,               //   Tag: NULL
                0x00,               //   Value length: 0
        });
        Assertions.assertNotNull(node);
        Assertions.assertInstanceOf(Asn1SetTreeNode.class, node);
        Asn1TestUtil.assertNodeMatches(1, "SET", node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(0));
    }

    @Test
    void fromPrimitive_InvalidBytes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new byte[] {
                0x31,               // Tag: SET
                0x03,               // Value length: 3 (INVALID)
                0x05,               //   Tag: NULL
                0x00,               //   Value length: 0
        });
        Assertions.assertNull(node);
    }
}