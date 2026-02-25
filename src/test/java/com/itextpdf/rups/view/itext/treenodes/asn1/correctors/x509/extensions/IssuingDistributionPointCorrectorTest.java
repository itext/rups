/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class IssuingDistributionPointCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        IssuingDistributionPointCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "issuingDistributionPoint");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        IssuingDistributionPointCorrector.INSTANCE.correct(node, "idp");
        validateDefaultNode(node, "idp");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new ASN1Integer(15));
        IssuingDistributionPointCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 15", node);
    }

    @Test
    void correct_EmptyRootSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new DERSequence());
        IssuingDistributionPointCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "issuingDistributionPoint", node);
    }

    @Test
    void correct_OnlyLastField() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERTaggedObject(false, 5, ASN1Boolean.TRUE)
                )
        );
        IssuingDistributionPointCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "issuingDistributionPoint", node);
        Asn1TestUtil.assertNodeMatches(0, "onlyContainsAttributeCerts: TRUE", node.getChildAt(0));
    }

    @Test
    void correct_SingleUnexpectedField() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(ASN1Boolean.FALSE)
        );
        IssuingDistributionPointCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "issuingDistributionPoint", node);
        Asn1TestUtil.assertNodeMatches(0, "BOOLEAN: FALSE", node.getChildAt(0));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(6, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode distributionPoint = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "distributionPoint", distributionPoint);
            Asn1TestUtil.assertNodeMatches(
                    0, "uniformResourceIdentifier: https://example.com/", distributionPoint.getChildAt(0)
            );
        }
        Asn1TestUtil.assertNodeMatches(0, "onlyContainsUserCerts: FALSE", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "onlyContainsCACerts: TRUE", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode onlySomeReasons = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(9, "onlySomeReasons: 0b101010101", onlySomeReasons);
            Asn1TestUtil.assertNodeMatches(0, "unused: TRUE", onlySomeReasons.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "keyCompromise: FALSE", onlySomeReasons.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "caCompromise: TRUE", onlySomeReasons.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "affiliationChanged: FALSE", onlySomeReasons.getChildAt(3));
            Asn1TestUtil.assertNodeMatches(0, "superseded: TRUE", onlySomeReasons.getChildAt(4));
            Asn1TestUtil.assertNodeMatches(0, "cessationOfOperation: FALSE", onlySomeReasons.getChildAt(5));
            Asn1TestUtil.assertNodeMatches(0, "certificateHold: TRUE", onlySomeReasons.getChildAt(6));
            Asn1TestUtil.assertNodeMatches(0, "privilegeWithdrawn: FALSE", onlySomeReasons.getChildAt(7));
            Asn1TestUtil.assertNodeMatches(0, "aaCompromise: TRUE", onlySomeReasons.getChildAt(8));
        }
        Asn1TestUtil.assertNodeMatches(0, "indirectCRL: FALSE", node.getChildAt(4));
        Asn1TestUtil.assertNodeMatches(0, "onlyContainsAttributeCerts: TRUE", node.getChildAt(5));
    }

    private static AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0,
                                new DERTaggedObject(false, 0, new DERSequence(
                                        new DERTaggedObject(false, 6, new DERIA5String(
                                                "https://example.com/"
                                        ))
                                ))
                        ),
                        new DERTaggedObject(false, 1, ASN1Boolean.FALSE),
                        new DERTaggedObject(false, 2, ASN1Boolean.TRUE),
                        new DERTaggedObject(false, 3, new DERBitString(
                                new byte[] {(byte) 0xAA, (byte) 0x80}, 7
                        )),
                        new DERTaggedObject(false, 4, ASN1Boolean.FALSE),
                        new DERTaggedObject(false, 5, ASN1Boolean.TRUE),
                })
        );
    }
}
