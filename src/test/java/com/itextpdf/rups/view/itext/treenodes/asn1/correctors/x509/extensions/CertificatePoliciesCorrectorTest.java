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

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class CertificatePoliciesCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CertificatePoliciesCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "certificatePolicies");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CertificatePoliciesCorrector.INSTANCE.correct(node, "CP");
        validateDefaultNode(node, "CP");
    }

    @Test
    void correct_EmptySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        CertificatePoliciesCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "certificatePolicies", node);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        CertificatePoliciesCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidSequenceEntry() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERPrintableString("test")
                )
        );
        CertificatePoliciesCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certificatePolicies", node);
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: test", node.getChildAt(0));
    }

    @Test
    void correct_TaggedSequenceEntryType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERTaggedObject(true, 0, new DERSequence())
                )
        );
        CertificatePoliciesCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certificatePolicies", node);
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", node.getChildAt(0));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode first = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "policyInformation", first);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "policyIdentifier: 2.23.140.1.2.1 (/joint-iso-itu-t/international-organizations"
                            + "/ca-browser-forum/certificate-policies/baseline-requirements"
                            + "/domain-validated)", first.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode second = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "policyInformation", second);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "policyIdentifier: 2.23.140.1.1 (/joint-iso-itu-t/international-organizations"
                            + "/ca-browser-forum/certificate-policies/extended-validation)",
                    second.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(1, "policyQualifiers", second.getChildAt(1));
            {
                final AbstractAsn1TreeNode policyQualifierInfo = second.getChildAt(1).getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "policyQualifierInfo", policyQualifierInfo);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "policyQualifierId: 1.3.6.1.5.5.7.2.1 (/iso/identified-organization/dod"
                                + "/internet/security/mechanisms/pkix/qt/cps)",
                        policyQualifierInfo.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "qualifier: http://www.digicert.com/CPS",
                        policyQualifierInfo.getChildAt(1)
                );
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(
                                new ASN1ObjectIdentifier("2.23.140.1.2.1")  // domain-validated
                        ),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("2.23.140.1.1"),   // extended-validation
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new ASN1ObjectIdentifier("1.3.6.1.5.5.7.2.1"),  // id-qt-cps
                                                new DERIA5String("http://www.digicert.com/CPS"),
                                        })
                                ),
                        }),
                })
        );
    }
}
