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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTCTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class CertificateCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CertificateCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "certificate");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CertificateCorrector.INSTANCE.correct(node, "crt");
        validateDefaultNode(node, "crt");
    }

    @Test
    void correct_WithoutOptionalFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(42),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                        }),
                        new DERSequence(),
                        new DERBitString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}),
                })
        );
        CertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "certificate", node);
        {
            final AbstractAsn1TreeNode tbsCertificate = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(6, "tbsCertificate", tbsCertificate);
            Asn1TestUtil.assertNodeMatches(0, "serialNumber: 42", tbsCertificate.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "signature", tbsCertificate.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "issuer", tbsCertificate.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "validity", tbsCertificate.getChildAt(3));
            Asn1TestUtil.assertNodeMatches(0, "subject", tbsCertificate.getChildAt(4));
            Asn1TestUtil.assertNodeMatches(0, "subjectPublicKeyInfo", tbsCertificate.getChildAt(5));
        }
        Asn1TestUtil.assertNodeMatches(0, "signatureAlgorithm", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "signatureValue: 0x1122334455", node.getChildAt(2));
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        CertificateCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidCertificateTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSet(),
                        new DERSet(),
                        new DEROctetString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}),
                })
        );
        CertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "certificate", node);
        Asn1TestUtil.assertNodeMatches(0, "SET", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "SET", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "OCTET STRING: 0x1122334455", node.getChildAt(2));
    }

    @Test
    void correct_InvalidTbsCertificateTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 10, DERNull.INSTANCE),
                                new DERTaggedObject(true, 11, new ASN1Integer(1)),
                                new DERTaggedObject(true, 12, new ASN1Integer(2)),
                                new DERTaggedObject(true, 13, new ASN1Integer(3)),
                                new DERTaggedObject(true, 14, new ASN1Integer(4)),
                                new DERTaggedObject(true, 15, new ASN1Integer(5)),
                                new DERTaggedObject(true, 16, new ASN1Integer(6)),
                                new ASN1Integer(7),
                                new ASN1Integer(8),
                                new ASN1Integer(9),
                        }),
                })
        );
        CertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certificate", node);
        {
            final AbstractAsn1TreeNode tbsCertificate = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(10, "tbsCertificate", tbsCertificate);
            Asn1TestUtil.assertNodeMatches(0, "[10] EXPLICIT NULL: NULL", tbsCertificate.getChildAt(0));
            for (int i = 1; i < 7; ++i) {
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "[1" + i + "] EXPLICIT INTEGER: " + i,
                        tbsCertificate.getChildAt(i)
                );
            }
            for (int i = 7; i < 9; ++i) {
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: " + i, tbsCertificate.getChildAt(i));
            }
        }
    }

    @Test
    void correct_TaggedSignatureAlgorithm() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(),
                        new DERTaggedObject(true, 0, new DERSequence()),
                })
        );
        CertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "certificate", node);
        Asn1TestUtil.assertNodeMatches(0, "tbsCertificate", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", node.getChildAt(1));
    }

    @Test
    void correct_TaggedValidityTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 0, new ASN1Integer(2)),
                                new ASN1Integer(42),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(new ASN1Encodable[] {
                                        new DERTaggedObject(true, 0, new DERGeneralizedTime("20240501100000Z")),
                                        new DERTaggedObject(true, 1, new DERUTCTime("260501100000")),
                                }),
                        }),
                })
        );
        CertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certificate", node);
        {
            final AbstractAsn1TreeNode tbsCertificate = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(5, "tbsCertificate", tbsCertificate);
            Asn1TestUtil.assertNodeMatches(0, "version: 2 (v3)", tbsCertificate.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "serialNumber: 42", tbsCertificate.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "signature", tbsCertificate.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "issuer", tbsCertificate.getChildAt(3));
            {
                final AbstractAsn1TreeNode validity = tbsCertificate.getChildAt(4);
                Asn1TestUtil.assertNodeMatches(2, "validity", validity);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "[0] EXPLICIT GeneralizedTime: 20240501100000Z (2024-05-01T10:00:00Z)",
                        validity.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "[1] EXPLICIT UTCTime: 260501100000 (2026-05-01T10:00:00Z)",
                        validity.getChildAt(1)
                );
            }
        }
    }

    @Test
    void correct_TaggedSubjectPublicKeyInfoTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 0, new ASN1Integer(2)),
                                new ASN1Integer(42),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(new ASN1Encodable[] {
                                        new DERTaggedObject(true, 0, new DERSequence()),
                                        new DERTaggedObject(true, 1,
                                                new DERBitString(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05})
                                        ),
                                }),
                        }),
                })
        );
        CertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certificate", node);
        {
            final AbstractAsn1TreeNode tbsCertificate = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(7, "tbsCertificate", tbsCertificate);
            Asn1TestUtil.assertNodeMatches(0, "version: 2 (v3)", tbsCertificate.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "serialNumber: 42", tbsCertificate.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "signature", tbsCertificate.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "issuer", tbsCertificate.getChildAt(3));
            Asn1TestUtil.assertNodeMatches(0, "validity", tbsCertificate.getChildAt(4));
            Asn1TestUtil.assertNodeMatches(0, "subject", tbsCertificate.getChildAt(5));
            {
                final AbstractAsn1TreeNode subjectPublicKeyInfo = tbsCertificate.getChildAt(6);
                Asn1TestUtil.assertNodeMatches(2, "subjectPublicKeyInfo", subjectPublicKeyInfo);
                Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", subjectPublicKeyInfo.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "[1] EXPLICIT BIT STRING: 0x0102030405",
                        subjectPublicKeyInfo.getChildAt(1)
                );
            }
        }
    }

    @Test
    void correct_InvalidUniqueIDBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 0, new ASN1Integer(2)),
                                new ASN1Integer(42),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                                new DERSequence(),
                                new DERTaggedObject(false, 1, new ASN1Integer(1)),
                        }),
                })
        );
        CertificateCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "certificate", node);
        {
            final AbstractAsn1TreeNode tbsCertificate = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(8, "tbsCertificate", tbsCertificate);
            Asn1TestUtil.assertNodeMatches(0, "version: 2 (v3)", tbsCertificate.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "serialNumber: 42", tbsCertificate.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "signature", tbsCertificate.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "issuer", tbsCertificate.getChildAt(3));
            Asn1TestUtil.assertNodeMatches(0, "validity", tbsCertificate.getChildAt(4));
            Asn1TestUtil.assertNodeMatches(0, "subject", tbsCertificate.getChildAt(5));
            Asn1TestUtil.assertNodeMatches(0, "subjectPublicKeyInfo", tbsCertificate.getChildAt(6));
            Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT INTEGER: 1", tbsCertificate.getChildAt(7));
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(3, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode tbsCertificate = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(10, "tbsCertificate", tbsCertificate);
            Asn1TestUtil.assertNodeMatches(0, "version: 2 (v3)", tbsCertificate.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "serialNumber: 42", tbsCertificate.getChildAt(1));
            {
                final AbstractAsn1TreeNode signature = tbsCertificate.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(2, "signature", signature);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "algorithm: 1.2.840.113549.1.1.11 (/iso/member-body/us/rsadsi/pkcs"
                                + "/pkcs-1/sha256WithRSAEncryption)",
                        signature.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", signature.getChildAt(1));
            }
            {
                final AbstractAsn1TreeNode issuer = tbsCertificate.getChildAt(3);
                Asn1TestUtil.assertNodeMatches(1, "issuer", issuer);
                Asn1TestUtil.assertNodeMatches(0, "relativeDistinguishedName", issuer.getChildAt(0));
            }
            {
                final AbstractAsn1TreeNode validity = tbsCertificate.getChildAt(4);
                Asn1TestUtil.assertNodeMatches(2, "validity", validity);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "notBefore: 20240501100000Z (2024-05-01T10:00:00Z)",
                        validity.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "notAfter: 260501100000 (2026-05-01T10:00:00Z)",
                        validity.getChildAt(1)
                );
            }
            {
                final AbstractAsn1TreeNode subject = tbsCertificate.getChildAt(5);
                Asn1TestUtil.assertNodeMatches(1, "subject", subject);
                Asn1TestUtil.assertNodeMatches(0, "relativeDistinguishedName", subject.getChildAt(0));
            }
            {
                final AbstractAsn1TreeNode subjectPublicKeyInfo = tbsCertificate.getChildAt(6);
                Asn1TestUtil.assertNodeMatches(2, "subjectPublicKeyInfo", subjectPublicKeyInfo);
                {
                    final AbstractAsn1TreeNode algorithm = subjectPublicKeyInfo.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(2, "algorithm", algorithm);
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "algorithm: 1.2.840.113549.1.1.1 (/iso/member-body/us/rsadsi/pkcs"
                                    + "/pkcs-1/rsaEncryption)",
                            algorithm.getChildAt(0)
                    );
                    Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", algorithm.getChildAt(1));
                }
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "subjectPublicKey: 0x0102030405",
                        subjectPublicKeyInfo.getChildAt(1)
                );
            }
            Asn1TestUtil.assertNodeMatches(0, "issuerUniqueID: 0x1234564321", tbsCertificate.getChildAt(7));
            Asn1TestUtil.assertNodeMatches(0, "subjectUniqueID: 0x2143653412", tbsCertificate.getChildAt(8));
            Asn1TestUtil.assertNodeMatches(1, "extensions", tbsCertificate.getChildAt(9));
            Asn1TestUtil.assertNodeMatches(0, "extension", tbsCertificate.getChildAt(9).getChildAt(0));
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
        Asn1TestUtil.assertNodeMatches(0, "signatureValue: 0x1122334455", node.getChildAt(2));
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        /*
         * No need to fill everything here, as it will be massive...
         * It is tested individually anyway.
         */
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 0, new ASN1Integer(2)),
                                new ASN1Integer(42),
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"),  // sha256WithRSAEncryption
                                        DERNull.INSTANCE,
                                }),
                                new DERSequence(
                                        new DERSet()
                                ),
                                new DERSequence(new ASN1Encodable[] {
                                        new DERGeneralizedTime("20240501100000Z"),
                                        new DERUTCTime("260501100000"),
                                }),
                                new DERSequence(
                                        new DERSet()
                                ),
                                new DERSequence(new ASN1Encodable[] {
                                        new DERSequence(new ASN1Encodable[] {
                                                new ASN1ObjectIdentifier("1.2.840.113549.1.1.1"),   // rsaEncryption
                                                DERNull.INSTANCE,
                                        }),
                                        new DERBitString(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05}),
                                }),
                                new DERTaggedObject(false, 1,
                                        new DERBitString(new byte[] {0x12, 0x34, 0x56, 0x43, 0x21})
                                ),
                                new DERTaggedObject(false, 2,
                                        new DERBitString(new byte[] {0x21, 0x43, 0x65, 0x34, 0x12})
                                ),
                                new DERTaggedObject(true, 3, new DERSequence(
                                        new DERSequence()
                                )),
                        }),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"),  // sha256WithRSAEncryption
                                DERNull.INSTANCE,
                        }),
                        new DERBitString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}),
                })
        );
    }
}