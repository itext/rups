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
final class CrlCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CrlCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "crl");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        CrlCorrector.INSTANCE.correct(node, "CertificateList");
        validateDefaultNode(node, "CertificateList");
    }

    @Test
    void correct_WithoutOptionalFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERSequence(),
                                new DERSequence(),
                                new DERUTCTime("240501100000"),
                        }),
                        new DERSequence(),
                        new DERBitString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}),
                })
        );
        CrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "crl", node);
        {
            final AbstractAsn1TreeNode tbsCertList = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsCertList", tbsCertList);
            Asn1TestUtil.assertNodeMatches(0, "signature", tbsCertList.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "issuer", tbsCertList.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "thisUpdate: 240501100000 (2024-05-01T10:00:00Z)",
                    tbsCertList.getChildAt(2)
            );
        }
        Asn1TestUtil.assertNodeMatches(0, "signatureAlgorithm", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "signatureValue: 0x1122334455", node.getChildAt(2));
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        CrlCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidCertificateListTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERSequence()),
                        new DERTaggedObject(true, 1, new DERSequence()),
                        new DEROctetString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}),
                })
        );
        CrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "crl", node);
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SEQUENCE", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "OCTET STRING: 0x1122334455", node.getChildAt(2));
    }

    @Test
    void correct_InvalidTbsCertListTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 5, new ASN1Integer(0)),
                                new DERTaggedObject(true, 6, new ASN1Integer(1)),
                                new DERTaggedObject(true, 7, new ASN1Integer(2)),
                                new DERTaggedObject(true, 8, new ASN1Integer(3)),
                                new DERTaggedObject(true, 9, new ASN1Integer(4)),
                                new ASN1Integer(5),
                                new ASN1Integer(6),
                        }),
                })
        );
        CrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crl", node);
        {
            final AbstractAsn1TreeNode tbsCertList = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(7, "tbsCertList", tbsCertList);
            for (int i = 0; i < 5; ++i) {
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "[" + (i + 5) + "] EXPLICIT INTEGER: " + i,
                        tbsCertList.getChildAt(i)
                );
            }
            for (int i = 5; i < 7; ++i) {
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: " + i, tbsCertList.getChildAt(i));
            }
        }
    }

    @Test
    void correct_WithoutNextUpdate() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(1),
                                new DERSequence(),
                                new DERSequence(),
                                new DERGeneralizedTime("20240501100000Z"),
                                new DERSequence(),
                                new DERTaggedObject(true, 0, new DERSequence()),
                        }),
                })
        );
        CrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crl", node);
        {
            final AbstractAsn1TreeNode tbsCertList = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(6, "tbsCertList", tbsCertList);
            Asn1TestUtil.assertNodeMatches(0, "version: 1 (v2)", tbsCertList.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "signature", tbsCertList.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "issuer", tbsCertList.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "thisUpdate: 20240501100000Z (2024-05-01T10:00:00Z)",
                    tbsCertList.getChildAt(3)
            );
            Asn1TestUtil.assertNodeMatches(0, "revokedCertificates", tbsCertList.getChildAt(4));
            Asn1TestUtil.assertNodeMatches(0, "crlExtensions", tbsCertList.getChildAt(5));
        }
    }

    @Test
    void correct_InvalidRevokedCertificateTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(1),
                                new DERSequence(),
                                new DERSequence(),
                                new DERGeneralizedTime("20240501100000Z"),
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERTaggedObject(true, 1, new DERSequence()),
                                        new DERSequence(),
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERTaggedObject(true, 0, new ASN1Integer(1)),
                                                new DERTaggedObject(true, 1, new DERGeneralizedTime("20240501100000Z")),
                                                new DERTaggedObject(true, 2, new DERSequence()),
                                        }),
                                }),
                        }),
                })
        );
        CrlCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "crl", node);
        {
            final AbstractAsn1TreeNode tbsCertList = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(5, "tbsCertList", tbsCertList);
            Asn1TestUtil.assertNodeMatches(0, "version: 1 (v2)", tbsCertList.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "signature", tbsCertList.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "issuer", tbsCertList.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "thisUpdate: 20240501100000Z (2024-05-01T10:00:00Z)",
                    tbsCertList.getChildAt(3)
            );
            {
                final AbstractAsn1TreeNode revokedCertificates = tbsCertList.getChildAt(4);
                Asn1TestUtil.assertNodeMatches(4, "revokedCertificates", revokedCertificates);
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", revokedCertificates.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SEQUENCE", revokedCertificates.getChildAt(1));
                Asn1TestUtil.assertNodeMatches(0, "revokedCertificate", revokedCertificates.getChildAt(2));
                {
                    final AbstractAsn1TreeNode cert = revokedCertificates.getChildAt(3);
                    Asn1TestUtil.assertNodeMatches(3, "revokedCertificate", cert);
                    Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT INTEGER: 1", cert.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "[1] EXPLICIT GeneralizedTime: 20240501100000Z (2024-05-01T10:00:00Z)",
                            cert.getChildAt(1)
                    );
                    Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT SEQUENCE", cert.getChildAt(2));
                }
            }
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(3, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode tbsCertList = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(7, "tbsCertList", tbsCertList);
            Asn1TestUtil.assertNodeMatches(0, "version: 1 (v2)", tbsCertList.getChildAt(0));
            {
                final AbstractAsn1TreeNode signature = tbsCertList.getChildAt(1);
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
                final AbstractAsn1TreeNode issuer = tbsCertList.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "issuer", issuer);
                Asn1TestUtil.assertNodeMatches(0, "relativeDistinguishedName", issuer.getChildAt(0));
            }
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "thisUpdate: 20240501100000Z (2024-05-01T10:00:00Z)",
                    tbsCertList.getChildAt(3)
            );
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "nextUpdate: 260501100000 (2026-05-01T10:00:00Z)",
                    tbsCertList.getChildAt(4)
            );
            {
                final AbstractAsn1TreeNode revokedCertificates = tbsCertList.getChildAt(5);
                Asn1TestUtil.assertNodeMatches(1, "revokedCertificates", revokedCertificates);
                {
                    final AbstractAsn1TreeNode revokedCertificate = revokedCertificates.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(3, "revokedCertificate", revokedCertificate);
                    Asn1TestUtil.assertNodeMatches(0, "userCertificate: 42", revokedCertificate.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "revocationDate: 200501100000 (2020-05-01T10:00:00Z)",
                            revokedCertificate.getChildAt(1)
                    );
                    {
                        final AbstractAsn1TreeNode crlEntryExtensions = revokedCertificate.getChildAt(2);
                        Asn1TestUtil.assertNodeMatches(1, "crlEntryExtensions", crlEntryExtensions);
                        Asn1TestUtil.assertNodeMatches(0, "extension", crlEntryExtensions.getChildAt(0));
                    }
                }
            }
            {
                final AbstractAsn1TreeNode crlExtensions = tbsCertList.getChildAt(6);
                Asn1TestUtil.assertNodeMatches(1, "crlExtensions", crlExtensions);
                Asn1TestUtil.assertNodeMatches(0, "extension", crlExtensions.getChildAt(0));
            }
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
                                new ASN1Integer(1),
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"),  // sha256WithRSAEncryption
                                        DERNull.INSTANCE,
                                }),
                                new DERSequence(
                                        new DERSet()
                                ),
                                new DERGeneralizedTime("20240501100000Z"),
                                new DERUTCTime("260501100000"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new ASN1Integer(42),
                                                new DERUTCTime("200501100000"),
                                                new DERSequence(
                                                        new DERSequence()
                                                )
                                        })
                                ),
                                new DERTaggedObject(true, 0, new DERSequence(
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
