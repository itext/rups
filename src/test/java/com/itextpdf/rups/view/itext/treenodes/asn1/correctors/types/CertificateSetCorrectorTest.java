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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class CertificateSetCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CertificateSetCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "certs");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CertificateSetCorrector.INSTANCE.correct(node, "certificates");
        validateDefaultNode(node, "certificates");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new DERSequence());
        CertificateSetCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node);
    }

    @Test
    void correct_EmptyRootSet() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new DERSet());
        CertificateSetCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "certs", node);
    }

    @Test
    void correct_InvalidCertificateType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(new DERTaggedObject(true, 9, new DERSequence()))
        );
        CertificateSetCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certs", node);
        Asn1TestUtil.assertNodeMatches(0, "[9] EXPLICIT SEQUENCE", node.getChildAt(0));
    }

    @Test
    void correct_InvalidOtherCertificateBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(new DERTaggedObject(false, 3, new ASN1Integer(1)))
        );
        CertificateSetCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certs", node);
        Asn1TestUtil.assertNodeMatches(0, "[3] IMPLICIT INTEGER: 1", node.getChildAt(0));
    }

    @Test
    void correct_EmptyOtherCertificateSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(new DERTaggedObject(false, 3, new DERSequence()))
        );
        CertificateSetCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certs", node);
        Asn1TestUtil.assertNodeMatches(0, "other", node.getChildAt(0));
    }

    @Test
    void correct_InvalidOtherCertFormatType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(
                        new DERTaggedObject(false, 3, new DERSequence(
                                new ASN1Integer(1)
                        ))
                )
        );
        CertificateSetCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certs", node);
        {
            final AbstractAsn1TreeNode cert = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "other", cert);
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", cert.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidCertificateChoiceBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(
                        new DERTaggedObject(false, 0, new ASN1Integer(1))
                )
        );
        CertificateSetCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certs", node);
        Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT INTEGER: 1", node.getChildAt(0));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(5, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode crt = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "certificate", crt);
            Asn1TestUtil.assertNodeMatches(0, "tbsCertificate", crt.getChildAt(0));
            {
                final AbstractAsn1TreeNode signatureAlgorithm = crt.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(2, "signatureAlgorithm", signatureAlgorithm);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "algorithm: 1.2.840.113549.1.1.11 (/iso/member-body/us/rsadsi/pkcs/pkcs-1"
                                + "/sha256WithRSAEncryption)",
                        signatureAlgorithm.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(
                        0, "parameters: NULL", signatureAlgorithm.getChildAt(1)
                );
            }
            Asn1TestUtil.assertNodeMatches(0, "signatureValue: 0x1122334455", crt.getChildAt(2));
        }
        Asn1TestUtil.assertNodeMatches(0, "extendedCertificate", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "v1AttrCert", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "v2AttrCert", node.getChildAt(3));
        {
            final AbstractAsn1TreeNode crt = node.getChildAt(4);
            Asn1TestUtil.assertNodeMatches(2, "other", crt);
            Asn1TestUtil.assertNodeMatches(
                    0, "otherCertFormat: 1.2 (/iso/member-body)", crt.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "otherCert: NULL", crt.getChildAt(1));
        }
    }

    private static AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DLSet(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                // Not filling here, since Certificate is covered separately
                                new DERSequence(),
                                new DERSequence(new ASN1Encodable[] {
                                        // sha256WithRSAEncryption
                                        new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"),
                                        DERNull.INSTANCE,
                                }),
                                new DERBitString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}),
                        }),
                        new DERTaggedObject(false, 0, new DERSequence()),
                        new DERTaggedObject(false, 1, new DERSequence()),
                        new DERTaggedObject(false, 2, new DERSequence()),
                        new DERTaggedObject(false, 3, new DERSequence(new ASN1Encodable[] {
                                // member-body
                                new ASN1ObjectIdentifier("1.2"),
                                DERNull.INSTANCE,
                        })),
                })
        );
    }
}
