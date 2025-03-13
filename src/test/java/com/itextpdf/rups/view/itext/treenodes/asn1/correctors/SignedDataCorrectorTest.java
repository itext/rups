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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.BERSet;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class SignedDataCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        SignedDataCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "signedData");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        SignedDataCorrector.INSTANCE.correct(node, "sd");
        validateDefaultNode(node, "sd");
    }

    @Test
    void correct_DifferentVersions() {
        testVersion(-1, null);
        for (int i = 0; i < 6; ++i) {
            testVersion(i, "v" + i);
        }
        testVersion(6, null);
    }

    @Test
    void correct_WithSubjectKeyIdentifier() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERTaggedObject(false, 0,
                                                new DEROctetString(new byte[] {1})
                                        ),
                                })
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode signerInfos = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "signerInfos", signerInfos);
            {
                final AbstractAsn1TreeNode signerInfo = signerInfos.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "signerInfo", signerInfo);
                Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", signerInfo.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "sid: 0x01", signerInfo.getChildAt(1));
            }
        }
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_EmptySignedDataSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "signedData", node);
    }

    @Test
    void correct_InvalidSignedDataTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        DERNull.INSTANCE,
                        new ASN1Integer(1),
                        new ASN1Integer(2),
                        new DERTaggedObject(false, 0, new ASN1Integer(3)),
                        new DERTaggedObject(false, 1, new ASN1Integer(4)),
                        new ASN1Integer(5),
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(6, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 2", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT INTEGER: 3", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT INTEGER: 4", node.getChildAt(4));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 5", node.getChildAt(5));
    }

    @Test
    void correct_InvalidDigestAlgorithmIdentifierType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(
                                new DERTaggedObject(true, 1, new DERSequence())
                        ),
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(1, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SEQUENCE", node.getChildAt(1).getChildAt(0));
    }

    @Test
    void correct_InvalidCertificateType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 0, new DERSet(
                                new DERTaggedObject(true, 9, new DERSequence())
                        ))
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "certificates", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "[9] EXPLICIT SEQUENCE", node.getChildAt(3).getChildAt(0));
    }

    @Test
    void correct_InvalidOtherCertificateBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 0, new DERSet(
                                new DERTaggedObject(false, 3, new ASN1Integer(1))
                        ))
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "certificates", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "[3] IMPLICIT INTEGER: 1", node.getChildAt(3).getChildAt(0));
    }

    @Test
    void correct_EmptyOtherCertificateSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 0, new DERSet(
                                new DERTaggedObject(false, 3, new DERSequence())
                        ))
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "certificates", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "other", node.getChildAt(3).getChildAt(0));
    }

    @Test
    void correct_InvalidOtherCertFormatType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 0, new DERSet(
                                new DERTaggedObject(false, 3, new DERSequence(
                                        new ASN1Integer(1)
                                ))
                        ))
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode certificates = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "certificates", certificates);
            {
                final AbstractAsn1TreeNode certificate = certificates.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "other", certificate);
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", certificate.getChildAt(0));
            }
        }
    }

    @Test
    void correct_InvalidEncapsulatedContentInfoTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(0),
                                new ASN1Integer(1),
                        }),
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        {
            final AbstractAsn1TreeNode ci = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(2, "encapContentInfo", ci);
            for (int i = 0; i < 2; ++i) {
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: " + i, ci.getChildAt(i));
            }
        }
    }

    @Test
    void correct_InvalidEContentBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2"),    // member-body
                                new DERTaggedObject(true, 0, new ASN1Integer(1)),
                        }),
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        {
            final AbstractAsn1TreeNode ci = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(2, "encapContentInfo", ci);
            Asn1TestUtil.assertNodeMatches(0, "eContentType: 1.2 (/iso/member-body)", ci.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT INTEGER: 1", ci.getChildAt(1));
        }
    }

    @Test
    void correct_UnknownEContentDataType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2"),    // member-body
                                new DERTaggedObject(true, 0, new DEROctetString(new byte[] {0x05, 0x00})),
                        }),
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        {
            final AbstractAsn1TreeNode ci = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(2, "encapContentInfo", ci);
            Asn1TestUtil.assertNodeMatches(0, "eContentType: 1.2 (/iso/member-body)", ci.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "eContent: 0x0500", ci.getChildAt(1));
        }
    }

    @Test
    void correct_InvalidSignerInfoType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new ASN1Integer(1)
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "signerInfos", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", node.getChildAt(3).getChildAt(0));
    }

    @Test
    void correct_EmptySignerInfoSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence()
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "signerInfos", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "signerInfo", node.getChildAt(3).getChildAt(0));
    }

    @Test
    void correct_InvalidSignerInfoSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        DERNull.INSTANCE,
                                        new DERTaggedObject(true, 11, new ASN1Integer(1)),
                                        new DERTaggedObject(true, 12, new ASN1Integer(2)),
                                        new DERTaggedObject(true, 13, new ASN1Integer(3)),
                                        new DERTaggedObject(true, 14, new ASN1Integer(4)),
                                        new DERTaggedObject(true, 15, new ASN1Integer(5)),
                                        new DERTaggedObject(true, 16, new ASN1Integer(6)),
                                })
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "signerInfos", node.getChildAt(3));
        {
            final AbstractAsn1TreeNode si = node.getChildAt(3).getChildAt(0);
            Asn1TestUtil.assertNodeMatches(7, "signerInfo", si);
            Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", si.getChildAt(0));
            for (int i = 1; i < 7; ++i) {
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "[" + (10 + i) + "] EXPLICIT INTEGER: " + i,
                        si.getChildAt(i)
                );
            }
        }
    }

    @Test
    void correct_EmptyIssuerAndSerialNumberSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERSequence(),
                                })
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "signerInfos", node.getChildAt(3));
        {
            final AbstractAsn1TreeNode si = node.getChildAt(3).getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "signerInfo", si);
            Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", si.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "sid", si.getChildAt(1));
        }
    }

    @Test
    void correct_InvalidIssuerAndSerialNumberSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERTaggedObject(true, 0, new ASN1Integer(0)),
                                                new DERTaggedObject(true, 1, new ASN1Integer(1)),
                                        }),
                                })
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "signerInfos", node.getChildAt(3));
        {
            final AbstractAsn1TreeNode si = node.getChildAt(3).getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "signerInfo", si);
            Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", si.getChildAt(0));
            {
                final AbstractAsn1TreeNode sid = si.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(2, "sid", sid);
                Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT INTEGER: 0", sid.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT INTEGER: 1", sid.getChildAt(1));
            }
        }
    }

    @Test
    void correct_EmptyOtherRevocationInfoFormatSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 1, new DERSet(
                                new DERTaggedObject(false, 1, new DERSequence())
                        ))
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "crls", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "other", node.getChildAt(3).getChildAt(0));
    }

    @Test
    void correct_InvalidRevocationInfoChoiceType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 1, new DERSet(
                                new DERTaggedObject(false, 2, new DERSequence())
                        ))
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode crls = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "crls", crls);
            Asn1TestUtil.assertNodeMatches(0, "[2] IMPLICIT SEQUENCE", crls.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidOtherRevocationInfoFormatBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 1, new DERSet(
                                new DERTaggedObject(false, 1, new ASN1Integer(1))
                        ))
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode crls = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "crls", crls);
            Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT INTEGER: 1", crls.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidAttributesBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERSequence(),
                                        new DERSequence(),
                                        new DERTaggedObject(false, 0, new ASN1Integer(1)),
                                })
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode signerInfos = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "signerInfos", signerInfos);
            {
                final AbstractAsn1TreeNode si = signerInfos.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(4, "signerInfo", si);
                Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", si.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "sid", si.getChildAt(1));
                Asn1TestUtil.assertNodeMatches(0, "digestAlgorithm", si.getChildAt(2));
                Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT INTEGER: 1", si.getChildAt(3));

            }
        }
    }

    @Test
    void correct_InvalidSubjectKeyIdentifierBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERTaggedObject(false, 0, new DERSequence()),
                                })
                        )
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode signerInfos = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "signerInfos", signerInfos);
            {
                final AbstractAsn1TreeNode signerInfo = signerInfos.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "signerInfo", signerInfo);
                Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", signerInfo.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT SEQUENCE", signerInfo.getChildAt(1));
            }
        }
    }

    @Test
    void correct_InvalidCertificateChoiceBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERTaggedObject(false, 0, new DERSet(
                                new DERTaggedObject(false, 0, new ASN1Integer(1))
                        )),
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode certificates = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "certificates", certificates);
            Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT INTEGER: 1", certificates.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidAttributes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(),
                        new DERSequence(),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERSequence(),
                                        new DERSequence(),
                                        new DERTaggedObject(false, 0, new BERSet(new ASN1Encodable[] {
                                                new ASN1Integer(0),
                                                new DERSequence(),
                                                new DERSequence(new ASN1Encodable[] {
                                                        new ASN1Integer(0),
                                                        new ASN1Integer(1),
                                                }),
                                        })),
                                })
                        ),
                })
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "signedData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "signerInfos", node.getChildAt(3));
        {
            final AbstractAsn1TreeNode si = node.getChildAt(3).getChildAt(0);
            Asn1TestUtil.assertNodeMatches(4, "signerInfo", si);
            Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", si.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "sid", si.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "digestAlgorithm", si.getChildAt(2));
            {
                final AbstractAsn1TreeNode attrs = si.getChildAt(3);
                Asn1TestUtil.assertNodeMatches(3, "signedAttrs", attrs);
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: 0", attrs.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "attribute", attrs.getChildAt(1));
                {
                    final AbstractAsn1TreeNode attr = attrs.getChildAt(2);
                    Asn1TestUtil.assertNodeMatches(2, "attribute", attr);
                    Asn1TestUtil.assertNodeMatches(0, "INTEGER: 0", attr.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", attr.getChildAt(1));
                }
            }
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(6, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        {
            final AbstractAsn1TreeNode digestAlgorithms = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(1, "digestAlgorithms", digestAlgorithms);
            {
                final AbstractAsn1TreeNode digestAlgorithm = digestAlgorithms.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "digestAlgorithm", digestAlgorithm);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "algorithm: 2.16.840.1.101.3.4.2.1 (/joint-iso-itu-t/country/us"
                                + "/organization/gov/csor/nistAlgorithms/hashAlgs/id-sha256)",
                        digestAlgorithm.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", digestAlgorithm.getChildAt(1));
            }
        }
        {
            final AbstractAsn1TreeNode encapContentInfo = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(2, "encapContentInfo", encapContentInfo);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "eContentType: 1.2.840.113549.1.9.16.1.4 (/iso/member-body/us/rsadsi/pkcs"
                            + "/pkcs-9/smime/ct/id-ct-TSTInfo)",
                    encapContentInfo.getChildAt(0)
            );
            {
                final AbstractAsn1TreeNode eContent = encapContentInfo.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(1, "eContent: 0x3000", eContent);
                Asn1TestUtil.assertNodeMatches(0, "tstInfo", eContent.getChildAt(0));
            }
        }
        {
            final AbstractAsn1TreeNode certificates = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(5, "certificates", certificates);
            {
                final AbstractAsn1TreeNode crt = certificates.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "certificate", crt);
                Asn1TestUtil.assertNodeMatches(0, "tbsCertificate", crt.getChildAt(0));
            }
            Asn1TestUtil.assertNodeMatches(0, "extendedCertificate", certificates.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "v1AttrCert", certificates.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "v2AttrCert", certificates.getChildAt(3));
            {
                final AbstractAsn1TreeNode crt = certificates.getChildAt(4);
                Asn1TestUtil.assertNodeMatches(2, "other", crt);
                Asn1TestUtil.assertNodeMatches(0, "otherCertFormat: 1.2 (/iso/member-body)", crt.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "otherCert: NULL", crt.getChildAt(1));
            }
        }
        {
            final AbstractAsn1TreeNode crls = node.getChildAt(4);
            Asn1TestUtil.assertNodeMatches(2, "crls", crls);
            {
                final AbstractAsn1TreeNode crl = crls.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "crl", crl);
                Asn1TestUtil.assertNodeMatches(0, "tbsCertList", crl.getChildAt(0));
            }
            {
                final AbstractAsn1TreeNode crl = crls.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(2, "other", crl);
                Asn1TestUtil.assertNodeMatches(0, "otherRevInfoFormat: 1.2 (/iso/member-body)", crl.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "otherRevInfo: NULL", crl.getChildAt(1));
            }
        }
        {
            final AbstractAsn1TreeNode signerInfos = node.getChildAt(5);
            Asn1TestUtil.assertNodeMatches(1, "signerInfos", signerInfos);
            validateDefaultSignerInfoNode(signerInfos.getChildAt(0));
        }
    }

    private static void validateDefaultSignerInfoNode(AbstractAsn1TreeNode node) {
        Asn1TestUtil.assertNodeMatches(7, "signerInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        {
            final AbstractAsn1TreeNode sid = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "sid", sid);
            {
                final AbstractAsn1TreeNode issuer = sid.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "issuer", issuer);
                Asn1TestUtil.assertNodeMatches(0, "relativeDistinguishedName", issuer.getChildAt(0));
            }
            Asn1TestUtil.assertNodeMatches(0, "serialNumber: 42", sid.getChildAt(1));
        }
        {
            final AbstractAsn1TreeNode digestAlgorithm = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(2, "digestAlgorithm", digestAlgorithm);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 2.16.840.1.101.3.4.2.1 (/joint-iso-itu-t/country/us"
                            + "/organization/gov/csor/nistAlgorithms/hashAlgs/id-sha256)",
                    digestAlgorithm.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", digestAlgorithm.getChildAt(1));
        }
        {
            final AbstractAsn1TreeNode signedAttrs = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "signedAttrs", signedAttrs);
            {
                final AbstractAsn1TreeNode attr = signedAttrs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "attribute", attr);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "attrType: 1.2.840.113549.1.9.25.4 (/iso/member-body/us/rsadsi/pkcs"
                                + "/pkcs-9/pkcs-9-at/sequenceNumber)",
                        attr.getChildAt(0)
                );
                {
                    final AbstractAsn1TreeNode values = attr.getChildAt(1);
                    Asn1TestUtil.assertNodeMatches(1, "attrValues", values);
                    Asn1TestUtil.assertNodeMatches(0, "sequenceNumber: 2", values.getChildAt(0));
                }
            }
        }
        {
            final AbstractAsn1TreeNode signatureAlgorithm = node.getChildAt(4);
            Asn1TestUtil.assertNodeMatches(2, "signatureAlgorithm", signatureAlgorithm);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 1.2.840.113549.1.1.1 (/iso/member-body/us/rsadsi/pkcs/pkcs-1"
                            + "/rsaEncryption)",
                    signatureAlgorithm.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", signatureAlgorithm.getChildAt(1));
        }
        Asn1TestUtil.assertNodeMatches(0, "signature: 0x1122334455", node.getChildAt(5));
        {
            final AbstractAsn1TreeNode unsignedAttrs = node.getChildAt(6);
            Asn1TestUtil.assertNodeMatches(1, "unsignedAttrs", unsignedAttrs);
            {
                final AbstractAsn1TreeNode attr = unsignedAttrs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "attribute", attr);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "attrType: 1.2.840.113549.1.9.25.4 (/iso/member-body/us/rsadsi/pkcs"
                                + "/pkcs-9/pkcs-9-at/sequenceNumber)",
                        attr.getChildAt(0)
                );
                {
                    final AbstractAsn1TreeNode values = attr.getChildAt(1);
                    Asn1TestUtil.assertNodeMatches(1, "attrValues", values);
                    Asn1TestUtil.assertNodeMatches(0, "sequenceNumber: 3", values.getChildAt(0));
                }
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        /*
         * No need to fill everything here, as it will be massive...
         * It is tested individually anyway.
         */
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1"), // id-sha256
                                        DERNull.INSTANCE,
                                })
                        ),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.1.4"),  // id-ct-TSTInfo
                                new DERTaggedObject(true, 0,
                                        new DEROctetString(new byte[] {0x30, 0x00})
                                ),
                        }),
                        new DERTaggedObject(false, 0, new DLSet(new ASN1Encodable[] {
                                new DERSequence(
                                        new DERSequence()
                                ),
                                new DERTaggedObject(false, 0, new DERSequence()),
                                new DERTaggedObject(false, 1, new DERSequence()),
                                new DERTaggedObject(false, 2, new DERSequence()),
                                new DERTaggedObject(false, 3, new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("1.2"),    // member-body
                                        DERNull.INSTANCE,
                                })),
                        })),
                        new DERTaggedObject(false, 1, new DLSet(new ASN1Encodable[] {
                                new DERSequence(
                                        new DERSequence()
                                ),
                                new DERTaggedObject(false, 1, new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("1.2"),    // member-body
                                        DERNull.INSTANCE,
                                })),
                        })),
                        new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(
                                                        new DERSet()
                                                ),
                                                new ASN1Integer(42),
                                        }),
                                        new DERSequence(new ASN1Encodable[] {
                                                new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1"), // id-sha256
                                                DERNull.INSTANCE,
                                        }),
                                        new DERTaggedObject(false, 0, new DERSet(
                                                new DERSequence(new ASN1Encodable[] {
                                                        // sequenceNumber
                                                        new ASN1ObjectIdentifier("1.2.840.113549.1.9.25.4"),
                                                        new DERSet(
                                                                new ASN1Integer(2)
                                                        )
                                                })
                                        )),
                                        new DERSequence(new ASN1Encodable[] {
                                                new ASN1ObjectIdentifier("1.2.840.113549.1.1.1"),   // rsaEncryption
                                                DERNull.INSTANCE,
                                        }),
                                        new DEROctetString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}),
                                        new DERTaggedObject(false, 1, new DERSet(
                                                new DERSequence(new ASN1Encodable[] {
                                                        // sequenceNumber
                                                        new ASN1ObjectIdentifier("1.2.840.113549.1.9.25.4"),
                                                        new DERSet(
                                                                new ASN1Integer(3)
                                                        )
                                                })
                                        )),
                                })
                        )
                })
        );
    }

    private static void testVersion(int value, String description) {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new ASN1Integer(value)
                )
        );
        SignedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "signedData", node);
        if (description == null) {
            Asn1TestUtil.assertNodeMatches(0, "version: " + value, node.getChildAt(0));
        } else {
            Asn1TestUtil.assertNodeMatches(0,
                    "version: " + value + " (" + description + ")",
                    node.getChildAt(0)
            );
        }
    }
}