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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class CrlDistributionPointsCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "crlDistributionPoints");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CrlDistributionPointsCorrector.INSTANCE.correct(node, "cdp");
        validateDefaultNode(node, "cdp");
    }

    @Test
    void correct_UsingRelativeDistinguishedName() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 0,
                                        new DERTaggedObject(false, 1,
                                                new DERSet(
                                                        new DERSequence()
                                                )
                                        )
                                )
                        )
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "distributionPoint", dp);
            {
                final AbstractAsn1TreeNode dpn = dp.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "distributionPointName", dpn);
                Asn1TestUtil.assertNodeMatches(0, "attributeTypeAndValue", dpn.getChildAt(0));
            }
        }
    }

    @Test
    void correct_UniversalRelativeDistinguishedName() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 0,
                                        new DERSet()
                                )
                        )
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "distributionPoint", dp);
            Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SET", dp.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidRelativeDistinguishedNameLeafObjectType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 0,
                                        new DERTaggedObject(false, 1, new DERSequence())
                                )
                        )
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "distributionPoint", dp);
            Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT [1] IMPLICIT SEQUENCE", dp.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidGeneralNamesLeafObjectType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 0,
                                        new DERTaggedObject(false, 0, new DERSet())
                                )
                        )
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "distributionPoint", dp);
            Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT [0] IMPLICIT SET", dp.getChildAt(0));
        }
    }

    @Test
    void correct_EmptyRootSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "crlDistributionPoints", node);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                DERNull.INSTANCE
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node);
    }

    @Test
    void correct_InvalidSequenceEntry() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        DERNull.INSTANCE
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(0));
    }

    @Test
    void correct_EmptyDistributionPointSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence()
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        Asn1TestUtil.assertNodeMatches(0, "distributionPoint", node.getChildAt(0));
    }

    @Test
    void correct_InvalidDistributionPointSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERSequence(),
                                new DERBitString((byte) 0x80, 7),
                                new DERSequence(),
                        })
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "distributionPoint", dp);
            Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", dp.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "BIT STRING: 0b1", dp.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", dp.getChildAt(2));
        }
    }

    @Test
    void correct_ShortReasonFlags() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(false, 1, new DERBitString((byte) 0x80, 7)),
                        })
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "distributionPoint", dp);
            {
                final AbstractAsn1TreeNode reasons = dp.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(9, "reasons: 0b1", reasons);
                Asn1TestUtil.assertNodeMatches(0, "unused: TRUE", reasons.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "keyCompromise: FALSE", reasons.getChildAt(1));
                Asn1TestUtil.assertNodeMatches(0, "caCompromise: FALSE", reasons.getChildAt(2));
                Asn1TestUtil.assertNodeMatches(0, "affiliationChanged: FALSE", reasons.getChildAt(3));
                Asn1TestUtil.assertNodeMatches(0, "superseded: FALSE", reasons.getChildAt(4));
                Asn1TestUtil.assertNodeMatches(0, "cessationOfOperation: FALSE", reasons.getChildAt(5));
                Asn1TestUtil.assertNodeMatches(0, "certificateHold: FALSE", reasons.getChildAt(6));
                Asn1TestUtil.assertNodeMatches(0, "privilegeWithdrawn: FALSE", reasons.getChildAt(7));
                Asn1TestUtil.assertNodeMatches(0, "aaCompromise: FALSE", reasons.getChildAt(8));
            }
        }
    }

    @Test
    void correct_InvalidReasonFlagsType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(false, 1, new DEROctetString(new byte[] {1})),
                        })
                )
        );
        CrlDistributionPointsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crlDistributionPoints", node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "distributionPoint", dp);
            {
                final AbstractAsn1TreeNode reasons = dp.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT OCTET STRING: 0x01", reasons);
            }
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(1, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode dp = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "distributionPoint", dp);
            {
                final AbstractAsn1TreeNode dpn = dp.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "distributionPointName", dpn);
                Asn1TestUtil.assertNodeMatches(0, "dnsName: example.com", dpn.getChildAt(0));
            }
            {
                final AbstractAsn1TreeNode reasons = dp.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(9, "reasons: 0b101010101", reasons);
                Asn1TestUtil.assertNodeMatches(0, "unused: TRUE", reasons.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "keyCompromise: FALSE", reasons.getChildAt(1));
                Asn1TestUtil.assertNodeMatches(0, "caCompromise: TRUE", reasons.getChildAt(2));
                Asn1TestUtil.assertNodeMatches(0, "affiliationChanged: FALSE", reasons.getChildAt(3));
                Asn1TestUtil.assertNodeMatches(0, "superseded: TRUE", reasons.getChildAt(4));
                Asn1TestUtil.assertNodeMatches(0, "cessationOfOperation: FALSE", reasons.getChildAt(5));
                Asn1TestUtil.assertNodeMatches(0, "certificateHold: TRUE", reasons.getChildAt(6));
                Asn1TestUtil.assertNodeMatches(0, "privilegeWithdrawn: FALSE", reasons.getChildAt(7));
                Asn1TestUtil.assertNodeMatches(0, "aaCompromise: TRUE", reasons.getChildAt(8));
            }
            Asn1TestUtil.assertNodeMatches(0, "crlIssuer", dp.getChildAt(2));
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 0,
                                        new DERTaggedObject(false, 0, new DERSequence(
                                                new DERTaggedObject(false, 2, new DERIA5String("example.com"))
                                        ))
                                ),
                                new DERTaggedObject(false, 1,
                                        new DERBitString(new byte[] {(byte) 0b10101010, (byte) 0b10000000}, 7)
                                ),
                                new DERTaggedObject(false, 2,
                                        new DERSequence()
                                ),
                        })
                )
        );
    }
}