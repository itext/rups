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
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class SigningCertificateCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        SigningCertificateCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "signingCertificate");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        SigningCertificateCorrector.INSTANCE.correct(node, "SC");
        validateDefaultNode(node, "SC");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_EmptyRootSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "signingCertificate", node);
    }

    @Test
    void correct_InvalidSequenceTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(42),
                        new ASN1Integer(43),
                })
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "signingCertificate", node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 43", node.getChildAt(1));
    }

    @Test
    void correct_TaggedSequenceEntryTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(
                                new DERTaggedObject(false, 0, new DERSequence())
                        ),
                        new DERSequence(
                                new DERTaggedObject(false, 0, new DERSequence())
                        ),
                })
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "signingCertificate", node);
        {
            final AbstractAsn1TreeNode certs = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "certs", certs);
            Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT SEQUENCE", certs.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode policies = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(1, "policies", policies);
            Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT SEQUENCE", policies.getChildAt(0));
        }
    }

    @Test
    void correct_EmptyEssCertIdSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERSequence()
                        )
                )
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "signingCertificate", node);
        {
            final AbstractAsn1TreeNode certs = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "certs", certs);
            Asn1TestUtil.assertNodeMatches(0, "essCertID", certs.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidEssCertIdSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new ASN1Integer(2),
                                })
                        )
                )
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "signingCertificate", node);
        {
            final AbstractAsn1TreeNode certs = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "certs", certs);
            {
                final AbstractAsn1TreeNode cert = certs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "essCertID", cert);
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", cert.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: 2", cert.getChildAt(1));
            }
        }
    }

    @Test
    void correct_EmptyIssuerSerialSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERSequence(new ASN1Encodable[] {
                                        new DEROctetString(new byte[] {0}),
                                        new DERSequence(),
                                })
                        )
                )
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "signingCertificate", node);
        {
            final AbstractAsn1TreeNode certs = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "certs", certs);
            {
                final AbstractAsn1TreeNode cert = certs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "essCertID", cert);
                Asn1TestUtil.assertNodeMatches(0, "certHash: 0x00", cert.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "issuerSerial", cert.getChildAt(1));
            }
        }
    }

    @Test
    void correct_TaggedIssuerSerialSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERSequence(new ASN1Encodable[] {
                                        new DEROctetString(new byte[] {0}),
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERTaggedObject(true, 0, new DERSequence()),
                                                new DERTaggedObject(true, 1, new ASN1Integer(1)),
                                        }),
                                })
                        )
                )
        );
        SigningCertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "signingCertificate", node);
        {
            final AbstractAsn1TreeNode certs = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "certs", certs);
            {
                final AbstractAsn1TreeNode cert = certs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "essCertID", cert);
                Asn1TestUtil.assertNodeMatches(0, "certHash: 0x00", cert.getChildAt(0));
                {
                    final AbstractAsn1TreeNode issuerSerial = cert.getChildAt(1);
                    Asn1TestUtil.assertNodeMatches(2, "issuerSerial", issuerSerial);
                    Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", issuerSerial.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT INTEGER: 1", issuerSerial.getChildAt(1));
                }
            }
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode certs = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "certs", certs);
            {
                final AbstractAsn1TreeNode cert = certs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "essCertID", cert);
                Asn1TestUtil.assertNodeMatches(0, "certHash: 0x112233", cert.getChildAt(0));
                {
                    final AbstractAsn1TreeNode issuerSerial = cert.getChildAt(1);
                    Asn1TestUtil.assertNodeMatches(2, "issuerSerial", issuerSerial);
                    {
                        final AbstractAsn1TreeNode issuer = issuerSerial.getChildAt(0);
                        Asn1TestUtil.assertNodeMatches(1, "issuer", issuer);
                        Asn1TestUtil.assertNodeMatches(0, "dnsName: example.com", issuer.getChildAt(0));
                    }
                    Asn1TestUtil.assertNodeMatches(0, "serialNumber: 42", issuerSerial.getChildAt(1));
                }
            }
        }
        {
            final AbstractAsn1TreeNode policies = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(1, "policies", policies);
            {
                final AbstractAsn1TreeNode policy = policies.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "policyInformation", policy);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "policyIdentifier: 2.23.140.1.2.1 (/joint-iso-itu-t"
                                + "/international-organizations/ca-browser-forum"
                                + "/certificate-policies/baseline-requirements/domain-validated)",
                        policy.getChildAt(0)
                );
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(
                                new DERSequence(new ASN1Encodable[] {
                                        new DEROctetString(new byte[] {0x11, 0x22, 0x33}),
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(
                                                        new DERTaggedObject(false, 2,
                                                                new DERIA5String("example.com")
                                                        )
                                                ),
                                                new ASN1Integer(42),
                                        }),
                                })
                        ),
                        new DERSequence(
                                new DERSequence(
                                        new ASN1ObjectIdentifier("2.23.140.1.2.1")  // domain-validated
                                )
                        ),
                })
        );
    }
}