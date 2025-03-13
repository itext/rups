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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class AlgorithmIdentifierCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        AlgorithmIdentifierCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "algorithmIdentifier");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        AlgorithmIdentifierCorrector.INSTANCE.correct(node, "ai");
        validateDefaultNode(node, "ai");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                DERNull.INSTANCE
        );
        AlgorithmIdentifierCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node);
    }

    @Test
    void correct_InvalidAlgorithmType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        DERNull.INSTANCE,
                        DERNull.INSTANCE,
                })
        );
        AlgorithmIdentifierCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "algorithmIdentifier", node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", node.getChildAt(1));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(
                0,
                "algorithm: 1.2.840.113549.1.1.8 (/iso/member-body/us/rsadsi/pkcs/pkcs-1/id-mgf1)",
                node.getChildAt(0)
        );
        {
            final AbstractAsn1TreeNode parameters = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "parameters", parameters);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 2.16.840.1.101.3.4.2.3 (/joint-iso-itu-t/country/us/organization"
                            + "/gov/csor/nistAlgorithms/hashAlgs/id-sha512)",
                    parameters.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", parameters.getChildAt(1));
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1ObjectIdentifier("1.2.840.113549.1.1.8"),           // mgf1
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.3"), // id-sha512
                                DERNull.INSTANCE,
                        }),
                })
        );
    }
}
