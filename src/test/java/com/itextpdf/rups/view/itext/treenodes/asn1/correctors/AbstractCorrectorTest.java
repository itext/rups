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
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1IntegerTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.correctPrimitiveUniversalType;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.correctUniversalObjectIdentifier;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.fixImplicitContextSpecificObject;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.getBaseObject;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.getBaseObjectUnchecked;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.hasFlag;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.isExplicitContextSpecificType;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.isImplicitContextSpecificType;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.isNumberInRange;
import static com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector.isUniversalType;

class AbstractCorrectorTest {
    @Test
    void isUniversalType_Universal() throws IOException {
        for (final ASN1Primitive obj : createTestUniversalTypeObjects()) {
            Assertions.assertTrue(isUniversalType(obj));
            final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
            Assertions.assertTrue(isUniversalType(node));
        }
    }

    @Test
    void isUniversalType_Tagged() throws IOException {
        for (int i = 0b01; i <= 0b11; ++i) {
            final byte tag = (byte) ((i << 6) | BERTags.CONSTRUCTED | 1);
            final ASN1Primitive obj = ASN1Primitive.fromByteArray(new byte[] {tag, 0});
            Assertions.assertFalse(isUniversalType(obj));
            final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
            Assertions.assertFalse(isUniversalType(node));
        }
    }

    @Test
    void isImplicitContextSpecificType_Universal() throws IOException {
        for (final ASN1Primitive obj : createTestUniversalTypeObjects()) {
            final int tagNo = obj.getEncoded()[0] & 0x1F;
            Assertions.assertFalse(isImplicitContextSpecificType(obj, tagNo));
            final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
            Assertions.assertFalse(isImplicitContextSpecificType(node, tagNo));
        }
    }

    @Test
    void isImplicitContextSpecificType_Explicit() {
        /*
         * They should still return true with the valid tagNo, as Bouncy
         * Castle can parse IMPLICIT as EXPLICIT.
         */
        final ASN1Primitive obj = new DERTaggedObject(true, 0, new ASN1Integer(1));
        Assertions.assertTrue(isImplicitContextSpecificType(obj, 0));
        Assertions.assertFalse(isImplicitContextSpecificType(obj, 1));
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
        Assertions.assertTrue(isImplicitContextSpecificType(node, 0));
        Assertions.assertFalse(isImplicitContextSpecificType(node, 1));
    }

    @Test
    void isImplicitContextSpecificType_Implicit() {
        final ASN1Primitive obj = new DERTaggedObject(false, 0, new ASN1Integer(1));
        Assertions.assertTrue(isImplicitContextSpecificType(obj, 0));
        Assertions.assertFalse(isImplicitContextSpecificType(obj, 1));
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
        Assertions.assertTrue(isImplicitContextSpecificType(node, 0));
        Assertions.assertFalse(isImplicitContextSpecificType(node, 1));
    }

    @Test
    void isImplicitContextSpecificType_OtherTagClasses() {
        for (final int tagClass : Arrays.asList(BERTags.APPLICATION, BERTags.PRIVATE)) {
            final ASN1Primitive obj = new DERTaggedObject(false, tagClass, 0, new ASN1Integer(1));
            Assertions.assertFalse(isImplicitContextSpecificType(obj, 0));
            final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
            Assertions.assertFalse(isImplicitContextSpecificType(node, 0));
        }
    }

    @Test
    void isExplicitContextSpecificType_Universal() throws IOException {
        for (final ASN1Primitive obj : createTestUniversalTypeObjects()) {
            final int tagNo = obj.getEncoded()[0] & 0x1F;
            Assertions.assertFalse(isExplicitContextSpecificType(obj, tagNo));
            final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
            Assertions.assertFalse(isExplicitContextSpecificType(node, tagNo));
        }
    }

    @Test
    void isExplicitContextSpecificType_Explicit() {
        final ASN1Primitive obj = new DERTaggedObject(true, 0, new ASN1Integer(1));
        Assertions.assertTrue(isExplicitContextSpecificType(obj, 0));
        Assertions.assertFalse(isExplicitContextSpecificType(obj, 1));
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
        Assertions.assertTrue(isExplicitContextSpecificType(node, 0));
        Assertions.assertFalse(isExplicitContextSpecificType(node, 1));
    }

    @Test
    void isExplicitContextSpecificType_Implicit() {
        final ASN1Primitive obj = new DERTaggedObject(false, 0, new ASN1Integer(1));
        Assertions.assertFalse(isExplicitContextSpecificType(obj, 0));
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
        Assertions.assertFalse(isExplicitContextSpecificType(node, 0));
    }

    @Test
    void isExplicitContextSpecificType_OtherTagClasses() {
        for (final int tagClass : Arrays.asList(BERTags.APPLICATION, BERTags.PRIVATE)) {
            final ASN1Primitive obj = new DERTaggedObject(true, tagClass, 0, new ASN1Integer(1));
            Assertions.assertFalse(isExplicitContextSpecificType(obj, 0));
            final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
            Assertions.assertFalse(isExplicitContextSpecificType(node, 0));
        }
    }

    @Test
    void isExplicitContextSpecificType_TypeChecking() {
        final ASN1Primitive obj = new DERTaggedObject(true, 1, new ASN1Integer(42));
        Assertions.assertTrue(isExplicitContextSpecificType(obj, 1, ASN1Integer.class));
        Assertions.assertFalse(isExplicitContextSpecificType(obj, 1, ASN1Enumerated.class));
        Assertions.assertFalse(isExplicitContextSpecificType(obj, 1, ASN1TaggedObject.class));
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(obj);
        Assertions.assertTrue(isExplicitContextSpecificType(node, 1, ASN1Integer.class));
        Assertions.assertFalse(isExplicitContextSpecificType(node, 1, ASN1Enumerated.class));
        Assertions.assertFalse(isExplicitContextSpecificType(node, 1, ASN1TaggedObject.class));
    }

    @Test
    void getBaseObjectUnchecked_Assertions() {
        final ASN1Integer obj = new ASN1Integer(1);
        Assertions.assertThrows(AssertionError.class, () -> getBaseObjectUnchecked(obj));
        final Asn1IntegerTreeNode node = new Asn1IntegerTreeNode(obj);
        Assertions.assertThrows(AssertionError.class, () -> getBaseObjectUnchecked(node));
    }

    @Test
    void correctPrimitiveUniversalType_Universal() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new ASN1Enumerated(2));
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 2", node);
        correctPrimitiveUniversalType(node, ASN1Integer.class, "value");
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 2", node);
        correctPrimitiveUniversalType(node, ASN1Enumerated.class, "value");
        Asn1TestUtil.assertNodeMatches(0, "value: 2", node);
    }

    @Test
    void correctPrimitiveUniversalType_Tagged() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 1, new ASN1Enumerated(2))
        );
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT ENUMERATED: 2", node);
        correctPrimitiveUniversalType(node, ASN1Integer.class, "value");
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT ENUMERATED: 2", node);
        correctPrimitiveUniversalType(node, ASN1Enumerated.class, "value");
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT ENUMERATED: 2", node);
        correctPrimitiveUniversalType(node, getBaseObjectUnchecked(node), ASN1Enumerated.class, "value");
        Asn1TestUtil.assertNodeMatches(0, "value: 2", node);
    }

    @Test
    void correctUniversalObjectIdentifier_UniversalOid() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1ObjectIdentifier("1.2")
        );
        Asn1TestUtil.assertNodeMatches(0, "OBJECT IDENTIFIER: 1.2 (/iso/member-body)", node);
        Assertions.assertEquals("1.2", correctUniversalObjectIdentifier(node, "type"));
        Asn1TestUtil.assertNodeMatches(0, "type: 1.2 (/iso/member-body)", node);
    }

    @Test
    void correctUniversalObjectIdentifier_UniversalNonOid() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERPrintableString("1.2")
        );
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: 1.2", node);
        Assertions.assertNull(correctUniversalObjectIdentifier(node, "type"));
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: 1.2", node);
    }

    @Test
    void correctUniversalObjectIdentifier_TaggedOid() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 2, new ASN1ObjectIdentifier("1.2"))
        );
        Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT OBJECT IDENTIFIER: 1.2 (/iso/member-body)", node);
        Assertions.assertNull(correctUniversalObjectIdentifier(node, "type"));
        Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT OBJECT IDENTIFIER: 1.2 (/iso/member-body)", node);
    }

    @Test
    void fixImplicitContextSpecificObject_PrimitiveValid() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | 7), 1, 0x35
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(0, "[7] IMPLICIT OCTET STRING: 0x35", node);
        Assertions.assertNotSame(obj, fixImplicitContextSpecificObject(node, ASN1PrintableString::getInstance));
        Asn1TestUtil.assertNodeMatches(0, "[7] IMPLICIT PrintableString: 5", node);
    }

    @Test
    void fixImplicitContextSpecificObject_PrimitiveInvalid() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | 7), 2, 0x30, 0x00
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(0, "[7] IMPLICIT OCTET STRING: 0x3000", node);
        Assertions.assertSame(obj, fixImplicitContextSpecificObject(node, ASN1Sequence::getInstance));
        Asn1TestUtil.assertNodeMatches(0, "[7] IMPLICIT OCTET STRING: 0x3000", node);
    }

    @Test
    void fixImplicitContextSpecificObject_ConstructedSingleValid() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | BERTags.CONSTRUCTED | 1), 2,
                (BERTags.CONSTRUCTED | BERTags.SEQUENCE), 0
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SEQUENCE", node);
        Assertions.assertNotSame(obj, fixImplicitContextSpecificObject(node, ASN1Set::getInstance));
        Asn1TestUtil.assertNodeMatches(1, "[1] IMPLICIT SET", node);
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node.getChildAt(0));
    }

    @Test
    void fixImplicitContextSpecificObject_ConstructedSingleInvalid() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | BERTags.CONSTRUCTED | 1), 2,
                (BERTags.CONSTRUCTED | BERTags.SEQUENCE), 0
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SEQUENCE", node);
        Assertions.assertSame(obj, fixImplicitContextSpecificObject(node, ASN1OctetString::getInstance));
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SEQUENCE", node);
    }

    @Test
    void fixImplicitContextSpecificObject_ConstructedMultiValid() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | BERTags.CONSTRUCTED | 1), 6,
                BERTags.BOOLEAN, 1, 0,
                BERTags.INTEGER, 1, 42,
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(2, "[1] IMPLICIT SEQUENCE", node);
        Asn1TestUtil.assertNodeMatches(0, "BOOLEAN: FALSE", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(1));
        Assertions.assertNotSame(obj, fixImplicitContextSpecificObject(node, ASN1Set::getInstance));
        Asn1TestUtil.assertNodeMatches(2, "[1] IMPLICIT SET", node);
        Asn1TestUtil.assertNodeMatches(0, "BOOLEAN: FALSE", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(1));
    }

    @Test
    void fixImplicitContextSpecificObject_ConstructedMultiInvalid() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | BERTags.CONSTRUCTED | 1), 6,
                BERTags.BOOLEAN, 1, 0,
                BERTags.INTEGER, 1, 42,
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(2, "[1] IMPLICIT SEQUENCE", node);
        Asn1TestUtil.assertNodeMatches(0, "BOOLEAN: FALSE", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(1));
        Assertions.assertSame(obj, fixImplicitContextSpecificObject(node, ASN1OctetString::getInstance));
        Asn1TestUtil.assertNodeMatches(2, "[1] IMPLICIT SEQUENCE", node);
        Asn1TestUtil.assertNodeMatches(0, "BOOLEAN: FALSE", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(1));
    }

    @Test
    void fixImplicitContextSpecificObject_AlreadyCorrect() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | 3), 2, 0x30, 0x31
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(0, "[3] IMPLICIT OCTET STRING: 0x3031", node);
        Assertions.assertSame(obj, fixImplicitContextSpecificObject(node, ASN1OctetString::getInstance));
        Asn1TestUtil.assertNodeMatches(0, "[3] IMPLICIT OCTET STRING: 0x3031", node);
    }

    @Test
    void fixImplicitContextSpecificObject_NestedTags() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | BERTags.CONSTRUCTED | 1), 7,
                (byte) (BERTags.CONTEXT_SPECIFIC | BERTags.CONSTRUCTED | 2), 5,
                // We assume this should be an IMPLICIT SET
                (byte) (BERTags.CONTEXT_SPECIFIC | BERTags.CONSTRUCTED | 3), 3,
                (byte) (BERTags.CONTEXT_SPECIFIC | 4), 1, 0x2A
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(
                0, "[1] EXPLICIT [2] EXPLICIT [3] EXPLICIT [4] IMPLICIT OCTET STRING: 0x2A", node
        );
        final ASN1TaggedObject modObj = (ASN1TaggedObject) getBaseObject((ASN1TaggedObject) getBaseObject(obj));
        Assertions.assertNotSame(modObj, fixImplicitContextSpecificObject(node, modObj, ASN1Set::getInstance));
        Asn1TestUtil.assertNodeMatches(
                1, "[1] EXPLICIT [2] EXPLICIT [3] IMPLICIT SET", node
        );
        Asn1TestUtil.assertNodeMatches(0, "[4] IMPLICIT OCTET STRING: 0x2A", node.getChildAt(0));
    }

    @Test
    void fixImplicitContextSpecificObject_Assertions() {
        final ASN1TaggedObject obj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | 1), 0
        });
        final Asn1TaggedObjectTreeNode node = new Asn1TaggedObjectTreeNode(obj);
        final ASN1TaggedObject otherObj = ASN1TaggedObject.getInstance(new byte[] {
                (byte) (BERTags.CONTEXT_SPECIFIC | 1), 1, 0x30
        });
        Assertions.assertThrows(
                AssertionError.class,
                () -> fixImplicitContextSpecificObject(node, otherObj, ASN1PrintableString::getInstance)
        );
    }

    @Test
    void hasFlag_Regular() {
        final byte[] bitString = {(byte) 0b10101010, (byte) 0b10101010};
        for (int i = 0; i < 16; i += 2) {
            Assertions.assertTrue(hasFlag(bitString, i));
            Assertions.assertFalse(hasFlag(bitString, i + 1));
        }
    }

    @Test
    void hasFlag_Assertions() {
        Assertions.assertThrows(AssertionError.class, () -> hasFlag(null, 0));
        Assertions.assertThrows(AssertionError.class, () -> hasFlag(new byte[] {1}, -1));
    }

    @Test
    void isNumberInRange_Regular() {
        Assertions.assertFalse(isNumberInRange(BigInteger.valueOf(-1), 10));
        for (int i = 0; i < 10; ++i) {
            Assertions.assertTrue(isNumberInRange(BigInteger.valueOf(i), 10));
        }
        Assertions.assertFalse(isNumberInRange(BigInteger.valueOf(11), 10));
    }

    @Test
    void isNumberInRange_TooBigForInt() {
        final BigInteger biggerThanInt = new BigInteger(1, new byte[] {(byte) 0x80, 0, 0, 0});
        Assertions.assertFalse(isNumberInRange(biggerThanInt, Integer.MAX_VALUE));
    }

    @Test
    void isNumberInRange_Assertions() {
        Assertions.assertThrows(AssertionError.class, () -> isNumberInRange(null, 10));
        Assertions.assertThrows(AssertionError.class, () -> isNumberInRange(BigInteger.ONE, 0));
    }

    private static List<ASN1Primitive> createTestUniversalTypeObjects() throws IOException {
        final List<ASN1Primitive> objects = new ArrayList<>();
        for (byte i = 1; i < 0b00011111; ++i) {
            // Not relevant or not supported by Bouncy Castle
            if (i == 8 || i == 9 || i == 11 || i == 14 || i == 15 || i == 29) {
                continue;
            }
            final byte[] data;
            switch (i) {
                case BERTags.BOOLEAN:
                    data = new byte[] {i, 1, 0};
                    break;
                case BERTags.INTEGER:
                case BERTags.ENUMERATED:
                case BERTags.UTC_TIME:
                case BERTags.GENERALIZED_TIME:
                    data = new byte[] {i, 4, 0x32, 0x32, 0x32, 0x32};
                    break;
                case BERTags.BIT_STRING:
                    data = new byte[] {i, 2, 0, 0};
                    break;
                case BERTags.SEQUENCE:
                case BERTags.SET:
                    data = new byte[] {(byte) (i | BERTags.CONSTRUCTED), 0};
                    break;
                default:
                    data = new byte[] {i, 0};
            }
            objects.add(ASN1Primitive.fromByteArray(data));
        }
        return objects;
    }
}