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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class UbiquityRightsCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        UbiquityRightsCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "ubiquityRights");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        UbiquityRightsCorrector.INSTANCE.correct(node, "UR");
        validateDefaultNode(node, "UR");
    }

    @Test
    void correct_EmptySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        UbiquityRightsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "ubiquityRights", node);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        UbiquityRightsCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidSequenceTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERPrintableString("test"),
                        ASN1Boolean.FALSE,
                        new ASN1Integer(42),
                })
        );
        UbiquityRightsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "ubiquityRights", node);
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: test", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "BOOLEAN: FALSE", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(2));
    }

    @Test
    void correct_InvalidVersionNumber() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new ASN1Integer(21)
                )
        );
        UbiquityRightsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "ubiquityRights", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 21", node.getChildAt(0));
    }

    @Test
    void correct_InvalidModeNumber() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        DERNull.INSTANCE,
                        new ASN1Enumerated(2),
                })
        );
        UbiquityRightsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "ubiquityRights", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "mode: 2", node.getChildAt(2));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(3, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        {
            final AbstractAsn1TreeNode ubSubRights = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(12, "ubSubRights: 0b101010101010", ubSubRights);
            Asn1TestUtil.assertNodeMatches(0, "FormFillInAndSave: TRUE", ubSubRights.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "FormImportExport: FALSE", ubSubRights.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "FormAddDelete: TRUE", ubSubRights.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "SubmitStandalone: FALSE", ubSubRights.getChildAt(3));
            Asn1TestUtil.assertNodeMatches(0, "SpawnTemplate: TRUE", ubSubRights.getChildAt(4));
            Asn1TestUtil.assertNodeMatches(0, "Signing: FALSE", ubSubRights.getChildAt(5));
            Asn1TestUtil.assertNodeMatches(0, "AnnotModify: TRUE", ubSubRights.getChildAt(6));
            Asn1TestUtil.assertNodeMatches(0, "AnnotImportExport: FALSE", ubSubRights.getChildAt(7));
            Asn1TestUtil.assertNodeMatches(0, "BarcodePlaintext: TRUE", ubSubRights.getChildAt(8));
            Asn1TestUtil.assertNodeMatches(0, "AnnotOnline: FALSE", ubSubRights.getChildAt(9));
            Asn1TestUtil.assertNodeMatches(0, "FormOnline: TRUE", ubSubRights.getChildAt(10));
            Asn1TestUtil.assertNodeMatches(0, "EFModify: FALSE", ubSubRights.getChildAt(11));
        }
        Asn1TestUtil.assertNodeMatches(0, "mode: 1 (production)", node.getChildAt(2));
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERBitString(new byte[] {(byte) 0b10101010, (byte) 0b10100000}, 4),
                        new ASN1Enumerated(1)
                })
        );
    }
}
