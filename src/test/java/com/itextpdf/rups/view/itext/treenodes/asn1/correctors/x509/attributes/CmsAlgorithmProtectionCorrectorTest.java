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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class CmsAlgorithmProtectionCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CmsAlgorithmProtectionCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "cmsAlgorithmProtection");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CmsAlgorithmProtectionCorrector.INSTANCE.correct(node, "CAP");
        validateDefaultNode(node, "CAP");
    }

    @Test
    void correct_WithMac() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(
                                new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1")  // sha256
                        ),
                        new DERTaggedObject(false, 2, new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2.840.113549.2.9"),     // hmacWithSHA256
                                DERNull.INSTANCE,
                        })),
                })
        );
        CmsAlgorithmProtectionCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "cmsAlgorithmProtection", node);
        {
            final AbstractAsn1TreeNode digestAlgorithm = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "digestAlgorithm", digestAlgorithm);
            Asn1TestUtil.assertNodeMatches(0, "algorithm: 2.16.840.1.101.3.4.2.1 (/joint-iso-itu-t/country/us"
                    + "/organization/gov/csor/nistAlgorithms/hashAlgs/sha256)", digestAlgorithm.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode macAlgorithm = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "macAlgorithm", macAlgorithm);
            Asn1TestUtil.assertNodeMatches(0,
                    "algorithm: 1.2.840.113549.2.9 (/iso/member-body/us/rsadsi/digestAlgorithm"
                            + "/hmacWithSHA256)", macAlgorithm.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", macAlgorithm.getChildAt(1));
        }
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        CmsAlgorithmProtectionCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_EmptyRootSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        CmsAlgorithmProtectionCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "cmsAlgorithmProtection", node);
    }

    @Test
    void correct_InvalidSequenceTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(42),
                        new ASN1Integer(43),
                        new ASN1Integer(44),
                })
        );
        CmsAlgorithmProtectionCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "cmsAlgorithmProtection", node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 43", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 44", node.getChildAt(2));
    }

    @Test
    void correct_TaggedDigestAlgorithmIdentifier() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERSequence()),
                })
        );
        CmsAlgorithmProtectionCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "cmsAlgorithmProtection", node);
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", node.getChildAt(0));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode digestAlgorithm = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "digestAlgorithm", digestAlgorithm);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 2.16.840.1.101.3.4.2.1 (/joint-iso-itu-t/country/us"
                            + "/organization/gov/csor/nistAlgorithms/hashAlgs/sha256)",
                    digestAlgorithm.getChildAt(0)
            );
        }
        {
            final AbstractAsn1TreeNode signatureAlgorithm = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "signatureAlgorithm", signatureAlgorithm);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 1.2.840.113549.1.1.11 (/iso/member-body/us/rsadsi/pkcs/pkcs-1"
                            + "/sha256WithRSAEncryption)",
                    signatureAlgorithm.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", signatureAlgorithm.getChildAt(1));
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(
                                new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1")  // sha256
                        ),
                        new DERTaggedObject(false, 1, new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"),  // sha256WithRSAEncryption
                                DERNull.INSTANCE,
                        })),
                })
        );
    }
}