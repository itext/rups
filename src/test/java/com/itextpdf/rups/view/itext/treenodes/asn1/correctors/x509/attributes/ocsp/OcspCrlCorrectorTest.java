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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class OcspCrlCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        OcspCrlCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "crlID");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        OcspCrlCorrector.INSTANCE.correct(node, "crls");
        validateDefaultNode(node, "crls");
    }

    @Test
    void correct_EmptySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        OcspCrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "crlID", node);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        OcspCrlCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidUniversalFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERIA5String("https://example.com/"),
                        new ASN1Integer(1),
                        new ASN1GeneralizedTime("202405011337"),
                })
        );
        OcspCrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "crlID", node);
        Asn1TestUtil.assertNodeMatches(0, "IA5String: https://example.com/", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "GeneralizedTime: 202405011337 (2024-05-01T13:37:00Z)", node.getChildAt(2));
    }

    @Test
    void correct_InvalidUnderlyingTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERPrintableString("https://example.com/")),
                        new DERTaggedObject(true, 1, new ASN1Enumerated(1)),
                        new DERTaggedObject(true, 2, new ASN1UTCTime("240501133700")),
                })
        );
        OcspCrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "crlID", node);
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT PrintableString: https://example.com/", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT ENUMERATED: 1", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT UTCTime: 240501133700 (2024-05-01T13:37:00Z)",
                node.getChildAt(2));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(3, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "crlUrl: https://example.com/", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "crlNum: 1", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "crlTime: 202405011337 (2024-05-01T13:37:00Z)", node.getChildAt(2));
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERIA5String("https://example.com/")),
                        new DERTaggedObject(true, 1, new ASN1Integer(1)),
                        new DERTaggedObject(true, 2, new ASN1GeneralizedTime("202405011337")),
                })
        );
    }
}
